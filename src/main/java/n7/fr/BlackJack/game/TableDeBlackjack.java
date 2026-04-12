package n7.fr.BlackJack.game;

import java.util.HashMap;
import java.util.Map;

public class TableDeBlackjack {
    private String idTable; 
    private Paquet paquet;
    private Main mainCroupier;
    
    private Map<String, Main> joueursMains;

    public TableDeBlackjack(String idTable) {
        this.idTable = idTable;
        this.paquet = new Paquet();
        this.mainCroupier = new Main();
        this.joueursMains = new HashMap<>();
    }

    public void ajouterJoueur(String pseudo) {
        joueursMains.put(pseudo, new Main());
    }

    public void demarrerManche() {
        mainCroupier.ajouterCarte(paquet.tirerCarte());
        mainCroupier.ajouterCarte(paquet.tirerCarte()); // à cacher !!!!!
        
        for (Main main : joueursMains.values()) {
            main.ajouterCarte(paquet.tirerCarte());
            main.ajouterCarte(paquet.tirerCarte());
        }
    }

    public void joueurTire(String pseudo) {
        Main main = joueursMains.get(pseudo);
        if (main != null && main.calculerScore() < 21) {
            main.ajouterCarte(paquet.tirerCarte());
        }
    }

    public Main getMainCroupier() { return mainCroupier; }
    public Map<String, Main> getMainsJoueurs() { return joueursMains; }
    public String getIdTable() { return idTable; }
}