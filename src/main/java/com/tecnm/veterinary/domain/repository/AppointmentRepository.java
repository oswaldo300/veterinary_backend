package com.tecnm.veterinary.domain.repository;

import com.tecnm.veterinary.domain.Appointment;

import java.util.List;
import java.util.Optional;

public interface AppointmentRepository {

    List<Appointment> getAll();

    Optional<Appointment> getAppointment(Long appointmentId);

    List<Appointment> getByPet(Long petId);

    Appointment save(Appointment appointment);

    void delete(Long appointmentId);
}
