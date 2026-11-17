package com.example.demo.service;

import com.example.demo.dto.ProfileForm;
import com.example.demo.dto.RegistrationForm;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

	@Autowired
	private UserRepository userRepository;
	@Autowired
	private PasswordEncoder passwordEncoder;

	@Transactional
	public User register(RegistrationForm form) {
		User user = new User();
		user.setName(form.name().trim());
		user.setEmail(form.email().trim().toLowerCase());
		user.setPassword(passwordEncoder.encode(form.password()));
		user.setRole(Role.UTILISATEUR);
		return userRepository.save(user);
	}

	public boolean isEmailTaken(String email) {
		return userRepository.existsByEmail(email.trim().toLowerCase());
	}

	public User findByEmail(String email) {
		return userRepository.findByEmail(email)
				.orElseThrow(() -> new IllegalStateException("Utilisateur connecté introuvable"));
	}

	@Transactional
	public void updateProfile(User user, ProfileForm form) {
		user.setName(form.name().trim());
		user.setEmail(form.email().trim().toLowerCase());

		if (form.newPassword() != null && !form.newPassword().isBlank()) {
			user.setPassword(passwordEncoder.encode(form.newPassword()));
		}

		userRepository.save(user);
	}

	public boolean isEmailTakenByOtherUser(String email, Long userId) {
		return userRepository.existsByEmailAndIdNot(email.trim().toLowerCase(), userId);
	}

	public boolean matchesPassword(User user, String rawPassword) {
		return passwordEncoder.matches(rawPassword, user.getPassword());
	}

	/**
	 * M5 : liste des comptes pour la page d'administration.
	 */
	public List<User> findAll() {
		return userRepository.findAllByOrderByIdAsc();
	}

	/**
	 * M5 : nombre d'utilisateurs (tableau de bord).
	 */
	public long count() {
		return userRepository.count();
	}

	/**
	 * M5 : active ou désactive un compte.
	 * Règle : un administrateur ne peut pas désactiver son propre compte.
	 * Le champ User.active est déjà relié à UserDetails.isEnabled(),
	 * donc Spring Security refuse automatiquement la connexion d'un compte désactivé.
	 */
	@Transactional
	public User setActive(Long id, boolean active, User admin) {
		User target = userRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));

		if (!active && target.getId().equals(admin.getId())) {
			throw new IllegalArgumentException("Vous ne pouvez pas désactiver votre propre compte");
		}

		target.setActive(active);
		return userRepository.save(target);
	}
}
