package com.tecnm.veterinary.persistence.crud;

import com.tecnm.veterinary.persistence.entity.Servicio;
import org.springframework.data.repository.CrudRepository;

public interface ServicioCrudRepository extends CrudRepository<Servicio, Long> {
}
