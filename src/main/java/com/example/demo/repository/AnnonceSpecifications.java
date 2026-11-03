package com.example.demo.repository;

import com.example.demo.dto.AnnonceSearchCriteria;
import com.example.demo.entity.Annonce;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public final class AnnonceSpecifications {

	private AnnonceSpecifications() {
	}

	public static Specification<Annonce> fromCriteria(AnnonceSearchCriteria criteria) {
		return (root, query, cb) -> {
			List<Predicate> predicates = new ArrayList<>();
			predicates.add(cb.isTrue(root.get("active")));

			if (criteria.make() != null && !criteria.make().isBlank()) {
				predicates.add(cb.equal(cb.lower(root.get("make")), criteria.make().trim().toLowerCase()));
			}
			if (criteria.minPrice() != null) {
				predicates.add(cb.greaterThanOrEqualTo(root.get("price"), criteria.minPrice()));
			}
			if (criteria.maxPrice() != null) {
				predicates.add(cb.lessThanOrEqualTo(root.get("price"), criteria.maxPrice()));
			}
			if (criteria.minYear() != null) {
				predicates.add(cb.greaterThanOrEqualTo(root.get("year"), criteria.minYear()));
			}
			if (criteria.maxYear() != null) {
				predicates.add(cb.lessThanOrEqualTo(root.get("year"), criteria.maxYear()));
			}
			if (criteria.maxMileage() != null) {
				predicates.add(cb.lessThanOrEqualTo(root.get("mileage"), criteria.maxMileage()));
			}

			return cb.and(predicates.toArray(new Predicate[0]));
		};
	}
}
