package n7.fr.BlackJack.entity.controller;

import n7.fr.BlackJack.game.TableDeBlackjack;
import n7.fr.BlackJack.service.TableManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/tables")
@CrossOrigin(origins = "*")
public class TableController {

    @Autowired
    private TableManager tableManager;

    // List tables et joueurs
    @GetMapping
    public List<Map<String, Object>> listTables() {
        List<Map<String, Object>> result = new ArrayList<>();
        for (TableDeBlackjack table : tableManager.getAllTables()) {
            Map<String, Object> info = new HashMap<>();
            info.put("id", table.getIdTable());
            info.put("name", "Table " + table.getIdTable());
            info.put("playerCount", table.getJoueurs().size());
            info.put("maxPlayers", 7);
            result.add(info);
        }
        return result;
    }

    // genere une table
    @PostMapping
    public Map<String, Object> createTable() {
        TableDeBlackjack newTable = tableManager.createTable();
        Map<String, Object> info = new HashMap<>();
        info.put("id", newTable.getIdTable());
        info.put("name", "Table " + newTable.getIdTable());
        info.put("playerCount", 0);
        info.put("maxPlayers", 7);
        return info;
    }

    // rejoindre une table
    @PostMapping("/{tableId}/join")
    public void joinTable(@PathVariable String tableId, @RequestParam String pseudo)
    {
        TableDeBlackjack table = tableManager.getTable(tableId);
        if (table != null) {
            table.ajouterJoueur(pseudo);
            if (table.getJoueurs().size() == 2) {
                table.demarrerManche();
            }
        }
    }

    // miser
    @PostMapping("/{tableId}/bet")
    public void placeBet(@PathVariable String tableId, @RequestParam String pseudo, @RequestParam int amount)
    {
        TableDeBlackjack table = tableManager.getTable(tableId);
        if (table != null) {
            table.placerMise(pseudo, amount);
        }
    }

    // tirer une carte
    @PostMapping("/{tableId}/hit")
    public void playerHit(@PathVariable String tableId, @RequestParam String pseudo)
    {
        TableDeBlackjack table = tableManager.getTable(tableId);
        if (table != null) {
            table.joueurTire(pseudo);
        }
    }

    // check (je sais pas comment on dit c'est genre ne pas tirer de carte)
    @PostMapping("/{tableId}/stand")
    public void playerStand(@PathVariable String tableId, @RequestParam String pseudo)
    {
        TableDeBlackjack table = tableManager.getTable(tableId);
        if (table != null) {
            table.joueurStand(pseudo);
        }
    }
}