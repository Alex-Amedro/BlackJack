package n7.fr.BlackJack.websocket;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.google.gson.Gson;

import jakarta.websocket.OnClose;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import n7.fr.BlackJack.game.TableDeBlackjack;

@ServerEndpoint("/ws/blackjack/{idTable}/{pseudo}")
public class Blackjack_websocket {

    private static final Map<String, TableDeBlackjack> tablesActives = new ConcurrentHashMap<>();
    private static final Map<String, java.util.Set<Session>> tableSessions = new ConcurrentHashMap<>();
    private static final Map<Session, String> sessionsJoueurs = new ConcurrentHashMap<>();
    private static final Gson gson = new Gson();

    @OnOpen
    public void onOpen(Session session, @PathParam("idTable") String idTable, @PathParam("pseudo") String pseudo) {
        // Créer la table si elle n'existe pas
        tablesActives.putIfAbsent(idTable, new TableDeBlackjack(idTable));
        tableSessions.putIfAbsent(idTable, ConcurrentHashMap.newKeySet());

        TableDeBlackjack table = tablesActives.get(idTable);
        table.ajouterJoueur(pseudo);

        tableSessions.get(idTable).add(session);
        sessionsJoueurs.put(session, pseudo);

        broadcasterEtat(idTable);
    }

    @OnMessage
    public void onMessage(String message, Session session, @PathParam("idTable") String idTable) {
        String pseudo = sessionsJoueurs.get(session);
        TableDeBlackjack table = tablesActives.get(idTable);

        if (table == null) return;

        // Parser: "HIT", "STAND", "START", "BET:100"
        if ("HIT".equals(message)) {
            table.joueurTire(pseudo);
        }
        else if ("STAND".equals(message)) {
            table.joueurStand(pseudo);
            if (table.tousLesJoueursTermines()) {
                table.terminerManche();
            }
        }
        else if ("START".equals(message)) {
            table.demarrerManche();
        }
        else if (message.startsWith("BET:")) {
            int montant = Integer.parseInt(message.substring(4));
            table.placerMise(pseudo, montant);
        }

        broadcasterEtat(idTable);
    }

    @OnClose
    public void onClose(Session session, @PathParam("idTable") String idTable) {
        sessionsJoueurs.remove(session);
        tableSessions.get(idTable).remove(session);
    }

    // Broadcaster l'état actuel à tous les joueurs de la table
    private void broadcasterEtat(String idTable) {
        TableDeBlackjack table = tablesActives.get(idTable);
        if (table == null) return;

        Map<String, Object> etat = new java.util.HashMap<>();
        etat.put("dealerCards", table.getMainCroupier().getCartes());
        etat.put("dealerScore", table.getMainCroupier().calculerScore());
        etat.put("joueurs", table.getJoueurs());
        etat.put("roundNumber", table.getNumeroManche());
        etat.put("miseTerminee", table.estMancheTerminee());

        String json = gson.toJson(etat);

        for (Session s : tableSessions.get(idTable)) {
            try {
                s.getBasicRemote().sendText(json);
            } catch (IOException e) {
                System.err.println("Error sending websocket message: " + e.getMessage());
            }
        }
    }
}