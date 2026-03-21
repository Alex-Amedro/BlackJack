import persdevance.*;

@Entity
public class Match {

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY) 
    private int id;

    @OneToMany
    private Collection<Joueur> joueurs;

    private Dictionary<Joueur, int> scores;


    public Match() {
        this.joueurs = new ArrayList<>();
        this.scores = new Dictionary<>();
    }

    public Match(Collection<Joueur> joueurs, Dictionary<Joueur, int> scores) {
        this.joueurs = joueurs;
        this.scores = scores;
    }

    public Collection<Joueur> getJoueurs() { return this.joueurs; }
    public Dictionary<Joueur, int> getScores() { return this.scores; }
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
}