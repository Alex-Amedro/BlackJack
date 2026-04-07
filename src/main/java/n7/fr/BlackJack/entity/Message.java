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
    private Table table;

    public Message() {
    }

    public Message(String date, String contenu, Joueur auteur, Table table) {
        this.date = date;
        this.contenu = contenu;
        this.auteur = auteur;
        this.table = table;
    }

    public String getDate() { return date; }
    public String getContenu() { return contenu; }
    public Joueur getAuteur() { return auteur; }
    
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
}
