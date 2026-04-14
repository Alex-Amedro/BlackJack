package n7.fr.BlackJack.repository;

import n7.fr.BlackJack.entity.Joueur;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JoueurRepository extends JpaRepository<Joueur, Integer> {
    Joueur findByPseudo(String pseudo);
}

