package com.tecnm.veterinary.persistence;

import com.tecnm.veterinary.domain.Pet;
import com.tecnm.veterinary.domain.repository.PetRepository;
import com.tecnm.veterinary.persistence.crud.MascotaCrudRepository;
import com.tecnm.veterinary.persistence.crud.PropietarioCrudRepository;
import com.tecnm.veterinary.persistence.entity.Mascota;
import com.tecnm.veterinary.persistence.entity.Propietario;
import com.tecnm.veterinary.persistence.mapper.PetMapper;
import com.tecnm.veterinary.web.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class MascotaRepository implements PetRepository {

    @Autowired
    private MascotaCrudRepository mascotaCrudRepository;

    @Autowired
    private PropietarioCrudRepository propietarioCrudRepository;

    @Autowired
    private PetMapper mapper;

    @Override
    public List<Pet> getAll() {
        return mapper.toPets((List<Mascota>) mascotaCrudRepository.findAll());
    }

    @Override
    public Optional<Pet> getPet(Long petId) {
        return mascotaCrudRepository.findById(petId).map(mapper::toPet);
    }

    @Override
    public Pet save(Pet pet) {
        // A pet cannot exist without an owner: resolve the owner first so a
        // bad ownerId fails with 404 instead of a database error.
        Propietario propietario = propietarioCrudRepository.findById(pet.getOwnerId())
                .orElseThrow(() -> new ResourceNotFoundException("Owner not found: " + pet.getOwnerId()));
        Mascota mascota = mapper.toMascota(pet);
        mascota.setPropietario(propietario);
        return mapper.toPet(mascotaCrudRepository.save(mascota));
    }

    @Override
    public void delete(Long petId) {
        mascotaCrudRepository.deleteById(petId);
    }
}
