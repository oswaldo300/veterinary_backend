package com.tecnm.veterinary.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

public class AppointmentRequest {

    @NotNull(message = "Pet id is required")
    private Long petId;

    @NotNull(message = "Veterinarian id is required")
    private Long veterinarianId;

    @NotNull(message = "Date and time are required")
    private LocalDateTime dateTime;

    private String reason;
    private String status;

    @NotEmpty(message = "The appointment must include at least one service")
    @Valid
    private List<AppointmentServiceItemRequest> services;

    public Long getPetId() {
        return petId;
    }

    public void setPetId(Long petId) {
        this.petId = petId;
    }

    public Long getVeterinarianId() {
        return veterinarianId;
    }

    public void setVeterinarianId(Long veterinarianId) {
        this.veterinarianId = veterinarianId;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<AppointmentServiceItemRequest> getServices() {
        return services;
    }

    public void setServices(List<AppointmentServiceItemRequest> services) {
        this.services = services;
    }
}
