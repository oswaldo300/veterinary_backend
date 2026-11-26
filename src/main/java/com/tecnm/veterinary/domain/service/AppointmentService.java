package com.tecnm.veterinary.domain.service;

import com.tecnm.veterinary.domain.Appointment;
import com.tecnm.veterinary.domain.AppointmentDetail;
import com.tecnm.veterinary.domain.ClinicService;
import com.tecnm.veterinary.domain.repository.AppointmentRepository;
import com.tecnm.veterinary.domain.repository.ClinicServiceRepository;
import com.tecnm.veterinary.domain.repository.PetRepository;
import com.tecnm.veterinary.domain.repository.VeterinarianRepository;
import com.tecnm.veterinary.web.dto.AppointmentRequest;
import com.tecnm.veterinary.web.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class AppointmentService {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private PetRepository petRepository;

    @Autowired
    private VeterinarianRepository veterinarianRepository;

    @Autowired
    private ClinicServiceRepository clinicServiceRepository;

    // Reads touch lazy associations (pet, veterinarian, details, service),
    // so they must run inside a transaction with open-in-view=false.
    @Transactional(readOnly = true)
    public List<Appointment> getAll() {
        return appointmentRepository.getAll();
    }

    @Transactional(readOnly = true)
    public Optional<Appointment> getAppointment(Long appointmentId) {
        return appointmentRepository.getAppointment(appointmentId);
    }

    @Transactional(readOnly = true)
    public List<Appointment> getByPet(Long petId) {
        petRepository.getPet(petId)
                .orElseThrow(() -> new ResourceNotFoundException("Pet not found: " + petId));
        return appointmentRepository.getByPet(petId);
    }

    // Master-detail in ONE transaction: the appointment and all its details
    // are persisted together by cascade. Prices are always taken from the
    // service catalog — client-sent totals are never trusted.
    @Transactional
    public Appointment create(AppointmentRequest request) {
        petRepository.getPet(request.getPetId())
                .orElseThrow(() -> new ResourceNotFoundException("Pet not found: " + request.getPetId()));
        veterinarianRepository.getVeterinarian(request.getVeterinarianId())
                .orElseThrow(() -> new ResourceNotFoundException("Veterinarian not found: " + request.getVeterinarianId()));

        List<AppointmentDetail> details = new ArrayList<>();
        double total = 0.0;
        for (var item : request.getServices()) {
            ClinicService service = clinicServiceRepository.getService(item.getServiceId())
                    .orElseThrow(() -> new ResourceNotFoundException("Service not found: " + item.getServiceId()));
            AppointmentDetail detail = new AppointmentDetail();
            detail.setServiceId(service.getId());
            detail.setServiceName(service.getName());
            detail.setQuantity(item.getQuantity());
            detail.setUnitPrice(service.getPrice());
            detail.setSubtotal(service.getPrice() * item.getQuantity());
            details.add(detail);
            total += detail.getSubtotal();
        }

        Appointment appointment = new Appointment();
        appointment.setPetId(request.getPetId());
        appointment.setVeterinarianId(request.getVeterinarianId());
        appointment.setDateTime(request.getDateTime());
        appointment.setReason(request.getReason());
        appointment.setStatus(request.getStatus() != null ? request.getStatus() : "SCHEDULED");
        appointment.setTotal(total);
        appointment.setDetails(details);

        return appointmentRepository.save(appointment);
    }

    @Transactional
    public Optional<Appointment> update(Long appointmentId, Appointment appointment) {
        Optional<Appointment> existing = getAppointment(appointmentId);
        if (existing.isEmpty()) {
            return Optional.empty();
        }
        Appointment current = existing.get();
        // Only scalar fields change here; the booked services stay as they are.
        current.setPetId(appointment.getPetId());
        current.setVeterinarianId(appointment.getVeterinarianId());
        current.setDateTime(appointment.getDateTime());
        current.setReason(appointment.getReason());
        if (appointment.getStatus() != null) {
            current.setStatus(appointment.getStatus());
        }
        return Optional.of(appointmentRepository.save(current));
    }

    @Transactional
    public boolean delete(Long appointmentId) {
        if (getAppointment(appointmentId).isPresent()) {
            appointmentRepository.delete(appointmentId);
            return true;
        }
        return false;
    }
}
