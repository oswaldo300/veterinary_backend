package com.tecnm.veterinary.persistence;

import com.tecnm.veterinary.domain.Appointment;
import com.tecnm.veterinary.domain.repository.AppointmentRepository;
import com.tecnm.veterinary.persistence.crud.CitaCrudRepository;
import com.tecnm.veterinary.persistence.crud.MascotaCrudRepository;
import com.tecnm.veterinary.persistence.crud.ServicioCrudRepository;
import com.tecnm.veterinary.persistence.crud.VeterinarioCrudRepository;
import com.tecnm.veterinary.persistence.entity.Cita;
import com.tecnm.veterinary.persistence.entity.CitaDetalle;
import com.tecnm.veterinary.persistence.entity.Mascota;
import com.tecnm.veterinary.persistence.entity.Servicio;
import com.tecnm.veterinary.persistence.entity.Veterinario;
import com.tecnm.veterinary.persistence.mapper.AppointmentMapper;
import com.tecnm.veterinary.web.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class CitaRepository implements AppointmentRepository {

    @Autowired
    private CitaCrudRepository citaCrudRepository;

    @Autowired
    private MascotaCrudRepository mascotaCrudRepository;

    @Autowired
    private VeterinarioCrudRepository veterinarioCrudRepository;

    @Autowired
    private ServicioCrudRepository servicioCrudRepository;

    @Autowired
    private AppointmentMapper mapper;

    @Override
    public List<Appointment> getAll() {
        return mapper.toAppointments((List<Cita>) citaCrudRepository.findAll());
    }

    @Override
    public Optional<Appointment> getAppointment(Long appointmentId) {
        return citaCrudRepository.findById(appointmentId).map(mapper::toAppointment);
    }

    @Override
    public List<Appointment> getByPet(Long petId) {
        return mapper.toAppointments(citaCrudRepository.findByMascotaIdMascota(petId));
    }

    @Override
    public Appointment save(Appointment appointment) {
        Mascota mascota = mascotaCrudRepository.findById(appointment.getPetId())
                .orElseThrow(() -> new ResourceNotFoundException("Pet not found: " + appointment.getPetId()));
        Veterinario veterinario = veterinarioCrudRepository.findById(appointment.getVeterinarianId())
                .orElseThrow(() -> new ResourceNotFoundException("Veterinarian not found: " + appointment.getVeterinarianId()));

        Cita cita = new Cita();
        cita.setIdCita(appointment.getId());
        cita.setMascota(mascota);
        cita.setVeterinario(veterinario);
        cita.setFechaHora(appointment.getDateTime());
        cita.setMotivo(appointment.getReason());
        cita.setEstado(appointment.getStatus());
        cita.setTotal(appointment.getTotal());

        // Details are rebuilt from the domain object; cascade persists them
        // together with the appointment in one transaction, and orphanRemoval
        // deletes details removed from the list on update.
        List<CitaDetalle> detalles = new ArrayList<>();
        if (appointment.getDetails() != null) {
            for (var detail : appointment.getDetails()) {
                Servicio servicio = servicioCrudRepository.findById(detail.getServiceId())
                        .orElseThrow(() -> new ResourceNotFoundException("Service not found: " + detail.getServiceId()));
                CitaDetalle citaDetalle = new CitaDetalle();
                citaDetalle.setCita(cita);
                citaDetalle.setServicio(servicio);
                citaDetalle.setCantidad(detail.getQuantity());
                citaDetalle.setPrecioUnitario(detail.getUnitPrice());
                citaDetalle.setSubtotal(detail.getSubtotal());
                detalles.add(citaDetalle);
            }
        }
        cita.setDetalles(detalles);

        return mapper.toAppointment(citaCrudRepository.save(cita));
    }

    @Override
    public void delete(Long appointmentId) {
        citaCrudRepository.deleteById(appointmentId);
    }
}
