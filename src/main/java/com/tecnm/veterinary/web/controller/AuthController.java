package com.tecnm.veterinary.web.controller;

import com.tecnm.veterinary.domain.Owner;
import com.tecnm.veterinary.domain.service.OwnerService;
import com.tecnm.veterinary.web.dto.AuthResponse;
import com.tecnm.veterinary.web.dto.LoginRequest;
import com.tecnm.veterinary.web.security.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/auth")
@Tag(name = "Auth", description = "Authentication with JWT")
public class AuthController {

    @Autowired
    private OwnerService ownerService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    @Operation(summary = "Login with owner id and password",
            description = "Validates the credentials against the BCrypt password stored in the database and returns a JWT access token")
    @ApiResponse(responseCode = "200", description = "Login successful, JWT token returned")
    @ApiResponse(responseCode = "400", description = "Invalid login data")
    @ApiResponse(responseCode = "401", description = "Invalid credentials")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(value = "{\"clientId\": \"OWN-001\", \"password\": \"Vet1234!\"}")))
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        Optional<Owner> owner = ownerService.getOwner(request.getClientId());
        if (owner.isEmpty()
                || owner.get().getPassword() == null
                || request.getPassword() == null
                || !passwordEncoder.matches(request.getPassword(), owner.get().getPassword())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        String token = jwtUtil.generateToken(request.getClientId());
        return ResponseEntity.ok(new AuthResponse(token, request.getClientId()));
    }
}
