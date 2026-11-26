package com.tecnm.veterinary.persistence;

import com.tecnm.veterinary.domain.Veterinarian;
import com.tecnm.veterinary.domain.repository.VeterinarianRepository;
import com.tecnm.veterinary.persistence.crud.VeterinarioCrudRepository;
import com.tecnm.veterinary.persistence.entity.Veterinario;
import com.tecnm.veterinary.persistence.mapper.VeterinarianMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class VeterinarioRepository implements VeterinarianRepository {

    @Autowired
    private VeterinarioCrudRepository veterinarioCrudRepository;

    @Autowired
    private VeterinarianMapper mapper;

    @Override
    public List<Veterinarian> getAll() {
        return mapper.toVeterinarians((List<Veterinario>) veterinarioCrudRepository.findAll());
    }

    @Override
    public Optional<Veterinarian> getVeterinarian(Long veterinarianId) {
        return veterinarioCrudRepository.findById(veterinarianId).map(mapper::toVeterinarian);
    }

    @Override
    public Veterinarian save(Veterinarian veterinarian) {
        return mapper.toVeterinarian(veterinarioCrudRepository.save(mapper.toVeterinario(veterinarian)));
    }

    @Override
    public void delete(Long veterinarianId) {
        veterinarioCrudRepository.deleteById(veterinarianId);
    }
}
