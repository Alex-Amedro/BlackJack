package n7.fr.BlackJack.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Paquet {
    private List<Carte> cartes;

    public Paquet() {
        cartes = new ArrayList<>();
        initialiserPaquet();
        Collections.shuffle(cartes);
    }

    private void initialiserPaquet() {
        for (Carte.Couleur couleur : Carte.Couleur.values()) {
            for (Carte.Valeur valeur : Carte.Valeur.values()) {
                cartes.add(new Carte(couleur, valeur));
            }
        }
    }

    public Carte tirerCarte() {
        if (cartes.isEmpty()) {
            throw new IllegalStateException("Le paquet est vide !");
        }
        return cartes.remove(0); 
    }
}