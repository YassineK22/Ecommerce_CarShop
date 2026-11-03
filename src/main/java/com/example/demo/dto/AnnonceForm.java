package com.example.demo.dto;

import com.example.demo.entity.TypeAnnonce;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

public record AnnonceForm(
		@NotBlank(message = "Le titre est obligatoire")
		@Size(max = 200, message = "Le titre ne peut pas dépasser 200 caractères")
		String title,
		@NotBlank(message = "La description est obligatoire")
		@Size(max = 4000, message = "La description est trop longue")
		String description,
		@NotBlank(message = "La marque est obligatoire")
		String make,
		@NotBlank(message = "Le modèle est obligatoire")
		String model,
		@NotNull(message = "L'année est obligatoire")
		@Min(value = 1900, message = "Année invalide")
		@Max(value = 2100, message = "Année invalide")
		Integer year,
		@NotNull(message = "Le kilométrage est obligatoire")
		@Min(value = 0, message = "Le kilométrage doit être positif")
		Integer mileage,
		@NotNull(message = "Le prix est obligatoire")
		@DecimalMin(value = "0.0", inclusive = false, message = "Le prix doit être supérieur à 0")
		BigDecimal price,
		@NotNull(message = "Le type d'annonce est obligatoire")
		TypeAnnonce type,
		@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
		LocalDateTime auctionEndDate,
		MultipartFile[] photos) {
}
