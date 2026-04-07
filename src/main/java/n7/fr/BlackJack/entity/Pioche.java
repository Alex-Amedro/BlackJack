package n7.fr.BlackJack.entity;

import java.util.*;
import jakarta.persistence.*;

@Entity 
public class Pioche{

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY) 
    private int id;

    @OneToMany
    private List<Carte> cartes;

    @OneToOne(mappedBy = "pioche")
    private Table table;

    public Pioche() {
        this.cartes = new ArrayList<>();
    }

    public Pioche(Table table) {
        this.table = table;
        this.cartes = new ArrayList<>();
        initCartes();
    }

    private void initCartes() {
        String[] couleurs = {"Coeur", "Carreau", "Trèfle", "Pique"};
        String[] valeurs = {"2", "3", "4", "5", "6", "7", "8", "9", "10", "Valet", "Dame", "Roi", "As"};
        for (String couleur : couleurs) {
            for (String valeur : valeurs) {
                this.cartes.add(new Carte(couleur, valeur, this));
            }
        }
    }
    public boolean isEmpty() {
        return this.cartes.isEmpty();
    }
    
    public void tirerCarte(Joueur joueur) {
        if (!this.cartes.isEmpty()) {
            Carte carte = this.cartes.remove(0);
            carte.transferToMain(joueur.getMain());
        }
    }

    public void remettreCarte(Carte carte) {
        carte.transferToPioche(this);
        this.cartes.add(carte);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
}