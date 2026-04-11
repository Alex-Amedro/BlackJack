package n7.fr.BlackJack;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.web.bind.annotation.*;

import n7.fr.BlackJack.entity.Invitation;
import n7.fr.BlackJack.entity.Joueur;
import n7.fr.BlackJack.entity.Table;

public class Facade{

    public Collection<Joueur> joueurs = null;
    public Collection<Table> tables = null;


    public Facade(){
        this.joueurs = new ArrayList<>();
        this.tables = new ArrayList<>();
    }

    @GET
    @Path("/ajoutJoueur")
    @Consumes("...")
    public void ajoutJoueur(@requestParam("joueur") String joueur){
        joueurs.add(new Joueur(joueur));
    }

    @GET
    @Path("/ajoutTable")
    @Consumes("...")
    public void ajoutTable(){
        this.tables.add(new Table());
    }

    public void rejoindreTable(Joueur user, Table table){
        if (this.tables.contains(table)){
            switch (Invitation user.getInvitations()){
                case 
            }
        } else {
            if user.getInvitations()==""{

            } else if ==""{

            }
        }
    }
    
}