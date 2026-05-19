package n7.fr.BlackJack.entity.controller;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import n7.fr.BlackJack.entity.Joueur;
import n7.fr.BlackJack.repository.JoueurRepository;
import n7.fr.BlackJack.service.AuthTokenService;

@RestController
@RequestMapping("/api/joueurs")
@CrossOrigin(origins = "*")
public class JoueurController {

    @Autowired
    private JoueurRepository joueurRepository;

    @Autowired
    private AuthTokenService authTokenService;

    /**
     * Inscription : pseudo + mdp requis, pseudo unique en base.
     * Le body JSON attendu : { "pseudo": "...", "mdp": "...", "confirmMdp": "..." }
     */
    @PostMapping("/inscription")
    public ResponseEntity<?> inscrire(@RequestBody Map<String, String> body) {
        String pseudo = body.get("pseudo");
        String mdp = body.get("mdp");
        String confirmMdp = body.get("confirmMdp");

        // Validations
        if (pseudo == null || pseudo.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Le pseudo est requis."));
        }
        if (mdp == null || mdp.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Le mot de passe est requis."));
        }
        if (confirmMdp == null || !mdp.equals(confirmMdp)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Les mots de passe ne correspondent pas."));
        }

        // Vérifier unicité du pseudo
        if (joueurRepository.existsByPseudo(pseudo.trim())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Ce pseudo est déjà utilisé."));
        }

        Joueur joueur = new Joueur(pseudo.trim());
        String hashedMap = BCrypt.hashpw(mdp, BCrypt.gensalt());
        joueur.setMdp(hashedMap);
        Joueur saved = joueurRepository.save(joueur);

        String token = authTokenService.generateToken(saved.getPseudo());
        return ResponseEntity.ok(Map.of(
                "id", saved.getId(),
                "pseudo", saved.getPseudo(),
                "solde", saved.getSolde(),
                "wsToken", token
        ));
    }

    /**
     * Connexion : pseudo + mdp.
     */
    @PostMapping("/login")
    public ResponseEntity<?> connexion(@RequestBody Map<String, String> body) {
        String pseudo = body.get("pseudo");
        String mdp = body.get("mdp");

        if (pseudo == null || pseudo.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Le pseudo est requis."));
        }
        if (mdp == null || mdp.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Le mot de passe est requis."));
        }

        Joueur existant = joueurRepository.findByPseudo(pseudo.trim());
        if (existant == null || !BCrypt.checkpw(mdp, existant.getMdp())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Pseudo ou mot de passe incorrect."));
        }

        String token = authTokenService.generateToken(existant.getPseudo());
        return ResponseEntity.ok(Map.of(
                "id", existant.getId(),
                "pseudo", existant.getPseudo(),
                "solde", existant.getSolde(),
                "wsToken", token
        ));
    }

    /**
     * Classement : top joueurs par solde décroissant.
     */
    @GetMapping("/ranking")
    public List<Map<String, Object>> getRanking() {
        List<Joueur> joueurs = joueurRepository.findAllByOrderBySoldeDesc();
        List<Map<String, Object>> ranking = new ArrayList<>();
        int rank = 1;
        for (Joueur j : joueurs) {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("rank", rank++);
            entry.put("pseudo", j.getPseudo());
            entry.put("solde", j.getSolde());
            ranking.add(entry);
        }
        return ranking;
    }

    @GetMapping("/{id}")
    public Joueur obtenirJoueur(@PathVariable int id) {
        return joueurRepository.findById(id).orElseThrow(() -> new RuntimeException("Joueur non trouvé"));
    }
}