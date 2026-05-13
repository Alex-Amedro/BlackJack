package n7.fr.BlackJack.repository;

import n7.fr.BlackJack.entity.FriendRequest;
import n7.fr.BlackJack.entity.Joueur;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FriendRequestRepository extends JpaRepository<FriendRequest, Integer> {
    List<FriendRequest> findByDestinataire(Joueur destinataire);
    List<FriendRequest> findByExpediteur(Joueur expediteur);
    List<FriendRequest> findByDestinataire_PseudoAndStatus(String pseudo, String status);
    FriendRequest findByExpediteurAndDestinataire(Joueur expediteur, Joueur destinataire);
    boolean existsByExpediteurAndDestinataire(Joueur expediteur, Joueur destinataire);
}
