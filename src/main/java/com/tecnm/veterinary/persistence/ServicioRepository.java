package com.tecnm.veterinary.persistence;

import com.tecnm.veterinary.domain.ClinicService;
import com.tecnm.veterinary.domain.repository.ClinicServiceRepository;
import com.tecnm.veterinary.persistence.crud.ServicioCrudRepository;
import com.tecnm.veterinary.persistence.entity.Servicio;
import com.tecnm.veterinary.persistence.mapper.ClinicServiceMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ServicioRepository implements ClinicServiceRepository {

    @Autowired
    private ServicioCrudRepository servicioCrudRepository;

    @Autowired
    private ClinicServiceMapper mapper;

    @Override
    public List<ClinicService> getAll() {
        return mapper.toClinicServices((List<Servicio>) servicioCrudRepository.findAll());
    }

    @Override
    public Optional<ClinicService> getService(Long serviceId) {
        return servicioCrudRepository.findById(serviceId).map(mapper::toClinicService);
    }

    @Override
    public ClinicService save(ClinicService service) {
        return mapper.toClinicService(servicioCrudRepository.save(mapper.toServicio(service)));
    }

    @Override
    public void delete(Long serviceId) {
        servicioCrudRepository.deleteById(serviceId);
    }
}
