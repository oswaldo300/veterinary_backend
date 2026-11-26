package com.tecnm.veterinary.domain.repository;

import com.tecnm.veterinary.domain.Veterinarian;

import java.util.List;
import java.util.Optional;

public interface VeterinarianRepository {

    List<Veterinarian> getAll();

    Optional<Veterinarian> getVeterinarian(Long veterinarianId);

    Veterinarian save(Veterinarian veterinarian);

    void delete(Long veterinarianId);
}
