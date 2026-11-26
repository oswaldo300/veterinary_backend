package com.tecnm.veterinary.web.controller;

import com.tecnm.veterinary.domain.ClinicService;
import com.tecnm.veterinary.domain.service.ClinicServiceService;
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
@RequestMapping("/services")
@Tag(name = "Services", description = "Manage clinic services (consultas, vacunas, cirugías...)")
@SecurityRequirement(name = "bearerAuth")
public class ClinicServiceController {

    @Autowired
    private ClinicServiceService clinicServiceService;

    @GetMapping("")
    @Operation(summary = "Get all services", description = "Return a list of all available clinic services")
    @ApiResponse(responseCode = "200", description = "Services retrieved successfully")
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
    public ResponseEntity<List<ClinicService>> getAll() {
        return new ResponseEntity<>(clinicServiceService.getAll(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get service by id", description = "Return a service by its id if it exists")
    @ApiResponse(responseCode = "200", description = "Service found")
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
    @ApiResponse(responseCode = "404", description = "Service not found")
    public ResponseEntity<ClinicService> getService(
            @Parameter(description = "Id of the service to retrieve", example = "1", required = true)
            @PathVariable("id") Long serviceId) {
        return clinicServiceService.getService(serviceId)
                .map(ResponseEntity::ok)
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping("")
    @Operation(summary = "Create a service", description = "Create a new clinic service")
    @ApiResponse(responseCode = "201", description = "Service created")
    @ApiResponse(responseCode = "400", description = "Invalid service data")
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(value = "{\"name\": \"Vacunación\", \"description\": \"Vacuna antirrábica anual\", \"price\": 450.0}")))
    public ResponseEntity<ClinicService> save(@Valid @RequestBody ClinicService service) {
        return new ResponseEntity<>(clinicServiceService.save(service), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a service", description = "Update an existing service by its id")
    @ApiResponse(responseCode = "200", description = "Service updated")
    @ApiResponse(responseCode = "400", description = "Invalid service data")
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
    @ApiResponse(responseCode = "404", description = "Service not found")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(value = "{\"name\": \"Vacunación\", \"description\": \"Vacuna antirrábica anual\", \"price\": 500.0}")))
    public ResponseEntity<ClinicService> update(
            @Parameter(description = "Id of the service to update", example = "1", required = true)
            @PathVariable("id") Long serviceId,
            @Valid @RequestBody ClinicService service) {
        return clinicServiceService.update(serviceId, service)
                .map(ResponseEntity::ok)
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a service", description = "Delete a service by its id if it exists")
    @ApiResponse(responseCode = "204", description = "Service deleted")
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
    @ApiResponse(responseCode = "404", description = "Service not found")
    @ApiResponse(responseCode = "409", description = "The service is used in an appointment and cannot be deleted")
    public ResponseEntity<Void> delete(@PathVariable("id") Long serviceId) {
        if (clinicServiceService.delete(serviceId)) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}
