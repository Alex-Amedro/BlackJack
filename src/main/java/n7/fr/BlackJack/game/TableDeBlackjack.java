package n7.fr.BlackJack.game;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class TableDeBlackjack {
    private String idTable;
    private Paquet paquet;
    private Main mainCroupier;
    private int numeroManche;

    private Map<String, Main> joueursMains;
    private Map<String, Integer> mises;
    private Map<String, Integer> soldes;
    private Map<String, Boolean> joueursTermines;
    private boolean mancheTerminee;

    public TableDeBlackjack(String idTable) {
        this.idTable = idTable;
        this.paquet = new Paquet();
        this.mainCroupier = new Main();
        this.joueursMains = new HashMap<>();
        this.mises = new HashMap<>();
        this.soldes = new HashMap<>();
        this.joueursTermines = new HashMap<>();
        this.mancheTerminee = false;
        this.numeroManche = 0;
    }

    public void ajouterJoueur(String pseudo) {
        joueursMains.put(pseudo, new Main());
        mises.put(pseudo, 0);
        soldes.put(pseudo, 1000);
        joueursTermines.put(pseudo, false);
    }

    public void placerMise(String pseudo, int montant) {
        if (soldes.containsKey(pseudo) && soldes.get(pseudo) >= montant && montant > 0) {
            mises.put(pseudo, montant);
            soldes.put(pseudo, soldes.get(pseudo) - montant);
        }
    }

    public void demarrerManche() {
        numeroManche++;
        mancheTerminee = false;
        mainCroupier = new Main();

        mainCroupier.ajouterCarte(paquet.tirerCarte());
        mainCroupier.ajouterCarte(paquet.tirerCarte());

        for (String pseudo : joueursMains.keySet()) {
            joueursMains.put(pseudo, new Main());
            joueursTermines.put(pseudo, false);
            Main main = joueursMains.get(pseudo);
            main.ajouterCarte(paquet.tirerCarte());
            main.ajouterCarte(paquet.tirerCarte());
        }
    }

    public void joueurTire(String pseudo) {
        Main main = joueursMains.get(pseudo);
        if (main != null && main.calculerScore() < 21) {
            main.ajouterCarte(paquet.tirerCarte());
        }
        if (main != null && main.calculerScore() >= 21) {
            joueursTermines.put(pseudo, true);
        }
    }

    public void joueurStand(String pseudo) {
        joueursTermines.put(pseudo, true);
    }

    public boolean tousLesJoueursTermines() {
        for (Boolean termine : joueursTermines.values()) {
            if (!termine) return false;
        }
        return true;
    }

    public void jouerCroupier() {
        while (mainCroupier.calculerScore() < 17) {
            mainCroupier.ajouterCarte(paquet.tirerCarte());
        }
    }

    public Map<String, String> determinerResultats() {
        Map<String, String> resultats = new HashMap<>();
        int scoreCroupier = mainCroupier.calculerScore();
        boolean croupierBust = scoreCroupier > 21;

        for (String pseudo : joueursMains.keySet()) {
            Main main = joueursMains.get(pseudo);
            int scoreJoueur = main.calculerScore();
            boolean joueurBust = scoreJoueur > 21;

            if (joueurBust) {
                resultats.put(pseudo, "LOSE");
            } else if (croupierBust) {
                resultats.put(pseudo, "WIN");
            } else if (scoreJoueur > scoreCroupier) {
                resultats.put(pseudo, "WIN");
            } else if (scoreJoueur < scoreCroupier) {
                resultats.put(pseudo, "LOSE");
            } else {
                resultats.put(pseudo, "PUSH");
            }
        }

        return resultats;
    }

    public void mettreAJourSoldes(Map<String, String> resultats) {
        for (String pseudo : resultats.keySet()) {
            int mise = mises.get(pseudo);
            String resultat = resultats.get(pseudo);
            int soldeActuel = soldes.get(pseudo);

            if ("WIN".equals(resultat)) {
                soldes.put(pseudo, soldeActuel + mise * 2);
            } else if ("PUSH".equals(resultat)) {
                soldes.put(pseudo, soldeActuel + mise);
            }
        }
    }

    public void terminerManche() {
        jouerCroupier();
        Map<String, String> resultats = determinerResultats();
        mettreAJourSoldes(resultats);
        mancheTerminee = true;
    }

    public void reinitialiserManche() {
        paquet = new Paquet();
        mainCroupier = new Main();
        joueursMains.clear();
        mises.clear();
        joueursTermines.clear();
        mancheTerminee = false;

        for (String pseudo : soldes.keySet()) {
            joueursMains.put(pseudo, new Main());
            mises.put(pseudo, 0);
            joueursTermines.put(pseudo, false);
        }
    }

    public int getMise(String pseudo) {
        return mises.getOrDefault(pseudo, 0);
    }

    public int getSolde(String pseudo) {
        return soldes.getOrDefault(pseudo, 0);
    }

    public boolean estBlackjack(String pseudo) {
        Main main = joueursMains.get(pseudo);
        return main != null && main.getCartes().size() == 2 && main.calculerScore() == 21;
    }

    public boolean estBust(String pseudo) {
        Main main = joueursMains.get(pseudo);
        return main != null && main.calculerScore() > 21;
    }

    public boolean estMancheTerminee() {
        return mancheTerminee;
    }

    public boolean joueurAFiniDeJouer(String pseudo) {
        return joueursTermines.getOrDefault(pseudo, false);
    }

    public void marquerJoueurCommeTermine(String pseudo) {
        joueursTermines.put(pseudo, true);
    }

    public Main getMainCroupier() { return mainCroupier; }
    public Map<String, Main> getMainsJoueurs() { return joueursMains; }
    public String getIdTable() { return idTable; }
    public Set<String> getJoueurs() { return joueursMains.keySet(); }
    public int getNumeroManche() { return numeroManche; }
}