package com.example.demo.dto;

import java.math.BigDecimal;

public record AnnonceSearchCriteria(
		String make,
		BigDecimal minPrice,
		BigDecimal maxPrice,
		Integer minYear,
		Integer maxYear,
		Integer maxMileage) {
}
