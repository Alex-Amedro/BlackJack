package n7.fr.BlackJack.entity.controller;

import n7.fr.BlackJack.entity.FriendRequest;
import n7.fr.BlackJack.entity.Joueur;
import n7.fr.BlackJack.repository.FriendRequestRepository;
import n7.fr.BlackJack.repository.JoueurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/friends")
@CrossOrigin(origins = "*")
public class FriendController {

    @Autowired
    private FriendRequestRepository friendRequestRepository;

    @Autowired
    private JoueurRepository joueurRepository;

    /**
     * Envoyer une demande d'ami.
     * Body: { "fromPseudo": "...", "toPseudo": "..." }
     */
    @PostMapping("/request")
    public ResponseEntity<?> sendFriendRequest(@RequestBody Map<String, String> body) {
        String fromPseudo = body.get("fromPseudo");
        String toPseudo = body.get("toPseudo");

        if (fromPseudo == null || toPseudo == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Pseudos requis."));
        }

        if (fromPseudo.equals(toPseudo)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Vous ne pouvez pas vous ajouter vous-même."));
        }

        Joueur from = joueurRepository.findByPseudo(fromPseudo.trim());
        Joueur to = joueurRepository.findByPseudo(toPseudo.trim());

        if (from == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Votre compte est introuvable."));
        }
        if (to == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Joueur '" + toPseudo.trim() + "' introuvable."));
        }

        // Vérifier s'ils sont déjà amis
        if (from.getAmis().stream().anyMatch(a -> a.getId() == to.getId())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Vous êtes déjà amis."));
        }

        // Vérifier si une demande existe déjà (dans les deux sens)
        boolean exists = friendRequestRepository.existsByExpediteurAndDestinataire(from, to)
                      || friendRequestRepository.existsByExpediteurAndDestinataire(to, from);
        if (exists) {
            return ResponseEntity.badRequest().body(Map.of("error", "Une demande d'ami existe déjà."));
        }

        FriendRequest request = new FriendRequest(from, to);
        friendRequestRepository.save(request);

        return ResponseEntity.ok(Map.of("message", "Demande d'ami envoyée à " + toPseudo + "."));
    }

    /**
     * Accepter une demande d'ami.
     */
    @PostMapping("/accept/{requestId}")
    public ResponseEntity<?> acceptFriendRequest(@PathVariable int requestId) {
        Optional<FriendRequest> opt = friendRequestRepository.findById(requestId);
        if (opt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Demande introuvable."));
        }

        FriendRequest request = opt.get();
        if (!"PENDING".equals(request.getStatus())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Cette demande a déjà été traitée."));
        }

        request.setStatus("ACCEPTED");
        friendRequestRepository.save(request);

        // Ajouter l'amitié dans les deux sens
        Joueur from = request.getExpediteur();
        Joueur to = request.getDestinataire();
        from.addAmi(to);
        to.addAmi(from);
        joueurRepository.save(from);
        joueurRepository.save(to);

        return ResponseEntity.ok(Map.of("message", "Demande acceptée. Vous êtes maintenant amis !"));
    }

    /**
     * Refuser une demande d'ami.
     */
    @PostMapping("/reject/{requestId}")
    public ResponseEntity<?> rejectFriendRequest(@PathVariable int requestId) {
        Optional<FriendRequest> opt = friendRequestRepository.findById(requestId);
        if (opt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Demande introuvable."));
        }

        FriendRequest request = opt.get();
        if (!"PENDING".equals(request.getStatus())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Cette demande a déjà été traitée."));
        }

        request.setStatus("REJECTED");
        friendRequestRepository.save(request);

        return ResponseEntity.ok(Map.of("message", "Demande refusée."));
    }

    /**
     * Liste des demandes en attente pour un joueur.
     */
    @GetMapping("/pending/{pseudo}")
    public ResponseEntity<?> getPendingRequests(@PathVariable String pseudo) {
        List<FriendRequest> pending = friendRequestRepository.findByDestinataire_PseudoAndStatus(pseudo, "PENDING");

        List<Map<String, Object>> result = new ArrayList<>();
        for (FriendRequest req : pending) {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("id", req.getId());
            entry.put("fromPseudo", req.getExpediteur().getPseudo());
            entry.put("date", req.getDateEnvoi());
            result.add(entry);
        }

        return ResponseEntity.ok(result);
    }

    /**
     * Liste des amis d'un joueur.
     */
    @GetMapping("/list/{pseudo}")
    public ResponseEntity<?> getFriends(@PathVariable String pseudo) {
        Joueur joueur = joueurRepository.findByPseudo(pseudo);
        if (joueur == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Joueur introuvable."));
        }

        List<Map<String, Object>> friends = new ArrayList<>();
        for (Joueur ami : joueur.getAmis()) {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("id", ami.getId());
            entry.put("pseudo", ami.getPseudo());
            entry.put("solde", ami.getSolde());
            friends.add(entry);
        }

        return ResponseEntity.ok(friends);
    }
}
