package com.tecnm.veterinary.domain;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

public class Appointment {

    private Long id;

    @NotNull(message = "Pet id is required")
    private Long petId;

    @NotNull(message = "Veterinarian id is required")
    private Long veterinarianId;

    @NotNull(message = "Date and time are required")
    private LocalDateTime dateTime;

    private String reason;
    private String status;
    private Double total;
    private List<AppointmentDetail> details;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public Double getTotal() {
        return total;
    }

    public void setTotal(Double total) {
        this.total = total;
    }

    public List<AppointmentDetail> getDetails() {
        return details;
    }

    public void setDetails(List<AppointmentDetail> details) {
        this.details = details;
    }
}
