package n7.fr.BlackJack.entity;

import java.util.ArrayList;
import java.util.Collection;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Transient;
import n7.fr.BlackJack.game.Main;

@Entity 
public class Joueur {
    
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY) 
    private int id;
    
    private String pseudo;

    private String mdp; 
    
    private int solde;

    @ManyToMany
    private Collection<Match> matchs;

    @OneToMany 
    private Collection<Invitation> invitations;

    @OneToMany
    private Collection<Message> messages;

    @Transient // game.Main is an in-memory model, not a JPA-mapped type
    private Main main;
    

    public Joueur() {
        this.solde = 1000;
        this.mdp = "";
        this.invitations = new ArrayList<>();
        this.messages = new ArrayList<>();
        this.matchs = new ArrayList<>();
        this.main = new Main();
    }

    public Joueur(String pseudo) {
        this.pseudo = pseudo;
        this.solde = 1000;
        this.mdp = "";
        this.invitations = new ArrayList<>();
        this.messages = new ArrayList<>();
        this.matchs = new ArrayList<>();
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

    public Collection<Invitation> getInvitations() {return this.invitations;}
    public void addInvitation(Invitation invit) {this.invitations.add(invit);}
    
    public Collection<Message> getMessages() {return this.messages;}
    public void addMessages(Message sms) {this.messages.add(sms);}
    
    public Collection<Match> getMatchs() {return this.matchs;}
    public void addMatch(Match game) {this.matchs.add(game);}

    public String getMdp() {
        return mdp;
    }

    public void setMdp(String mdp) {
        this.mdp = mdp;
    }
}