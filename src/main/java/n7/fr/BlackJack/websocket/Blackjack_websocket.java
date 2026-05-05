package n7.fr.BlackJack.websocket;

import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import n7.fr.BlackJack.game.TableDeBlackjack;
import n7.fr.BlackJack.game.Main;
import n7.fr.BlackJack.service.TableManager;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.io.IOException;

@ServerEndpoint("/ws/blackjack/{idTable}/{pseudo}")
public class Blackjack_websocket {

    private static final Map<String, java.util.Set<Session>> tableSessions = new ConcurrentHashMap<>();
    private static final Map<Session, String> sessionsJoueurs = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session, @PathParam("idTable") String idTable, @PathParam("pseudo") String pseudo) {
        // Obtenir la table crée par l'API REST (via TableController)
        TableDeBlackjack table = TableManager.getTableStatic(idTable);
        if (table == null) {
            System.out.println("[WS ERROR] La table " + idTable + " n'existe pas en static!");
            return;
        }
        
        tableSessions.putIfAbsent(idTable, ConcurrentHashMap.newKeySet());

        // Le joueur l'ajoute qu'à l'ouverture de la connexion WS !
        table.ajouterJoueur(pseudo);

        tableSessions.get(idTable).add(session);
        sessionsJoueurs.put(session, pseudo);

        System.out.println("[WS] onOpen: " + pseudo + " joined " + idTable);
        broadcasterEtat(idTable);
    }

    @OnMessage
    public void onMessage(String message, Session session, @PathParam("idTable") String idTable) {
        String pseudo = sessionsJoueurs.get(session);
        System.out.println("[WS] onMessage from " + pseudo + ": " + message);
        TableDeBlackjack table = TableManager.getTableStatic(idTable);

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
            if (table.getMise(pseudo) > 0) {
                table.demarrerManche();
                System.out.println("[WS] Game started. Manche: " + table.getNumeroManche());
            } else {
                System.out.println("[WS] Cannot start: " + pseudo + " has no bet");
            }
        }
        else if (message.startsWith("BET:")) {
            try {
                int montant = Integer.parseInt(message.substring(4));
                table.placerMise(pseudo, montant);
                System.out.println("[WS] BET placed by " + pseudo + ": " + montant + " - Solde: " + table.getSolde(pseudo));
            } catch (NumberFormatException e) {
                System.out.println("[WS] Invalid bet format: " + message);
            }
        }

        broadcasterEtat(idTable);
    }

    @OnClose
    public void onClose(Session session, @PathParam("idTable") String idTable) {
        String pseudo = sessionsJoueurs.remove(session);
        tableSessions.get(idTable).remove(session);
    }

    // Broadcaster l'état actuel à tous les joueurs de la table
    private void broadcasterEtat(String idTable) {
        TableDeBlackjack table = TableManager.getTableStatic(idTable);
        if (table == null) return;

        java.util.Set<Session> sessions = tableSessions.get(idTable);
        if (sessions != null) {
            for (Session s : sessions) {
                try {
                    String pseudo = sessionsJoueurs.get(s);
                    String json = buildJsonForPlayer(table, pseudo);
                    System.out.println("[WS] Sending JSON to " + pseudo + ": " + json);
                    s.getBasicRemote().sendText(json);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private String getCardFileName(n7.fr.BlackJack.game.Carte carte) {
        String v;
        switch (carte.getValeur()) {
            case DEUX: v = "2"; break;
            case TROIS: v = "3"; break;
            case QUATRE: v = "4"; break;
            case CINQ: v = "5"; break;
            case SIX: v = "6"; break;
            case SEPT: v = "7"; break;
            case HUIT: v = "8"; break;
            case NEUF: v = "9"; break;
            case DIX: v = "10"; break;
            case VALET: v = "valet"; break;
            case DAME: v = "dame"; break;
            case ROI: v = "roi"; break;
            case AS: v = "as"; break;
            default: v = "2";
        }
        String c = carte.getCouleur().name().toLowerCase();
        return v + "_" + c;
    }

    private String buildJsonForPlayer(TableDeBlackjack table, String pseudoCurrent) {
        StringBuilder json = new StringBuilder("{");

        // Determination de la phase ("betting", "playing", "results")
        String phase = "betting";
        if (table.getNumeroManche() > 0) {
            phase = table.estMancheTerminee() ? "results" : "playing";
        }
        json.append("\"phase\":\"").append(phase).append("\",");

        // Croupier
        json.append("\"dealerCards\":[");
        java.util.List<n7.fr.BlackJack.game.Carte> dc = table.getMainCroupier().getCartes();
        for (int i = 0; i < dc.size(); i++) {
            json.append("\"").append(getCardFileName(dc.get(i))).append("\"");
            if (i < dc.size() - 1) json.append(",");
        }
        json.append("],");
        json.append("\"dealerScore\":").append(table.getMainCroupier().calculerScore()).append(",");

        // Joueur Actuel
        n7.fr.BlackJack.game.Main mainCurrent = table.getMainsJoueurs().get(pseudoCurrent);
        int scoreCurrent = mainCurrent != null ? mainCurrent.calculerScore() : 0;
        json.append("\"playerCards\":[");
        if (mainCurrent != null) {
            java.util.List<n7.fr.BlackJack.game.Carte> pc = mainCurrent.getCartes();
            for (int i = 0; i < pc.size(); i++) {
                json.append("\"").append(getCardFileName(pc.get(i))).append("\"");
                if (i < pc.size() - 1) json.append(",");
            }
        }
        json.append("],");
        json.append("\"playerScore\":").append(scoreCurrent).append(",");
        json.append("\"playerBalance\":").append(table.getSolde(pseudoCurrent)).append(",");
        json.append("\"playerBet\":").append(table.getMise(pseudoCurrent)).append(",");

        // Autres Joueurs
        json.append("\"allPlayers\":{");
        boolean first = true;
        for (String p : table.getJoueurs()) {
            if (p.equals(pseudoCurrent)) continue; // Ne pas inclure le joueur actuel comme "autre joueur"
            
            if (!first) json.append(",");
            json.append("\"").append(p).append("\":{");
            
            n7.fr.BlackJack.game.Main opm = table.getMainsJoueurs().get(p);
            json.append("\"cards\":[");
            if (opm != null) {
                java.util.List<n7.fr.BlackJack.game.Carte> opc = opm.getCartes();
                for (int i = 0; i < opc.size(); i++) {
                    json.append("\"").append(getCardFileName(opc.get(i))).append("\"");
                    if (i < opc.size() - 1) json.append(",");
                }
            }
            json.append("],");
            int scoreP = opm != null ? opm.calculerScore() : 0;
            json.append("\"score\":").append(scoreP).append(",");
            json.append("\"bet\":").append(table.getMise(p));
            
            json.append("}");
            first = false;
        }
        json.append("},");

        json.append("\"roundNumber\":").append(Math.max(1, table.getNumeroManche())).append(",");
        json.append("\"message\":\"\"");
        json.append("}");

        return json.toString();
    }
}