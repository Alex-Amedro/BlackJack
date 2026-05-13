package n7.fr.BlackJack.entity;

import java.util.ArrayList;
import java.util.Collection;

import jakarta.persistence.*;
import n7.fr.BlackJack.game.Main;

@Entity 
public class Joueur {
    
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY) 
    private int id;
    
    @Column(unique = true)
    private String pseudo;

    private String mdp; 
    
    private int solde;

    @ManyToMany
    private Collection<Match> matchs;

    @ManyToMany
    @JoinTable(
        name = "joueur_amis",
        joinColumns = @JoinColumn(name = "joueur_id"),
        inverseJoinColumns = @JoinColumn(name = "ami_id")
    )
    private Collection<Joueur> amis;

    @Transient // game.Main is an in-memory model, not a JPA-mapped type
    private Main main;
    

    public Joueur() {
        this.solde = 1000;
        this.mdp = "";
        this.matchs = new ArrayList<>();
        this.amis = new ArrayList<>();
        this.main = new Main();
    }

    public Joueur(String pseudo) {
        this.pseudo = pseudo;
        this.solde = 1000;
        this.mdp = "";
        this.matchs = new ArrayList<>();
        this.amis = new ArrayList<>();
    }

    public int getId() { return this.id; }
    public void setId(int id) { this.id = id; }

    public int getSolde() { return this.solde; }
    public void setSolde(int solde) { this.solde = solde; }

    @Transient
    public Main getMain() { return this.main; }
    @Transient
    public void setMain(Main main) { this.main = main; }
    public void clearMain() { this.main = null; }

    public String getPseudo() { return this.pseudo; }
    public void setPseudo(String pseudo) { this.pseudo = pseudo; }

    public String getMdp() { return this.mdp; }
    public void setMdp(String mdp) { this.mdp = mdp; }
    
    public Collection<Match> getMatchs() {return this.matchs;}
    public void addMatch(Match game) {this.matchs.add(game);}

    public Collection<Joueur> getAmis() { return this.amis; }
    public void addAmi(Joueur ami) { this.amis.add(ami); }
    public void removeAmi(Joueur ami) { this.amis.remove(ami); }
}