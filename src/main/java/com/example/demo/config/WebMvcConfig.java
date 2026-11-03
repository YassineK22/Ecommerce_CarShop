package com.example.demo.config;

import java.nio.file.Path;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

	@Value("${app.upload.dir:uploads}")
	private String uploadDir;

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		Path uploadPath = Path.of(uploadDir).toAbsolutePath().normalize();
		String uri = uploadPath.toUri().toString();
		if (!uri.endsWith("/")) {
			uri += "/";
		}
		registry.addResourceHandler("/uploads/**")
				.addResourceLocations(uri);
	}
}
