package com.joanjpx.product_service;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;

@SpringBootApplication
public class ProductServiceApplication {

	private final Environment env;

	public ProductServiceApplication(Environment env) {
		this.env = env;
	}

	public static void main(String[] args) {
		SpringApplication.run(ProductServiceApplication.class, args);
	}

	@PostConstruct
	public void logActiveProfiles() {
		System.out.println("Active Profiles: " + String.join(", ", env.getActiveProfiles()));
	}
}
