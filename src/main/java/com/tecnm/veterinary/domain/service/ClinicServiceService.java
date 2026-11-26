package com.tecnm.veterinary.domain.service;

import com.tecnm.veterinary.domain.ClinicService;
import com.tecnm.veterinary.domain.repository.ClinicServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ClinicServiceService {

    @Autowired
    private ClinicServiceRepository clinicServiceRepository;

    @Transactional(readOnly = true)
    public List<ClinicService> getAll() {
        return clinicServiceRepository.getAll();
    }

    @Transactional(readOnly = true)
    public Optional<ClinicService> getService(Long serviceId) {
        return clinicServiceRepository.getService(serviceId);
    }

    @Transactional
    public ClinicService save(ClinicService service) {
        return clinicServiceRepository.save(service);
    }

    @Transactional
    public Optional<ClinicService> update(Long serviceId, ClinicService service) {
        if (getService(serviceId).isPresent()) {
            service.setId(serviceId);
            return Optional.of(save(service));
        }
        return Optional.empty();
    }

    @Transactional
    public boolean delete(Long serviceId) {
        if (getService(serviceId).isPresent()) {
            clinicServiceRepository.delete(serviceId);
            return true;
        }
        return false;
    }
}
