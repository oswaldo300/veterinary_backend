package com.tecnm.veterinary;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Exposes the "Authorize" button in Swagger UI: after login, paste the
// token (without the "Bearer " prefix) once and every protected endpoint
// sends it as "Authorization: Bearer <token>".
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
@SpringBootApplication
public class VeterinaryBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(VeterinaryBackendApplication.class, args);
    }
}
