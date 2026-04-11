package n7.fr.BlackJack.entity;

import jakarta.persistence.*;

@Entity
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String date;
    private String contenu;

    @ManyToOne
    private Joueur auteur;
    @ManyToOne
    private Joueur receveur;

    public Message() {
    }

    public Message(String date, String contenu, Joueur auteur, Joueur receveur) {
        this.date = date;
        this.contenu = contenu;
        this.auteur = auteur;
        this.receveur = receveur;
    }

    public String getDate() { return date; }
    public String getContenu() { return contenu; }
    public Joueur getAuteur() { return auteur; }
    
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
}
