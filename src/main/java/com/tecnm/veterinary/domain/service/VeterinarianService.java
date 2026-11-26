package com.tecnm.veterinary.domain.service;

import com.tecnm.veterinary.domain.Veterinarian;
import com.tecnm.veterinary.domain.repository.VeterinarianRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class VeterinarianService {

    @Autowired
    private VeterinarianRepository veterinarianRepository;

    @Transactional(readOnly = true)
    public List<Veterinarian> getAll() {
        return veterinarianRepository.getAll();
    }

    @Transactional(readOnly = true)
    public Optional<Veterinarian> getVeterinarian(Long veterinarianId) {
        return veterinarianRepository.getVeterinarian(veterinarianId);
    }

    @Transactional
    public Veterinarian save(Veterinarian veterinarian) {
        return veterinarianRepository.save(veterinarian);
    }

    @Transactional
    public Optional<Veterinarian> update(Long veterinarianId, Veterinarian veterinarian) {
        if (getVeterinarian(veterinarianId).isPresent()) {
            veterinarian.setId(veterinarianId);
            return Optional.of(save(veterinarian));
        }
        return Optional.empty();
    }

    @Transactional
    public boolean delete(Long veterinarianId) {
        if (getVeterinarian(veterinarianId).isPresent()) {
            veterinarianRepository.delete(veterinarianId);
            return true;
        }
        return false;
    }
}
