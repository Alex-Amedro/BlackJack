public class TableManager {
    private static Map<String, TableDeBlackjack> tables = new HashMap<>();
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
    
    // Lister toutes les tables
    public List<TableDeBlackjack> getAllTables() {
        return new ArrayList<>(tables.values());
    }
}