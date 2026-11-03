package com.example.demo.repository;

import com.example.demo.entity.Signalement;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SignalementRepository extends JpaRepository<Signalement, Long> {

	List<Signalement> findByResolvedFalseOrderByCreatedAtDesc();
}
