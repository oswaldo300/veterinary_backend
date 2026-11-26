package com.tecnm.veterinary.persistence.mapper;

import com.tecnm.veterinary.domain.Appointment;
import com.tecnm.veterinary.domain.AppointmentDetail;
import com.tecnm.veterinary.persistence.entity.Cita;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Manual mapper for appointments: the detail prices are computed
 * server-side in the service, so entity mapping is hand-written instead
 * of generated. Must run inside a transaction (lazy associations).
 */
@Component
public class AppointmentMapper {

    public Appointment toAppointment(Cita cita) {
        Appointment appointment = new Appointment();
        appointment.setId(cita.getIdCita());
        appointment.setPetId(cita.getMascota().getIdMascota());
        appointment.setVeterinarianId(cita.getVeterinario().getIdVeterinario());
        appointment.setDateTime(cita.getFechaHora());
        appointment.setReason(cita.getMotivo());
        appointment.setStatus(cita.getEstado());
        appointment.setTotal(cita.getTotal());
        if (cita.getDetalles() != null) {
            appointment.setDetails(cita.getDetalles().stream()
                    .map(detalle -> {
                        AppointmentDetail detail = new AppointmentDetail();
                        detail.setId(detalle.getIdDetalle());
                        detail.setServiceId(detalle.getServicio().getIdServicio());
                        detail.setServiceName(detalle.getServicio().getNombre());
                        detail.setQuantity(detalle.getCantidad());
                        detail.setUnitPrice(detalle.getPrecioUnitario());
                        detail.setSubtotal(detalle.getSubtotal());
                        return detail;
                    })
                    .toList());
        }
        return appointment;
    }

    public List<Appointment> toAppointments(List<Cita> citas) {
        return citas.stream().map(this::toAppointment).toList();
    }
}
