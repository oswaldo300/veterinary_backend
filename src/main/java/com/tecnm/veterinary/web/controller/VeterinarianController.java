package com.tecnm.veterinary.web.controller;

import com.tecnm.veterinary.domain.Veterinarian;
import com.tecnm.veterinary.domain.service.VeterinarianService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/veterinarians")
@Tag(name = "Veterinarians", description = "Manage veterinarians")
@SecurityRequirement(name = "bearerAuth")
public class VeterinarianController {

    @Autowired
    private VeterinarianService veterinarianService;

    @GetMapping("")
    @Operation(summary = "Get all veterinarians", description = "Return a list of all registered veterinarians")
    @ApiResponse(responseCode = "200", description = "Veterinarians retrieved successfully")
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
    public ResponseEntity<List<Veterinarian>> getAll() {
        return new ResponseEntity<>(veterinarianService.getAll(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get veterinarian by id", description = "Return a veterinarian by its id if it exists")
    @ApiResponse(responseCode = "200", description = "Veterinarian found")
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
    @ApiResponse(responseCode = "404", description = "Veterinarian not found")
    public ResponseEntity<Veterinarian> getVeterinarian(
            @Parameter(description = "Id of the veterinarian to retrieve", example = "1", required = true)
            @PathVariable("id") Long veterinarianId) {
        return veterinarianService.getVeterinarian(veterinarianId)
                .map(ResponseEntity::ok)
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping("")
    @Operation(summary = "Create a veterinarian", description = "Create a new veterinarian")
    @ApiResponse(responseCode = "201", description = "Veterinarian created")
    @ApiResponse(responseCode = "400", description = "Invalid veterinarian data")
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
    @ApiResponse(responseCode = "409", description = "A veterinarian with the same license number already exists")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(value = "{\"firstName\": \"Ana\", \"lastName\": \"García\", \"specialty\": \"Cirugía\", \"licenseNumber\": \"VET-1001\"}")))
    public ResponseEntity<Veterinarian> save(@Valid @RequestBody Veterinarian veterinarian) {
        return new ResponseEntity<>(veterinarianService.save(veterinarian), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a veterinarian", description = "Update an existing veterinarian by its id")
    @ApiResponse(responseCode = "200", description = "Veterinarian updated")
    @ApiResponse(responseCode = "400", description = "Invalid veterinarian data")
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
    @ApiResponse(responseCode = "404", description = "Veterinarian not found")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(value = "{\"firstName\": \"Ana\", \"lastName\": \"García\", \"specialty\": \"Medicina interna\", \"licenseNumber\": \"VET-1001\"}")))
    public ResponseEntity<Veterinarian> update(
            @Parameter(description = "Id of the veterinarian to update", example = "1", required = true)
            @PathVariable("id") Long veterinarianId,
            @Valid @RequestBody Veterinarian veterinarian) {
        return veterinarianService.update(veterinarianId, veterinarian)
                .map(ResponseEntity::ok)
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a veterinarian", description = "Delete a veterinarian by its id if it exists")
    @ApiResponse(responseCode = "204", description = "Veterinarian deleted")
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
    @ApiResponse(responseCode = "404", description = "Veterinarian not found")
    @ApiResponse(responseCode = "409", description = "The veterinarian still has appointments and cannot be deleted")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Id of the veterinarian to delete", example = "1", required = true)
            @PathVariable("id") Long veterinarianId) {
        if (veterinarianService.delete(veterinarianId)) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}
