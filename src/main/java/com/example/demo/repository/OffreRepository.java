package com.example.demo.repository;

import com.example.demo.entity.Offre;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OffreRepository extends JpaRepository<Offre, Long> {

	List<Offre> findByAnnonceIdOrderByAmountDescCreatedAtDesc(Long annonceId);

	boolean existsByAnnonceId(Long annonceId);
}
