package n7.fr.BlackJack.repository;

import n7.fr.BlackJack.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, Integer> {

}

