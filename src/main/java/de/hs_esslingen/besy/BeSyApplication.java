package de.hs_esslingen.besy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class BeSyApplication {

	public static void main(String[] args) {
		SpringApplication.run(BeSyApplication.class, args);
	}

}
