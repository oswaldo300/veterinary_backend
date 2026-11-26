package com.tecnm.veterinary.domain.service;

import com.tecnm.veterinary.domain.Pet;
import com.tecnm.veterinary.domain.repository.PetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class PetService {

    @Autowired
    private PetRepository petRepository;

    @Transactional(readOnly = true)
    public List<Pet> getAll() {
        return petRepository.getAll();
    }

    @Transactional(readOnly = true)
    public Optional<Pet> getPet(Long petId) {
        return petRepository.getPet(petId);
    }

    @Transactional
    public Pet save(Pet pet) {
        return petRepository.save(pet);
    }

    @Transactional
    public Optional<Pet> update(Long petId, Pet pet) {
        if (getPet(petId).isPresent()) {
            pet.setId(petId);
            return Optional.of(save(pet));
        }
        return Optional.empty();
    }

    @Transactional
    public boolean delete(Long petId) {
        if (getPet(petId).isPresent()) {
            petRepository.delete(petId);
            return true;
        }
        return false;
    }
}
