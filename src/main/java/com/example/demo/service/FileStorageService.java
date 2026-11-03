package com.example.demo.service;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageService {

	// Seules ces extensions sont acceptées pour les photos d'annonces
	private static final List<String> EXTENSIONS_AUTORISEES = List.of(".jpg", ".jpeg", ".png", ".webp");

	@Value("${app.upload.dir:uploads}")
	private String uploadDir;

	private Path uploadRoot;

	@PostConstruct
	public void init() throws IOException {
		this.uploadRoot = Path.of(uploadDir).toAbsolutePath().normalize();
		Files.createDirectories(uploadRoot);
	}

	public List<String> storePhotos(MultipartFile[] files) throws IOException {
		List<String> paths = new ArrayList<>();
		if (files == null) {
			return paths;
		}
		for (MultipartFile file : files) {
			if (file == null || file.isEmpty()) {
				continue;
			}
			String originalName = StringUtils.cleanPath(file.getOriginalFilename());
			String extension = "";
			int dot = originalName.lastIndexOf('.');
			if (dot >= 0) {
				extension = originalName.substring(dot).toLowerCase();
			}
			// Contrôle du type : une simple extension ne suffit pas, on vérifie aussi le Content-Type
			String contentType = file.getContentType();
			if (!EXTENSIONS_AUTORISEES.contains(extension)
					|| contentType == null || !contentType.startsWith("image/")) {
				throw new IllegalArgumentException("Format d'image non supporté (jpg, jpeg, png ou webp)");
			}
			String storedName = UUID.randomUUID() + extension;
			Path target = uploadRoot.resolve(storedName);
			Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
			paths.add("/uploads/" + storedName);
		}
		return paths;
	}

	public void deletePhoto(String publicPath) throws IOException {
		if (publicPath == null || !publicPath.startsWith("/uploads/")) {
			return;
		}
		String fileName = publicPath.substring("/uploads/".length());
		Path file = uploadRoot.resolve(fileName).normalize();
		if (file.startsWith(uploadRoot)) {
			Files.deleteIfExists(file);
		}
	}
}
