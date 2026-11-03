package com.example.demo.repository;

import com.example.demo.entity.Message;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, Long> {

	List<Message> findByRecipientIdOrderBySentAtDesc(Long recipientId);

	List<Message> findBySenderIdOrderBySentAtDesc(Long senderId);

	boolean existsByAnnonceId(Long annonceId);
}
