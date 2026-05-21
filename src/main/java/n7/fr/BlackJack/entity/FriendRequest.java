package n7.fr.BlackJack.entity;

import jakarta.persistence.*;

@Entity
public class FriendRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    private Joueur expediteur;

    @ManyToOne
    private Joueur destinataire;

    // PENDING, ACCEPTED, REJECTED
    private String status;

    private String dateEnvoi;

    public FriendRequest() {}

    public FriendRequest(Joueur expediteur, Joueur destinataire) {
        this.expediteur = expediteur;
        this.destinataire = destinataire;
        this.status = "PENDING";
        this.dateEnvoi = java.time.LocalDateTime.now().toString();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Joueur getExpediteur() { return expediteur; }
    public void setExpediteur(Joueur expediteur) { this.expediteur = expediteur; }

    public Joueur getDestinataire() { return destinataire; }
    public void setDestinataire(Joueur destinataire) { this.destinataire = destinataire; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getDateEnvoi() { return dateEnvoi; }
    public void setDateEnvoi(String dateEnvoi) { this.dateEnvoi = dateEnvoi; }
}
