package com.tecnm.veterinary.persistence.crud;

import com.tecnm.veterinary.persistence.entity.Veterinario;
import org.springframework.data.repository.CrudRepository;

public interface VeterinarioCrudRepository extends CrudRepository<Veterinario, Long> {
}
