package com.tecnm.veterinary.domain.repository;

import com.tecnm.veterinary.domain.ClinicService;

import java.util.List;
import java.util.Optional;

public interface ClinicServiceRepository {

    List<ClinicService> getAll();

    Optional<ClinicService> getService(Long serviceId);

    ClinicService save(ClinicService service);

    void delete(Long serviceId);
}
