package com.tecnm.veterinary.persistence.crud;

import com.tecnm.veterinary.persistence.entity.Mascota;
import org.springframework.data.repository.CrudRepository;

public interface MascotaCrudRepository extends CrudRepository<Mascota, Long> {
}
