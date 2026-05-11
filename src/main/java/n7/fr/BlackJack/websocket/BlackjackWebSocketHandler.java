package n7.fr.BlackJack.websocket;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import n7.fr.BlackJack.game.*;
import n7.fr.BlackJack.service.TableManager;
import com.google.gson.Gson;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class BlackjackWebSocketHandler extends TextWebSocketHandler {

    private final TableManager tableManager;
    private static final Gson gson = new Gson();

    private final Map<String, Set<WebSocketSession>> tableSessions = new ConcurrentHashMap<>();
    private final Map<String, String> sessionPseudo = new ConcurrentHashMap<>();
    private final Map<String, String> sessionTableId = new ConcurrentHashMap<>();

    public BlackjackWebSocketHandler(TableManager tableManager) {
        this.tableManager = tableManager;
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
        String pseudo = URLDecoder.decode(parts[4], StandardCharsets.UTF_8);

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
            }
        } else if ("STAND".equals(payload)) {
            table.joueurStand(pseudo);
            if (table.tousLesJoueursTermines()) {
                table.terminerManche();
            }
        } else if ("START".equals(payload)) {
            table.demarrerManche();
        } else if (payload.startsWith("BET:")) {
            try {
                int amount = Integer.parseInt(payload.substring(4));
                table.placerMise(pseudo, amount);
            } catch (NumberFormatException ignored) {}
        }

        broadcastState(tableId);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String tableId = sessionTableId.remove(session.getId());
        sessionPseudo.remove(session.getId());
        if (tableId != null) {
            Set<WebSocketSession> sessions = tableSessions.get(tableId);
            if (sessions != null) sessions.remove(session);
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
        info.put("phase", table.estMancheTerminee() ? "results"
                : (table.getNumeroManche() > 0 ? "playing" : "waiting"));
        info.put("dealerCards", mapCards(table.getMainCroupier()));
        info.put("dealerScore", table.getMainCroupier().calculerScore());
        info.put("players", buildPlayers(table));
        if (table.estMancheTerminee()) {
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
            players.put(pseudo, p);
        }
        return players;
    }
}
