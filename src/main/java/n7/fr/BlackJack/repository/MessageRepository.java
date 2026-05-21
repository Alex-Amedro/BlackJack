package n7.fr.BlackJack.repository;

import n7.fr.BlackJack.entity.Joueur;
import n7.fr.BlackJack.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Integer> {

    // Messages globaux, ordonnés par id (chronologique)
    List<Message> findByTypeOrderByIdAsc(String type);

    // Conversation privée entre deux joueurs (dans les deux sens)
    @Query("SELECT m FROM Message m WHERE m.type = 'PRIVATE' AND " +
           "((m.auteur = :joueur1 AND m.receveur = :joueur2) OR " +
           "(m.auteur = :joueur2 AND m.receveur = :joueur1)) " +
           "ORDER BY m.id ASC")
    List<Message> findConversation(@Param("joueur1") Joueur joueur1, @Param("joueur2") Joueur joueur2);
}
