package com.example.demo.repository;

import com.example.demo.entity.Favori;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FavoriRepository extends JpaRepository<Favori, Long> {

	List<Favori> findByUserIdOrderByCreatedAtDesc(Long userId);

	Optional<Favori> findByUserIdAndAnnonceId(Long userId, Long annonceId);

	boolean existsByUserIdAndAnnonceId(Long userId, Long annonceId);

	void deleteByAnnonceId(Long annonceId);
}
