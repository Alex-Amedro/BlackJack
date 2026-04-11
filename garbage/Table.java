package n7.fr.BlackJack.entity;

import java.util.*;
import jakarta.persistence.*;

@Entity 
public class Table {

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY) 
    private int id;

    @OneToMany(mappedBy = "table")
    private Collection<Joueur> joueurs;

    @OneToOne
    private Pioche pioche;

    @OneToMany
    private Collection<Invitation> invitations;

    @OneToMany
    private Collection<Message> messages;
    
    private Map<Joueur, Integer> scores;

    public Table() {
        this.joueurs = new ArrayList<>();
        this.invitations = new ArrayList<>();
        this.messages = new ArrayList<>();
        this.scores = new Hashtable();
    }

    public void finish() {
        Match match = new Match(this.joueurs, this.scores); 
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
}
