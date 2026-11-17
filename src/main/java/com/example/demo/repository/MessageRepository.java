package com.example.demo.repository;

import com.example.demo.entity.Message;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, Long> {

	List<Message> findByRecipientIdOrderBySentAtDesc(Long recipientId);

	List<Message> findBySenderIdOrderBySentAtDesc(Long senderId);

	boolean existsByAnnonceId(Long annonceId);

	/**
	 * Tableau de bord : nombre de messages non lus reçus par un utilisateur.
	 */
	long countByRecipientIdAndReadFalse(Long recipientId);

	/**
	 * Tableau de bord : les 5 derniers messages reçus, les non lus en premier.
	 */
	List<Message> findTop5ByRecipientIdOrderByReadAscSentAtDesc(Long recipientId);

	/**
	 * M5 : suppression en cascade par l'administrateur.
	 */
	void deleteByAnnonceId(Long annonceId);
}
