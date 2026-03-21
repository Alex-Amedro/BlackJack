import jakarta.persistence.*;

@Entity 
public class Main {

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY) 
    private int id;

    @OneToOne(mappedBy = "main")
    private Joueur joueur;

    @oneToMany
    private Collection<Carte> cartes;

    public Main() {
        this.cartes = new ArrayList<>();
    }

    public Main(Joueur joueur) {
        this.joueur = joueur;
        this.cartes = new ArrayList<>();
    }

    public Joueur getJoueur() { return joueur; }
    public void addCarte(Carte carte) { this.cartes.add(carte); }
    public void clearCartes() { this.cartes.clear(); }

    public int getScore() {
        int score = 0;
        for (Carte carte : cartes) {
            String valeur = carte.getValeur();
            if (valeur.equals("As")) {
                score += 11;
            } else if (valeur.equals("Roi") || valeur.equals("Dame") || valeur.equals("Valet")) {
                score += 10;
            } else {
                score += Integer.parseInt(valeur);
            }
        }
        return score;
    }
    
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

}
