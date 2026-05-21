package n7.fr.BlackJack.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import n7.fr.BlackJack.game.TableDeBlackjack;

@Service
public class TableManager {
    private final Map<String, TableDeBlackjack> tables = new HashMap<>();
    private int tableCounter = 1;

    // Créer une nouvelle table
    public TableDeBlackjack createTable() {
        String tableId = "table_" + tableCounter++;
        TableDeBlackjack table = new TableDeBlackjack(tableId);
        tables.put(tableId, table);
        return table;
    }
    
    // Récupérer une table
    public TableDeBlackjack getTable(String tableId) {
        return tables.get(tableId);
    }

    // Supprimer une table (quand elle est vide)
    public void removeTable(String tableId) {
        tables.remove(tableId);
    }
    
    // Lister toutes les tables
    public List<TableDeBlackjack> getAllTables() {
        return new ArrayList<>(tables.values());
    }
}