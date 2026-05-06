package n7.fr.BlackJack.entity.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import n7.fr.BlackJack.game.Carte;
import n7.fr.BlackJack.game.Main;
import n7.fr.BlackJack.game.TableDeBlackjack;
import n7.fr.BlackJack.service.TableManager;

@RestController
@RequestMapping("/api/tables")
@CrossOrigin(origins = "*")
public class TableController {

    @Autowired
    private TableManager tableManager;

    @GetMapping
    public List<Map<String, Object>> listTables() {
        List<Map<String, Object>> result = new ArrayList<>();
        for (TableDeBlackjack table : tableManager.getAllTables()) {
            result.add(buildSnapshot(table));
        }
        return result;
    }

    @PostMapping
    public Map<String, Object> createTable() {
        TableDeBlackjack newTable = tableManager.createTable();
        return buildSnapshot(newTable);
    }

    @PostMapping("/{tableId}/join")
    public Map<String, Object> joinTable(@PathVariable String tableId, @RequestParam String pseudo) {
        TableDeBlackjack table = tableManager.getTable(tableId);
        if (table == null) {
            throw new RuntimeException("Table non trouvée");
        }

        table.ajouterJoueur(pseudo);
        if (table.getJoueurs().size() == 2) {
            table.demarrerManche();
        }

        return buildSnapshot(table);
    }

    @PostMapping("/{tableId}/bet")
    public Map<String, Object> placeBet(@PathVariable String tableId, @RequestParam String pseudo, @RequestParam int amount) {
        TableDeBlackjack table = tableManager.getTable(tableId);
        if (table == null) {
            throw new RuntimeException("Table non trouvée");
        }

        table.placerMise(pseudo, amount);
        return buildSnapshot(table);
    }

    @PostMapping("/{tableId}/hit")
    public Map<String, Object> playerHit(@PathVariable String tableId, @RequestParam String pseudo) {
        TableDeBlackjack table = tableManager.getTable(tableId);
        if (table == null) {
            throw new RuntimeException("Table non trouvée");
        }

        table.joueurTire(pseudo);

        if (table.tousLesJoueursTermines()) {
            table.terminerManche();
        }

        return buildSnapshot(table);
    }

    @PostMapping("/{tableId}/stand")
    public Map<String, Object> playerStand(@PathVariable String tableId, @RequestParam String pseudo) {
        TableDeBlackjack table = tableManager.getTable(tableId);
        if (table == null) {
            throw new RuntimeException("Table non trouvée");
        }

        table.joueurStand(pseudo);

        if (table.tousLesJoueursTermines()) {
            table.terminerManche();
        }

        return buildSnapshot(table);
    }

    @GetMapping("/{tableId}")
    public Map<String, Object> getTable(@PathVariable String tableId) {
        TableDeBlackjack table = tableManager.getTable(tableId);
        if (table == null) {
            throw new RuntimeException("Table non trouvée");
        }

        return buildSnapshot(table);
    }

    private Map<String, Object> buildSnapshot(TableDeBlackjack table) {
        Map<String, Object> info = new HashMap<>();
        info.put("id", table.getIdTable());
        info.put("name", "Table " + table.getIdTable());
        info.put("playerCount", table.getJoueurs().size());
        info.put("maxPlayers", 7);
        info.put("roundNumber", table.getNumeroManche());
        info.put("phase", table.estMancheTerminee() ? "results" : (table.getNumeroManche() > 0 ? "playing" : "waiting"));
        info.put("dealerCards", mapCards(table.getMainCroupier()));
        info.put("dealerScore", table.getMainCroupier().calculerScore());
        info.put("players", buildPlayers(table));

        if (table.estMancheTerminee()) {
            info.put("resultats", table.determinerResultats());
        }

        return info;
    }

    private List<String> mapCards(Main main) {
        List<String> cards = new ArrayList<>();
        for (Carte card : main.getCartes()) {
            cards.add(card.getAssetName());
        }
        return cards;
    }

    private Map<String, Object> buildPlayers(TableDeBlackjack table) {
        Map<String, Object> players = new HashMap<>();
        for (String pseudo : table.getJoueurs()) {
            Map<String, Object> player = new HashMap<>();
            Main main = table.getMainsJoueurs().get(pseudo);
            player.put("cards", main == null ? List.of() : mapCards(main));
            player.put("score", main == null ? 0 : main.calculerScore());
            player.put("bet", table.getMise(pseudo));
            player.put("balance", table.getSolde(pseudo));
            player.put("finished", table.joueurAFiniDeJouer(pseudo));
            players.put(pseudo, player);
        }

        return players;
    }
}