package n7.fr.BlackJack.entity;

import jakarta.persistence.*;

@Entity
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String date;
    private String contenu;

    // "GLOBAL" ou "PRIVATE"
    private String type;

    @ManyToOne
    private Joueur auteur;

    @ManyToOne
    private Joueur receveur; // null pour GLOBAL

    public Message() {}

    public Message(String contenu, Joueur auteur, String type) {
        this.contenu = contenu;
        this.auteur = auteur;
        this.type = type;
        this.receveur = null;
        this.date = java.time.LocalDateTime.now().toString();
    }

    public Message(String contenu, Joueur auteur, Joueur receveur) {
        this.contenu = contenu;
        this.auteur = auteur;
        this.receveur = receveur;
        this.type = "PRIVATE";
        this.date = java.time.LocalDateTime.now().toString();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getContenu() { return contenu; }
    public void setContenu(String contenu) { this.contenu = contenu; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Joueur getAuteur() { return auteur; }
    public void setAuteur(Joueur auteur) { this.auteur = auteur; }

    public Joueur getReceveur() { return receveur; }
    public void setReceveur(Joueur receveur) { this.receveur = receveur; }
}
