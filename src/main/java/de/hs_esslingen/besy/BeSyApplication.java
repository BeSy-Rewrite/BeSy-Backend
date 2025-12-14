package de.hs_esslingen.besy;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.OAuthFlow;
import io.swagger.v3.oas.annotations.security.OAuthFlows;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
@SecurityScheme(
		name = "Keycloak",
		type = SecuritySchemeType.OAUTH2,
		bearerFormat = "JWT",
		scheme = "bearer",
		in = SecuritySchemeIn.HEADER,
		flows = @OAuthFlows(
				password = @OAuthFlow(
						authorizationUrl = "https://auth.insy.hs-esslingen.com/realms/insy/protocol/openid-connect/auth",
						tokenUrl = "https://auth.insy.hs-esslingen.com/realms/insy/protocol/openid-connect/token"
				)
		)
)
public class BeSyApplication {

	public static void main(String[] args) {
		SpringApplication.run(BeSyApplication.class, args);
	}

}
