package n7.fr.BlackJack.controller; //fait par ia ce fichier 

import n7.fr.BlackJack.entity.Utilisateur;
import n7.fr.BlackJack.repository.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/utilisateurs")
@CrossOrigin(origins = "*")
public class UtilisateurController {

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @PostMapping("/inscription")
    public Utilisateur inscrire(@RequestBody Utilisateur utilisateur) {
        return utilisateurRepository.save(utilisateur);
    }

    @PostMapping("/login")
    public Utilisateur connexion(@RequestBody Utilisateur utilisateur) {
        Utilisateur existant = utilisateurRepository.findByPseudo(utilisateur.getPseudo());
        if (existant != null && existant.getMotDePasse().equals(utilisateur.getMotDePasse())) {
            return existant; 
        }
        throw new RuntimeException("Identifiants incorrects"); 
    }
}