package n7.fr.BlackJack.game;

import java.util.Map;

public class Croupier {

    private TableDeBlackjack table;

    public Croupier(TableDeBlackjack table) {
        this.table = table;
    }

    public void executerJeuCroupier() {
        table.jouerCroupier();
    }

    public Map<String, String> determinerGagnants() {
        return table.determinerResultats();
    }

    public void distribuerCartes() {
        table.demarrerManche();
    }

    public int getScoreCroupier() {
        return table.getMainCroupier().calculerScore();
    }

    public Main getMainCroupier() {
        return table.getMainCroupier();
    }
}
