package com.sigedoc.sistemasigedoc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "repository")
@EntityScan(basePackages = "models")
public class SistemasigedocApplication {

	public static void main(String[] args) {
		SpringApplication.run(SistemasigedocApplication.class, args);
	}

}
