package com.tecnm.veterinary.web.controller;

import com.tecnm.veterinary.domain.Appointment;
import com.tecnm.veterinary.domain.service.AppointmentService;
import com.tecnm.veterinary.web.dto.AppointmentRequest;
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
@RequestMapping("/appointments")
@Tag(name = "Appointments", description = "Manage medical appointments (master-detail: appointment + services)")
@SecurityRequirement(name = "bearerAuth")
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    @GetMapping("")
    @Operation(summary = "Get all appointments", description = "Return a list of all appointments with their service details")
    @ApiResponse(responseCode = "200", description = "Appointments retrieved successfully")
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
    public ResponseEntity<List<Appointment>> getAll() {
        return new ResponseEntity<>(appointmentService.getAll(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get appointment by id", description = "Return an appointment by its id, including its service details")
    @ApiResponse(responseCode = "200", description = "Appointment found")
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
    @ApiResponse(responseCode = "404", description = "Appointment not found")
    public ResponseEntity<Appointment> getAppointment(
            @Parameter(description = "Id of the appointment to retrieve", example = "1", required = true)
            @PathVariable("id") Long appointmentId) {
        return appointmentService.getAppointment(appointmentId)
                .map(ResponseEntity::ok)
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/pet/{petId}")
    @Operation(summary = "Get appointments by pet", description = "Return all appointments of a specific pet")
    @ApiResponse(responseCode = "200", description = "Appointments found for the pet")
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
    @ApiResponse(responseCode = "404", description = "Pet not found")
    public ResponseEntity<List<Appointment>> getByPet(
            @Parameter(description = "Id of the pet", example = "1", required = true)
            @PathVariable("petId") Long petId) {
        return new ResponseEntity<>(appointmentService.getByPet(petId), HttpStatus.OK);
    }

    @PostMapping("")
    @Operation(summary = "Create an appointment",
            description = "Create an appointment with its services in a single transaction (master-detail). Prices are taken from the service catalog: unitPrice and subtotal are computed server-side, and the total is the sum of the subtotals.")
    @ApiResponse(responseCode = "201", description = "Appointment created")
    @ApiResponse(responseCode = "400", description = "Invalid appointment data")
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
    @ApiResponse(responseCode = "404", description = "Pet, veterinarian or service not found")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(value = "{\"petId\": 1, \"veterinarianId\": 1, \"dateTime\": \"2026-12-01T10:00:00\", \"reason\": \"Vacunación anual\", \"status\": \"SCHEDULED\", \"services\": [{\"serviceId\": 2, \"quantity\": 1}]}")))
    public ResponseEntity<Appointment> create(@Valid @RequestBody AppointmentRequest request) {
        return new ResponseEntity<>(appointmentService.create(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an appointment", description = "Update the date, reason, status, pet or veterinarian of an existing appointment. The booked services stay as they are.")
    @ApiResponse(responseCode = "200", description = "Appointment updated")
    @ApiResponse(responseCode = "400", description = "Invalid appointment data")
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
    @ApiResponse(responseCode = "404", description = "Appointment, pet or veterinarian not found")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(value = "{\"petId\": 1, \"veterinarianId\": 1, \"dateTime\": \"2026-12-01T11:00:00\", \"reason\": \"Vacunación anual\", \"status\": \"SCHEDULED\"}")))
    public ResponseEntity<Appointment> update(
            @Parameter(description = "Id of the appointment to update", example = "1", required = true)
            @PathVariable("id") Long appointmentId,
            @Valid @RequestBody Appointment appointment) {
        return appointmentService.update(appointmentId, appointment)
                .map(ResponseEntity::ok)
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an appointment", description = "Delete an appointment by its id if it exists. Its service details are deleted together with it.")
    @ApiResponse(responseCode = "204", description = "Appointment deleted")
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
    @ApiResponse(responseCode = "404", description = "Appointment not found")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Id of the appointment to delete", example = "1", required = true)
            @PathVariable("id") Long appointmentId) {
        if (appointmentService.delete(appointmentId)) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}
