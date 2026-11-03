package com.example.layared.archetype;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LayeredArchetypeApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(LayeredArchetypeApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		
	}
}
