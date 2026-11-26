package com.tecnm.veterinary.persistence.crud;

import com.tecnm.veterinary.persistence.entity.Propietario;
import org.springframework.data.repository.CrudRepository;

public interface PropietarioCrudRepository extends CrudRepository<Propietario, String> {
}
