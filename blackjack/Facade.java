package n7.fr.BlackJack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import n7.fr.BlackJack.entity.Joueur;
import n7.fr.BlackJack.entity.Table;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"})
public class Facade {

    //Collections qui sont thread-safe
    private final Map<Integer, Joueur> joueurs;
    private final Map<Integer, Table> tables;
    private final Map<Integer, Set<Integer>> tableJoueurs;

    //Séquences qui sont thread-safe
    private final AtomicInteger joueurIdSequence;
    private final AtomicInteger tableIdSequence;

    public Facade() {
        this.joueurs = new ConcurrentHashMap<>();
        this.tables = new ConcurrentHashMap<>();
        this.tableJoueurs = new ConcurrentHashMap<>();
        this.joueurIdSequence = new AtomicInteger(1);
        this.tableIdSequence = new AtomicInteger(1);
    }

    @GetMapping("/health")
    public String health() {
        return "ok";
    }

    @PostMapping("/joueurs")
    public ResponseEntity<?> ajoutJoueur(@RequestParam("pseudo") String pseudo) {
        if (pseudo == null || pseudo.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Le pseudo est obligatoire");
        }

        Joueur nouveauJoueur = new Joueur(pseudo.trim());
        int id = this.joueurIdSequence.getAndIncrement();
        nouveauJoueur.setId(id);
        this.joueurs.put(id, nouveauJoueur);
        return ResponseEntity.status(HttpStatus.CREATED).body(nouveauJoueur);
    }

    @PostMapping("/ajoutJoueur")
    public ResponseEntity<?> ajoutJoueurLegacy(@RequestParam("joueur") String joueur) {
        return this.ajoutJoueur(joueur);
    }

    @PostMapping("/tables")
    public ResponseEntity<Table> ajoutTable() {
        Table table = new Table();
        int id = this.tableIdSequence.getAndIncrement();
        table.setId(id);
        this.tables.put(id, table);
        this.tableJoueurs.put(id, ConcurrentHashMap.newKeySet());
        return ResponseEntity.status(HttpStatus.CREATED).body(table);
    }

    @PostMapping("/ajoutTable")
    public ResponseEntity<Table> ajoutTableLegacy() {
        return this.ajoutTable();
    }

    @GetMapping("/joueurs")
    public Collection<Joueur> listerJoueurs() {
        return new ArrayList<>(this.joueurs.values());
    }

    @GetMapping("/tables")
    public Collection<Table> listerTables() {
        return new ArrayList<>(this.tables.values());
    }

    @PostMapping("/tables/{tableId}/join")
    public ResponseEntity<String> rejoindreTable(
            @PathVariable("tableId") int tableId,
            @RequestParam("joueurId") int joueurId) {

        if (!this.tables.containsKey(tableId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Table introuvable");
        }
        if (!this.joueurs.containsKey(joueurId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Joueur introuvable");
        }

        this.tableJoueurs.get(tableId).add(joueurId);
        return ResponseEntity.ok("Joueur ajoute a la table");
    }

    @PostMapping("/rejoindreTable")
    public ResponseEntity<String> rejoindreTableLegacy(
            @RequestParam("tableId") int tableId,
            @RequestParam("joueurId") int joueurId) {
        return this.rejoindreTable(tableId, joueurId);
    }

    @GetMapping("/tables/{tableId}/joueurs")
    public ResponseEntity<Collection<Joueur>> listerJoueursTable(@PathVariable("tableId") int tableId) {
        Set<Integer> currentTableJoueurs = this.tableJoueurs.get(tableId);
        if (currentTableJoueurs == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        // Functional approach (Stream) for cleaner lookup and mapping
        Collection<Joueur> membres = currentTableJoueurs.stream()
                .map(this.joueurs::get)
                .filter(joueur -> joueur != null) // Safety check if a user is randomly missing
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(membres);
    }
}