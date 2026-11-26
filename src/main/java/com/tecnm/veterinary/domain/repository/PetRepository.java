package com.tecnm.veterinary.domain.repository;

import com.tecnm.veterinary.domain.Pet;

import java.util.List;
import java.util.Optional;

public interface PetRepository {

    List<Pet> getAll();

    Optional<Pet> getPet(Long petId);

    Pet save(Pet pet);

    void delete(Long petId);
}
