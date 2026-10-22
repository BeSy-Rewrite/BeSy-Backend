package de.hs_esslingen.besy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.OAuthFlow;
import io.swagger.v3.oas.annotations.security.OAuthFlows;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

@SpringBootApplication
@EnableJpaAuditing
@EnableAsync
@SecurityScheme(name = "Keycloak", type = SecuritySchemeType.OAUTH2, bearerFormat = "JWT", scheme = "bearer", in = SecuritySchemeIn.HEADER, flows = @OAuthFlows(password = @OAuthFlow(authorizationUrl = "${spring.security.oauth2.resourceserver.jwt.issuer-uri}/protocol/openid-connect/auth", tokenUrl = "${spring.security.oauth2.resourceserver.jwt.issuer-uri}/protocol/openid-connect/token")))
@OpenAPIDefinition(info = @Info(title = "BeSy API", version = "${spring.application.version}", description = "API des Bestellsystems"))
public class BeSyApplication {

	public static void main(String[] args) {
		SpringApplication.run(BeSyApplication.class, args);
	}

}
