package com.example.demo.config;

import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * M5 - Création du compte administrateur au démarrage.
 * L'email et le mot de passe viennent de la configuration (application.properties
 * ou variables d'environnement) : aucun mot de passe n'est codé en dur ici.
 * Le compte n'est créé que s'il n'existe pas encore.
 */
@Component
public class AdminInitializer implements CommandLineRunner {

	private static final Logger logger = LoggerFactory.getLogger(AdminInitializer.class);

	@Value("${app.admin.email:}")
	private String adminEmail;

	@Value("${app.admin.password:}")
	private String adminPassword;

	@Value("${app.admin.name:Administrateur}")
	private String adminName;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Override
	public void run(String... args) {
		if (adminEmail == null || adminEmail.isBlank() || adminPassword == null || adminPassword.isBlank()) {
			logger.warn("Aucun compte administrateur créé : APP_ADMIN_EMAIL et APP_ADMIN_PASSWORD ne sont pas définis.");
			return;
		}

		String email = adminEmail.trim().toLowerCase();
		if (userRepository.existsByEmail(email)) {
			logger.info("Compte administrateur déjà présent : {}", email);
			return;
		}

		User admin = new User();
		admin.setName(adminName);
		admin.setEmail(email);
		admin.setPassword(passwordEncoder.encode(adminPassword));
		admin.setRole(Role.ADMIN);
		admin.setActive(true);
		userRepository.save(admin);

		logger.info("Compte administrateur créé : {}", email);
	}
}
