package n7.fr.BlackJack.websocket;

import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import n7.fr.BlackJack.game.TableDeBlackjack;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.io.IOException;

@ServerEndpoint("/ws/blackjack/{idTable}/{pseudo}")
public class BlackjackEndpoint {

    private static final Map<String, TableDeBlackjack> tablesActives = new ConcurrentHashMap<>();
    
    private static final Map<Session, String> sessionsJoueurs = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session, @PathParam("idTable") String idTable, @PathParam("pseudo") String pseudo) {
        tablesActives.putIfAbsent(idTable, new TableDeBlackjack(idTable));
        TableDeBlackjack table = tablesActives.get(idTable);

        table.ajouterJoueur(pseudo);
        sessionsJoueurs.put(session, pseudo);
    }

    @OnMessage
    public void onMessage(String action, Session session, @PathParam("idTable") String idTable) {
        String pseudo = sessionsJoueurs.get(session);
        TableDeBlackjack table = tablesActives.get(idTable);

        if ("TIRER".equals(action)) {
            table.joueurTire(pseudo);
        } 
        else if ("DEMARRER".equals(action)) {
            table.demarrerManche();
        }
    }

    @OnClose
    public void onClose(Session session, @PathParam("idTable") String idTable) {
        String pseudo = sessionsJoueurs.remove(session);
    }
}