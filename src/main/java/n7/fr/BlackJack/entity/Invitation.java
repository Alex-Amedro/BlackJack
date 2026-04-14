package n7.fr.BlackJack.entity;

import jakarta.persistence.*;

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

    String tableId;

    public Invitation() {   
    }

    public Invitation(Joueur expediteur, Joueur destinataire, String tableId) {
        this.expediteur = expediteur;
        this.destinataire = destinataire;
        this.tableId = tableId;
        this.status = "Attente"; 
    }
    
    public void Accepted() {
        this.status = "Accepté";
    }
    public void Refused() {
        this.status = "Refusé";
    }
    public Boolean isValid() {
        return "Attente".equals(this.status);
    }
    public void decay() {
        this.status = "Expiré";
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
}