package n7.fr.BlackJack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import n7.fr.BlackJack.entity.Joueur;
import n7.fr.BlackJack.entity.Table;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"})
public class Facade {

    private final Map<Integer, Joueur> joueurs;
    private final Map<Integer, Table> tables;
    private final Map<Integer, Set<Integer>> tableJoueurs;
    private final AtomicInteger joueurIdSequence;
    private final AtomicInteger tableIdSequence;

    public Facade() {
        this.joueurs = new LinkedHashMap<>();
        this.tables = new LinkedHashMap<>();
        this.tableJoueurs = new LinkedHashMap<>();
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
        this.tableJoueurs.put(id, new LinkedHashSet<>());
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
        if (!this.tables.containsKey(tableId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Collection<Joueur> membres = new ArrayList<>();
        for (Integer joueurId : this.tableJoueurs.get(tableId)) {
            Joueur joueur = this.joueurs.get(joueurId);
            if (joueur != null) {
                membres.add(joueur);
            }
        }
        return ResponseEntity.ok(membres);
    }
}