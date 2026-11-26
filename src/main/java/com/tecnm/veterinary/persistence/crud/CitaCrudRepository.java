package com.tecnm.veterinary.persistence.crud;

import com.tecnm.veterinary.persistence.entity.Cita;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface CitaCrudRepository extends CrudRepository<Cita, Long> {

    List<Cita> findByMascotaIdMascota(Long petId);
}
