package n7.fr.BlackJack.service;

import n7.fr.BlackJack.game.TableDeBlackjack;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class TableManager {
    private static Map<String, TableDeBlackjack> tables = new java.util.concurrent.ConcurrentHashMap<>();
    private static int tableCounter = 1;

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

    public static TableDeBlackjack getTableStatic(String tableId) {
        return tables.get(tableId);
    }

    public static void putTableStatic(String tableId, TableDeBlackjack table) {
        tables.put(tableId, table);
    }

    // Lister toutes les tables
    public List<TableDeBlackjack> getAllTables() {
        return new ArrayList<>(tables.values());
    }
}