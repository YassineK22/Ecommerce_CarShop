package com.example.demo.service;

import com.example.demo.entity.Annonce;
import com.example.demo.entity.Message;
import com.example.demo.entity.User;
import com.example.demo.repository.MessageRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MessageService {

	@Autowired
	private MessageRepository messageRepository;
	@Autowired
	private AnnonceService annonceService;

	/**
	 * Envoie un message au vendeur d'une annonce.
	 * Règles : contenu non vide, pas de message sur sa propre annonce.
	 */
	@Transactional
	public Message envoyer(Long annonceId, User sender, String contenu) {
		if (contenu == null || contenu.isBlank()) {
			throw new IllegalArgumentException("Le message ne peut pas être vide");
		}
		if (contenu.length() > 4000) {
			throw new IllegalArgumentException("Le message est trop long (4000 caractères max)");
		}

		Annonce annonce = annonceService.findById(annonceId);
		if (annonce.getSeller().getId().equals(sender.getId())) {
			throw new IllegalArgumentException("Vous ne pouvez pas contacter le vendeur de votre propre annonce");
		}

		Message message = new Message();
		message.setAnnonce(annonce);
		message.setSender(sender);
		message.setRecipient(annonce.getSeller());
		message.setContent(contenu.trim());
		return messageRepository.save(message);
	}

	public List<Message> getRecus(User user) {
		return messageRepository.findByRecipientIdOrderBySentAtDesc(user.getId());
	}

	public List<Message> getEnvoyes(User user) {
		return messageRepository.findBySenderIdOrderBySentAtDesc(user.getId());
	}

	/**
	 * Marque tous les messages reçus comme lus (bouton "Tout marquer comme lu").
	 */
	@Transactional
	public void marquerTousLus(User user) {
		List<Message> recus = getRecus(user);
		recus.forEach(message -> message.setRead(true));
		messageRepository.saveAll(recus);
	}
}
