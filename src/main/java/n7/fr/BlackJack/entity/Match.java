package n7.fr.BlackJack.entity;

import java.util.*;
import jakarta.persistence.*;

@Entity
public class Match {

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY) 
    private int id;

    @OneToMany
    private Collection<Joueur> joueurs;

    private Map<Joueur, Integer> scores;


    public Match() {
        this.joueurs = new ArrayList<>();
        this.scores = new HashMap<>();
    }

    public Match(Collection<Joueur> joueurs, Map<Joueur, Integer> scores) {
        this.joueurs = joueurs;
        this.scores = scores;
    }

    public Collection<Joueur> getJoueurs() { return this.joueurs; }
    public Map<Joueur, Integer> getScores() { return this.scores; }
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
}