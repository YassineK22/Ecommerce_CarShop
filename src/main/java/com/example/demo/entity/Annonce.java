package com.example.demo.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Table(name = "annonces")
@Getter
@Setter
@NoArgsConstructor
public class Annonce {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String title;

	@Column(nullable = false, length = 4000)
	private String description;

	@Column(nullable = false)
	private String make;

	@Column(nullable = false)
	private String model;

	@Column(nullable = false)
	private Integer year;

	@Column(nullable = false)
	private Integer mileage;

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal price;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private TypeAnnonce type;

	@Column
	private LocalDateTime auctionEndDate;

	@Enumerated(EnumType.STRING)
	@Column
	private StatutEnchere statutEnchere;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "winner_id")
	private User winner;

	@Column(nullable = false)
	private boolean active = true;

	// M5 : annonce signalée par un utilisateur, elle remonte en tête de la liste d'administration
	@Column(nullable = false)
	@ColumnDefault("false")
	private boolean signalee = false;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "annonce_photos", joinColumns = @JoinColumn(name = "annonce_id"))
	@OrderColumn(name = "photo_order")
	@Column(name = "photo_path", nullable = false)
	private List<String> photos = new ArrayList<>();

	@ManyToOne(optional = false, fetch = FetchType.EAGER)
	@JoinColumn(name = "seller_id", nullable = false)
	private User seller;

	@Column(nullable = false)
	private LocalDateTime createdAt = LocalDateTime.now();

	// Verrou optimiste : deux modifications simultanées de la même annonce ne peuvent pas s'écraser
	@Version
	private Long version;
}
