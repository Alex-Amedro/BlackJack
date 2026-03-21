import persistence.*;

@Entity
public class Invitation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String status; 
    @ManyToOne
    private Joueur expediteur;

    @ManyToOne
    private Joueur destinataire;

    @ManyToOne
    private Table table;

    public Invitation() {   
    }

    public Invitation(Joueur expediteur, Joueur destinataire, Table table) {
        this.expediteur = expediteur;
        this.destinataire = destinataire;
        this.table = table;
        this.status = "Attente"; 
    }
    
    public void Accepted() {
        this.status = "Accepté";
    }
    public void Refused() {
        this.status = "Refusé";
    }
    public bool isValid() {
        return "Attente".equals(this.status);
    }
    public void decay() {
        this.status = "Expiré";
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
}