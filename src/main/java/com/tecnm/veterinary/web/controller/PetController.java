package com.tecnm.veterinary.web.controller;

import com.tecnm.veterinary.domain.Pet;
import com.tecnm.veterinary.domain.service.PetService;
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
@RequestMapping("/pets")
@Tag(name = "Pets", description = "Manage pets (mascotas)")
@SecurityRequirement(name = "bearerAuth")
public class PetController {

    @Autowired
    private PetService petService;

    @GetMapping("")
    @Operation(summary = "Get all pets", description = "Return a list of all registered pets")
    @ApiResponse(responseCode = "200", description = "Pets retrieved successfully")
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
    public ResponseEntity<List<Pet>> getAll() {
        return new ResponseEntity<>(petService.getAll(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get pet by id", description = "Return a pet by its id if it exists")
    @ApiResponse(responseCode = "200", description = "Pet found")
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
    @ApiResponse(responseCode = "404", description = "Pet not found")
    public ResponseEntity<Pet> getPet(
            @Parameter(description = "Id of the pet to retrieve", example = "1", required = true)
            @PathVariable("id") Long petId) {
        return petService.getPet(petId)
                .map(ResponseEntity::ok)
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping("")
    @Operation(summary = "Create a pet", description = "Create a new pet. The owner must already exist.")
    @ApiResponse(responseCode = "201", description = "Pet created")
    @ApiResponse(responseCode = "400", description = "Invalid pet data")
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
    @ApiResponse(responseCode = "404", description = "Owner not found")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(value = "{\"name\": \"Firulais\", \"species\": \"Perro\", \"breed\": \"Labrador\", \"age\": 3, \"weightKg\": 22.5, \"ownerId\": \"OWN-001\"}")))
    public ResponseEntity<Pet> save(@Valid @RequestBody Pet pet) {
        return new ResponseEntity<>(petService.save(pet), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a pet", description = "Update an existing pet by its id")
    @ApiResponse(responseCode = "200", description = "Pet updated")
    @ApiResponse(responseCode = "400", description = "Invalid pet data")
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
    @ApiResponse(responseCode = "404", description = "Pet or owner not found")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(value = "{\"name\": \"Firulais\", \"species\": \"Perro\", \"breed\": \"Labrador\", \"age\": 4, \"weightKg\": 24.0, \"ownerId\": \"OWN-001\"}")))
    public ResponseEntity<Pet> update(
            @Parameter(description = "Id of the pet to update", example = "1", required = true)
            @PathVariable("id") Long petId,
            @Valid @RequestBody Pet pet) {
        return petService.update(petId, pet)
                .map(ResponseEntity::ok)
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a pet", description = "Delete a pet by its id if it exists")
    @ApiResponse(responseCode = "204", description = "Pet deleted")
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
    @ApiResponse(responseCode = "404", description = "Pet not found")
    @ApiResponse(responseCode = "409", description = "The pet still has appointments and cannot be deleted")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Id of the pet to delete", example = "1", required = true)
            @PathVariable("id") Long petId) {
        if (petService.delete(petId)) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}
