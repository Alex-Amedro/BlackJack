package n7.fr.BlackJack.game;

public class Carte {

    public enum Couleur { COEUR, CARREAU, TREFLE, PIQUE }
    
    public enum Valeur { 
        DEUX(2), TROIS(3), QUATRE(4),
        CINQ(5), SIX(6), SEPT(7), 
        HUIT(8), NEUF(9), DIX(10),
        VALET(10), DAME(10), ROI(10), AS(11);

        private final int points;
        Valeur(int points) { this.points = points; }
        public int getPoints() { return points; }
    }

    private final Couleur couleur;
    private final Valeur valeur;

    public Carte(Couleur couleur, Valeur valeur) {
        this.couleur = couleur;
        this.valeur = valeur;
    }

    public Couleur getCouleur() { return couleur; }
    public Valeur getValeur() { return valeur; }
    public int getPoints() { return valeur.getPoints(); }

    public String getAssetName() {
        String valueName = switch (valeur) {
            case VALET -> "jack";
            case DAME -> "queen";
            case ROI -> "king";
            case AS -> "as";
            default -> String.valueOf(valeur.getPoints());
        };

        String suitName = switch (couleur) {
            case COEUR -> "coeur";
            case CARREAU -> "diamant";
            case TREFLE -> "trefle";
            case PIQUE -> "pique";
        };

        return valueName + "_" + suitName;
    }

    @Override
    public String toString() {
        return valeur + " de " + couleur;
    }
}