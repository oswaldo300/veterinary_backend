package com.tecnm.veterinary.web.controller;

import com.tecnm.veterinary.domain.Owner;
import com.tecnm.veterinary.domain.service.OwnerService;
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
@RequestMapping("/owners")
@Tag(name = "Owners", description = "Manage pet owners (login identities)")
@SecurityRequirement(name = "bearerAuth")
public class OwnerController {

    @Autowired
    private OwnerService ownerService;

    @GetMapping("")
    @Operation(summary = "Get all owners", description = "Return a list of all registered owners")
    @ApiResponse(responseCode = "200", description = "Owners retrieved successfully")
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
    public ResponseEntity<List<Owner>> getAll() {
        return new ResponseEntity<>(ownerService.getAll(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get owner by id", description = "Return an owner by its id if it exists")
    @ApiResponse(responseCode = "200", description = "Owner found")
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
    @ApiResponse(responseCode = "404", description = "Owner not found")
    public ResponseEntity<Owner> getOwner(
            @Parameter(description = "Id of the owner to retrieve", example = "OWN-001", required = true)
            @PathVariable("id") String ownerId) {
        return ownerService.getOwner(ownerId)
                .map(ResponseEntity::ok)
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping("")
    @Operation(summary = "Create an owner", description = "Create a new owner. The password is hashed with BCrypt and never returned in responses.")
    @ApiResponse(responseCode = "201", description = "Owner created")
    @ApiResponse(responseCode = "400", description = "Invalid owner data")
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
    @ApiResponse(responseCode = "409", description = "An owner with the same id or email already exists")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(value = "{\"id\": \"OWN-002\", \"firstName\": \"María\", \"lastName\": \"López\", \"phone\": \"9991234567\", \"email\": \"maria.lopez@example.com\", \"password\": \"Vet1234!\"}")))
    public ResponseEntity<Owner> save(@Valid @RequestBody Owner owner) {
        return new ResponseEntity<>(ownerService.save(owner), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an owner", description = "Update an existing owner by its id. Send no password to keep the stored one.")
    @ApiResponse(responseCode = "200", description = "Owner updated")
    @ApiResponse(responseCode = "400", description = "Invalid owner data")
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
    @ApiResponse(responseCode = "404", description = "Owner not found")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(value = "{\"firstName\": \"María\", \"lastName\": \"López\", \"phone\": \"9997654321\", \"email\": \"maria.lopez@example.com\"}")))
    public ResponseEntity<Owner> update(
            @Parameter(description = "Id of the owner to update", example = "OWN-001", required = true)
            @PathVariable("id") String ownerId,
            @Valid @RequestBody Owner owner) {
        return ownerService.update(ownerId, owner)
                .map(ResponseEntity::ok)
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an owner", description = "Delete an owner by its id if it exists")
    @ApiResponse(responseCode = "204", description = "Owner deleted")
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
    @ApiResponse(responseCode = "404", description = "Owner not found")
    @ApiResponse(responseCode = "409", description = "The owner still has pets and cannot be deleted")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Id of the owner to delete", example = "OWN-001", required = true)
            @PathVariable("id") String ownerId) {
        if (ownerService.delete(ownerId)) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}
