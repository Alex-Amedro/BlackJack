import jakarta.persistence.*;

@Entity 
public class Joueur {
    
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY) 
    private int id;
    
    private String pseudo;
    private int solde;

    @OneToOne
    private Main main;

    @OneToMany
    private Table table;

    @ManyToMany
    private Collection<Match> matchs;

    @OneToMany 
    private Collection<Invitations> invitations;

    @OneToMany
    private Collection<Message> messages;
    

    public Joueur() {
        this.solde = 1000;
        this.invitations = new ArrayList<>();
        this.messages = new ArrayList<>();
        this.matchs = new ArrayList<>();
    }

    public Joueur(String pseudo) {
        this.pseudo = pseudo;
        this.solde = 1000;
        this.invitations = new ArrayList<>();
        this.messages = new ArrayList<>();
        this.matchs = new ArrayList<>();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getSolde() { return solde; }
    public void setSolde(int solde) { this.solde = solde; }
    public Main getMain() { return main; }
    public void setMain(Main main) { this.main = main; }
    public void clearMain() { this.main = null; }
    public String getPseudo() { return pseudo; }
    public void setPseudo(String pseudo) { this.pseudo = pseudo; }
}