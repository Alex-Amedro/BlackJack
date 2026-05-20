package n7.fr.BlackJack.game;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class TableDeBlackjack {
    private String idTable;
    private Paquet paquet;
    private Main mainCroupier;
    private int numeroManche;

    // Phase du jeu : "waiting" → "betting" → "playing" → "results"
    private String phase;

    private Map<String, Main> joueursMains;
    private Map<String, Integer> mises;
    private Map<String, Integer> soldes;
    private Map<String, Boolean> joueursTerminesMise;
    private Map<String, Boolean> joueursTermines;

    public TableDeBlackjack(String idTable) {
        this.idTable = idTable;
        this.paquet = new Paquet();
        this.mainCroupier = new Main();
        this.joueursMains = new HashMap<>();
        this.mises = new HashMap<>();
        this.soldes = new HashMap<>();
        this.joueursTerminesMise = new HashMap<>();
        this.joueursTermines = new HashMap<>();
        this.phase = "waiting";
        this.numeroManche = 0;
    }

    // ─── Gestion des joueurs ───────────────────────────────

    public void ajouterJoueur(String pseudo, int soldeEnBanque) {
        if (joueursMains.containsKey(pseudo)) return;
        joueursMains.put(pseudo, new Main());
        mises.put(pseudo, 0);
        soldes.put(pseudo, soldeEnBanque);
        joueursTermines.put(pseudo, false);
        joueursTerminesMise.put(pseudo, false);

        // Si on est en playing, distribuer des cartes au nouveau
        if ("playing".equals(phase)) {
            Main main = joueursMains.get(pseudo);
            main.ajouterCarte(paquet.tirerCarte());
            main.ajouterCarte(paquet.tirerCarte());
        }

        // Si on est en waiting et 2+ joueurs → lancer le tour de mise
        if ("waiting".equals(phase) && joueursMains.size() >= 2) {
            lancerTourDeMise();
        }
    }

    public void retirerJoueur(String pseudo) {
        joueursMains.remove(pseudo);
        mises.remove(pseudo);
        soldes.remove(pseudo);
        joueursTermines.remove(pseudo);
        joueursTerminesMise.remove(pseudo);

        // Si on est en betting et que tous les restants ont misé → distribuer
        if ("betting".equals(phase) && !joueursMains.isEmpty() && allPlayerBet()) {
            distribuerCartes();
        }
    }

    public boolean estVide() {
        return joueursMains.isEmpty();
    }

    // ─── Phase BETTING ─────────────────────────────────────

    /**
     * Lance un tour de mise. Appelé automatiquement quand 2+ joueurs
     * sont à la table, ou quand on recommence après les résultats.
     */
    public void lancerTourDeMise() {
        numeroManche++;
        phase = "betting";
        paquet = new Paquet(); // nouveau paquet
        mainCroupier = new Main();

        for (String p : joueursMains.keySet()) {
            joueursMains.put(p, new Main());
            mises.put(p, 0);
            joueursTerminesMise.put(p, false);
            joueursTermines.put(p, false);
        }
    }

    public void placerMise(String pseudo, int montant) {
        if (!"betting".equals(phase)) return;
        if (soldes.containsKey(pseudo) && soldes.get(pseudo) >= montant && montant > 0) {
            mises.put(pseudo, montant);
            soldes.put(pseudo, soldes.get(pseudo) - montant);
            joueursTerminesMise.put(pseudo, true);
        }

        // Si tous les joueurs ont misé → distribuer les cartes
        if (allPlayerBet()) {
            distribuerCartes();
        }
    }

    public boolean allPlayerBet() {
        if (joueursTerminesMise.isEmpty()) return false;
        for (Boolean termineMise : joueursTerminesMise.values()) {
            if (!termineMise) return false;
        }
        return true;
    }

    // ─── Phase PLAYING ─────────────────────────────────────

    /**
     * Distribue les cartes au croupier et à tous les joueurs.
     * Transition automatique betting → playing.
     */
    private void distribuerCartes() {
        phase = "playing";
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
        if (!"playing".equals(phase)) return;
        Main main = joueursMains.get(pseudo);
        if (main != null && main.calculerScore() < 21) {
            main.ajouterCarte(paquet.tirerCarte());
        }
        if (main != null && main.calculerScore() >= 21) {
            joueursTermines.put(pseudo, true);
        }
    }

    public void joueurStand(String pseudo) {
        if (!"playing".equals(phase)) return;
        joueursTermines.put(pseudo, true);
    }

    public void joueurDouble(String pseudo) {
        if (!"playing".equals(phase)) return;

        int solde = soldes.getOrDefault(pseudo, 0);
        int miseActuelle = mises.getOrDefault(pseudo, 0);

        // Vérifier si le joueur a assez d'argent pour doubler sa mise
        if (solde >= miseActuelle) {
            // Déduire l'argent du solde et doubler la mise
            soldes.put(pseudo, solde - miseActuelle);
            mises.put(pseudo, miseActuelle * 2);

            // Tirer exactement une carte
            Main main = joueursMains.get(pseudo);
            if (main != null) {
                main.ajouterCarte(paquet.tirerCarte());
            }

            // Fin du tour obligatoire
            joueursTermines.put(pseudo, true);
        }
    }

    public boolean tousLesJoueursTermines() {
        for (Boolean termine : joueursTermines.values()) {
            if (!termine) return false;
        }
        return !joueursTermines.isEmpty();
    }

    // ─── Phase RESULTS ─────────────────────────────────────

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
            int mise = mises.getOrDefault(pseudo, 0);
            String resultat = resultats.get(pseudo);
            int soldeActuel = soldes.getOrDefault(pseudo, 0);

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
        phase = "results";
    }

    // ─── Getters ───────────────────────────────────────────

    public String getPhase() { return phase; }
    public int getMise(String pseudo) { return mises.getOrDefault(pseudo, 0); }
    public int getSolde(String pseudo) { return soldes.getOrDefault(pseudo, 0); }
    public boolean estMancheTerminee() { return "results".equals(phase); }
    public boolean joueurAFiniDeJouer(String pseudo) { return joueursTermines.getOrDefault(pseudo, false); }
    public boolean joueurAMise(String pseudo) { return joueursTerminesMise.getOrDefault(pseudo, false); }
    public Main getMainCroupier() { return mainCroupier; }
    public Map<String, Main> getMainsJoueurs() { return joueursMains; }
    public String getIdTable() { return idTable; }
    public Set<String> getJoueurs() { return joueursMains.keySet(); }
    public int getNumeroManche() { return numeroManche; }
}