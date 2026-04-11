package n7.fr.BlackJack.game;

import java.util.ArrayList;
import java.util.List;

public class Main {

    private List<Carte> cartes = new ArrayList<>();

    public void ajouterCarte(Carte carte) {
        cartes.add(carte);
    }

    public int calculerScore() {
        int score = 0;
        int nombreAs = 0;

        for (Carte c : cartes) {
            score += c.getPoints();
            if (c.getValeur() == Carte.Valeur.AS) {
                nombreAs += 1;
            }
        }

        while (score > 21 && nombreAs > 0) {
            score -= 10;
            nombreAs -= 1;
        }
        return score;
    }

    public List<Carte> getCartes() {
        return cartes;
    }
}