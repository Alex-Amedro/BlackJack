package n7.fr.BlackJack.entity.controller;

import n7.fr.BlackJack.entity.Joueur;
import n7.fr.BlackJack.entity.Message;
import n7.fr.BlackJack.repository.JoueurRepository;
import n7.fr.BlackJack.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class ChatController {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private JoueurRepository joueurRepository;

    /**
     * Envoyer un message global.
     * Body: { "pseudo": "...", "contenu": "..." }
     */
    @PostMapping("/global")
    public ResponseEntity<?> sendGlobalMessage(@RequestBody Map<String, String> body) {
        String pseudo = body.get("pseudo");
        String contenu = body.get("contenu");

        if (pseudo == null || pseudo.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Pseudo requis."));
        }
        if (contenu == null || contenu.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Message vide."));
        }

        Joueur auteur = joueurRepository.findByPseudo(pseudo.trim());
        if (auteur == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Joueur introuvable."));
        }

        Message msg = new Message(contenu.trim(), auteur, "GLOBAL");
        messageRepository.save(msg);

        return ResponseEntity.ok(Map.of(
                "id", msg.getId(),
                "pseudo", auteur.getPseudo(),
                "contenu", msg.getContenu(),
                "date", msg.getDate(),
                "type", "GLOBAL"
        ));
    }

    /**
     * Récupérer les messages globaux (les 100 derniers).
     */
    @GetMapping("/global")
    public List<Map<String, Object>> getGlobalMessages() {
        List<Message> messages = messageRepository.findByTypeOrderByIdAsc("GLOBAL");

        // Garder les 100 derniers
        int start = Math.max(0, messages.size() - 100);
        List<Message> recent = messages.subList(start, messages.size());

        List<Map<String, Object>> result = new ArrayList<>();
        for (Message msg : recent) {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("id", msg.getId());
            entry.put("pseudo", msg.getAuteur().getPseudo());
            entry.put("contenu", msg.getContenu());
            entry.put("date", msg.getDate());
            entry.put("type", "GLOBAL");
            result.add(entry);
        }
        return result;
    }

    /**
     * Envoyer un message privé.
     * Body: { "fromPseudo": "...", "toPseudo": "...", "contenu": "..." }
     */
    @PostMapping("/private")
    public ResponseEntity<?> sendPrivateMessage(@RequestBody Map<String, String> body) {
        String fromPseudo = body.get("fromPseudo");
        String toPseudo = body.get("toPseudo");
        String contenu = body.get("contenu");

        if (fromPseudo == null || toPseudo == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Pseudos requis."));
        }
        if (contenu == null || contenu.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Message vide."));
        }

        Joueur from = joueurRepository.findByPseudo(fromPseudo.trim());
        Joueur to = joueurRepository.findByPseudo(toPseudo.trim());

        if (from == null || to == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Joueur introuvable."));
        }

        // Vérifier qu'ils sont amis
        boolean areFriends = from.getAmis().stream().anyMatch(a -> a.getId() == to.getId());
        if (!areFriends) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Vous devez être amis pour envoyer un message privé."));
        }

        Message msg = new Message(contenu.trim(), from, to);
        messageRepository.save(msg);

        return ResponseEntity.ok(Map.of(
                "id", msg.getId(),
                "fromPseudo", from.getPseudo(),
                "toPseudo", to.getPseudo(),
                "contenu", msg.getContenu(),
                "date", msg.getDate(),
                "type", "PRIVATE"
        ));
    }

    /**
     * Récupérer la conversation privée entre deux joueurs.
     */
    @GetMapping("/private")
    public ResponseEntity<?> getPrivateMessages(@RequestParam String from, @RequestParam String to) {
        Joueur joueur1 = joueurRepository.findByPseudo(from.trim());
        Joueur joueur2 = joueurRepository.findByPseudo(to.trim());

        if (joueur1 == null || joueur2 == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Joueur introuvable."));
        }

        List<Message> messages = messageRepository.findConversation(joueur1, joueur2);

        List<Map<String, Object>> result = new ArrayList<>();
        for (Message msg : messages) {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("id", msg.getId());
            entry.put("fromPseudo", msg.getAuteur().getPseudo());
            entry.put("toPseudo", msg.getReceveur() != null ? msg.getReceveur().getPseudo() : null);
            entry.put("contenu", msg.getContenu());
            entry.put("date", msg.getDate());
            entry.put("type", "PRIVATE");
            result.add(entry);
        }

        return ResponseEntity.ok(result);
    }
}
