package n7.fr.BlackJack.controller;

import n7.fr.BlackJack.entity.Joueur;
import n7.fr.BlackJack.repository.JoueurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/joueurs")
@CrossOrigin(origins = "*")
public class JoueurController {

    @Autowired
    private JoueurRepository joueurRepository;

    @PostMapping("/inscription")
    public Joueur inscrire(@RequestBody Joueur joueur) {
        return joueurRepository.save(joueur);
    }

    @PostMapping("/login")
    public Joueur connexion(@RequestBody Joueur joueur) {
        Joueur existant = joueurRepository.findByPseudo(joueur.getPseudo());
        if (existant != null && existant.getMdp().equals(joueur.getMdp())) {
            return existant;
        }
        throw new RuntimeException("Identifiants incorrects");
    }

    @GetMapping("/{id}")
    public Joueur obtenirJoueur(@PathVariable int id) {
        return joueurRepository.findById(id).orElseThrow(() -> new RuntimeException("Joueur non trouvé"));
    }
}