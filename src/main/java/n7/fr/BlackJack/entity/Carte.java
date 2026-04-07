package n7.fr.BlackJack.entity;

import jakarta.persistence.*;


@Entity 
public class Carte {

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY) 
    private int id;

    private String couleur;
    private String valeur;
    private boolean isPioche;

    @OneToOne(mappedBy = "cartes")
    private Pioche pioche;

    @OneToOne(mappedBy = "cartes")
    private Main main;

    public Carte() {

    }

    public Carte(String couleur, String valeur, Pioche pioche) {
        this.couleur = couleur;
        this.valeur = valeur;
        this.isPioche = true;
    }
    
    public void transferToMain(Main main) {
        this.isPioche = false;
        this.pioche = null;
        this.main = main;
    }

    public void transferToPioche(Pioche pioche) {
        this.isPioche = true;
        this.main = null;
        this.pioche = pioche;
    }

    public String getCouleur() { return couleur; }
    public String getValeur() { return valeur; }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
}
