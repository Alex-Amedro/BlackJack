package n7.fr.BlackJack.entity;

import java.util.ArrayList;
import java.util.Collection;

import jakarta.persistence.*;

@Entity 
public class Joueur {
    
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY) 
    private int id;
    
    private String pseudo;
    private int solde;

    @OneToOne
    private Main main;

    @OneToMany
    private Table table;

    @ManyToMany
    private Collection<Match> matchs;

    @OneToMany 
    private Collection<Invitation> invitations;

    @OneToMany
    private Collection<Message> messages;
    

    public Joueur() {
        this.solde = 1000;
        this.invitations = new ArrayList<>();
        this.messages = new ArrayList<>();
        this.matchs = new ArrayList<>();
        this.main = new Main();
    }

    public Joueur(String pseudo) {
        this.pseudo = pseudo;
        this.solde = 1000;
        this.invitations = new ArrayList<>();
        this.messages = new ArrayList<>();
        this.matchs = new ArrayList<>();
    }

    public int getId() { return this.id; }
    public void setId(int id) { this.id = id; }

    public int getSolde() { return this.solde; }
    public void setSolde(int solde) { this.solde = solde; }

    public Main getMain() { return this.main; }
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
}