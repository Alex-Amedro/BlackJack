package n7.fr.BlackJack.websocket;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.google.gson.Gson;

import n7.fr.BlackJack.game.Carte;
import n7.fr.BlackJack.game.Main;
import n7.fr.BlackJack.game.TableDeBlackjack;
import n7.fr.BlackJack.service.AuthTokenService;
import n7.fr.BlackJack.service.TableManager;

@Component
public class BlackjackWebSocketHandler extends TextWebSocketHandler {

    private final TableManager tableManager;
    private static final Gson gson = new Gson();
    private final AuthTokenService authTokenService;
    private final n7.fr.BlackJack.repository.JoueurRepository joueurRepository;

    private final Map<String, Set<WebSocketSession>> tableSessions = new ConcurrentHashMap<>();
    private final Map<String, String> sessionPseudo = new ConcurrentHashMap<>();
    private final Map<String, String> sessionTableId = new ConcurrentHashMap<>();

    public BlackjackWebSocketHandler(TableManager tableManager, AuthTokenService authTokenService, n7.fr.BlackJack.repository.JoueurRepository joueurRepository) {
        this.tableManager = tableManager;
        this.authTokenService = authTokenService;
        this.joueurRepository = joueurRepository;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String path = session.getUri().getPath();
        String[] parts = path.split("/");
        if (parts.length < 5) {
            session.close(CloseStatus.BAD_DATA);
            return;
        }

        String tableId = URLDecoder.decode(parts[3], StandardCharsets.UTF_8);
        String token = URLDecoder.decode(parts[4], StandardCharsets.UTF_8);

        // Security Check: Look up the real pseudo using the secure token
        String pseudo = authTokenService.getPseudoAndRemoveToken(token);
        if (pseudo == null) {
            System.err.println("WS connexion refusée: Token invalide !");
            session.close(CloseStatus.NOT_ACCEPTABLE);
            return;
        }

        sessionPseudo.put(session.getId(), pseudo);
        sessionTableId.put(session.getId(), tableId);
        tableSessions.computeIfAbsent(tableId, k -> ConcurrentHashMap.newKeySet()).add(session);

        System.out.println("WS connecté: " + pseudo + " sur table " + tableId);
        // On n'envoie PAS l'état ici — le client enverra "REFRESH" quand il est prêt
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String pseudo = sessionPseudo.get(session.getId());
        String tableId = sessionTableId.get(session.getId());
        if (pseudo == null || tableId == null) return;

        TableDeBlackjack table = tableManager.getTable(tableId);
        if (table == null) return;

        String payload = message.getPayload().trim();

        if ("REFRESH".equals(payload)) {
            // Le client demande l'état actuel
            sendStateTo(session, tableId);
            return;
        } else if ("HIT".equals(payload)) {
            table.joueurTire(pseudo);
            if (table.tousLesJoueursTermines()) {
                table.terminerManche();
                syncBalancesWithDatabase(table); // SAUVEGARDER
            }
        } else if ("STAND".equals(payload)) {
            table.joueurStand(pseudo);
            if (table.tousLesJoueursTermines()) {
                table.terminerManche();
                syncBalancesWithDatabase(table); // SAUVEGARDER
            }
        } else if ("DOUBLE".equals(payload)) {
            table.joueurDouble(pseudo);
            if (table.tousLesJoueursTermines()) {
                table.terminerManche();
                syncBalancesWithDatabase(table); // SAUVEGARDER
            }
        } else if ("START".equals(payload)) {
            table.lancerTourDeMise();
        } else if (payload.startsWith("BET:")) {
            try {
                int amount = Integer.parseInt(payload.substring(4));
                table.placerMise(pseudo, amount);
                syncBalancesWithDatabase(table); // SAUVEGARDER LA MISE (Dédouanement)
            } catch (NumberFormatException ignored) {}
        }

        broadcastState(tableId);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String tableId = sessionTableId.remove(session.getId());
        String pseudo = sessionPseudo.remove(session.getId());
        if (tableId != null) {
            Set<WebSocketSession> sessions = tableSessions.get(tableId);
            if (sessions != null) sessions.remove(session);

            // Retirer le joueur de la table
            if (pseudo != null) {
                TableDeBlackjack table = tableManager.getTable(tableId);
                if (table != null) {
                    table.retirerJoueur(pseudo);
                    System.out.println("Joueur " + pseudo + " retiré de " + tableId);

                    if (table.estVide()) {
                        // Supprimer la table vide
                        tableManager.removeTable(tableId);
                        tableSessions.remove(tableId);
                        System.out.println("Table " + tableId + " supprimée (vide)");
                    } else {
                        // Si tous les joueurs restants ont fini, terminer la manche
                        if (!table.estMancheTerminee() && table.getNumeroManche() > 0
                                && table.tousLesJoueursTermines()) {
                            table.terminerManche();
                        }
                        broadcastState(tableId);
                    }
                }
            }
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable ex) {
        System.err.println("WS transport error: " + ex.getMessage());
    }

    /**
     * Envoie l'état à une seule session (thread-safe via synchronized).
     */
    private void sendStateTo(WebSocketSession session, String tableId) {
        TableDeBlackjack table = tableManager.getTable(tableId);
        if (table == null) return;

        String json = gson.toJson(buildSnapshot(table));
        synchronized (session) {
            try {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(json));
                }
            } catch (IOException e) {
                System.err.println("Erreur envoi état: " + e.getMessage());
            }
        }
    }

    /**
     * Envoie l'état de la table à tous les clients WS connectés.
     */
    public void broadcastState(String tableId) {
        TableDeBlackjack table = tableManager.getTable(tableId);
        if (table == null) return;

        Set<WebSocketSession> sessions = tableSessions.get(tableId);
        if (sessions == null || sessions.isEmpty()) return;

        String json = gson.toJson(buildSnapshot(table));
        TextMessage msg = new TextMessage(json);

        for (WebSocketSession s : new ArrayList<>(sessions)) {
            synchronized (s) {
                try {
                    if (s.isOpen()) {
                        s.sendMessage(msg);
                    }
                } catch (IOException e) {
                    System.err.println("WS broadcast error: " + e.getMessage());
                    sessions.remove(s);
                    sessionPseudo.remove(s.getId());
                    sessionTableId.remove(s.getId());
                }
            }
        }
    }

    private Map<String, Object> buildSnapshot(TableDeBlackjack table) {
        Map<String, Object> info = new HashMap<>();
        info.put("id", table.getIdTable());
        info.put("name", "Table " + table.getIdTable());
        info.put("playerCount", table.getJoueurs().size());
        info.put("maxPlayers", 7);
        info.put("roundNumber", table.getNumeroManche());
        info.put("phase", table.getPhase());
        info.put("dealerCards", mapCards(table.getMainCroupier()));
        info.put("dealerScore", table.getMainCroupier().calculerScore());
        info.put("players", buildPlayers(table));
        if ("results".equals(table.getPhase())) {
            info.put("resultats", table.determinerResultats());
        }
        return info;
    }

    private List<String> mapCards(Main main) {
        List<String> cards = new ArrayList<>();
        for (Carte c : main.getCartes()) cards.add(c.getAssetName());
        return cards;
    }

    private Map<String, Object> buildPlayers(TableDeBlackjack table) {
        Map<String, Object> players = new HashMap<>();
        for (String pseudo : table.getJoueurs()) {
            Map<String, Object> p = new HashMap<>();
            Main main = table.getMainsJoueurs().get(pseudo);
            p.put("cards", main == null ? List.of() : mapCards(main));
            p.put("score", main == null ? 0 : main.calculerScore());
            p.put("bet", table.getMise(pseudo));
            p.put("balance", table.getSolde(pseudo));
            p.put("finished", table.joueurAFiniDeJouer(pseudo));
            p.put("hasBet", table.joueurAMise(pseudo));
            players.put(pseudo, p);
        }
        return players;
    }

    private void syncBalancesWithDatabase(TableDeBlackjack table) {
        for (String pseudo : table.getJoueurs()) {
            n7.fr.BlackJack.entity.Joueur joueur = joueurRepository.findByPseudo(pseudo);
            if (joueur != null) {
                joueur.setSolde(table.getSolde(pseudo));
                joueurRepository.save(joueur);
            }
        }
    }
}
