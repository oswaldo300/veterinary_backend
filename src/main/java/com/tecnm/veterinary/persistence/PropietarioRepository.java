package com.tecnm.veterinary.persistence;

import com.tecnm.veterinary.domain.Owner;
import com.tecnm.veterinary.domain.repository.OwnerRepository;
import com.tecnm.veterinary.persistence.crud.PropietarioCrudRepository;
import com.tecnm.veterinary.persistence.entity.Propietario;
import com.tecnm.veterinary.persistence.mapper.OwnerMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class PropietarioRepository implements OwnerRepository {

    @Autowired
    private PropietarioCrudRepository propietarioCrudRepository;

    @Autowired
    private OwnerMapper mapper;

    @Override
    public List<Owner> getAll() {
        return mapper.toOwners((List<Propietario>) propietarioCrudRepository.findAll());
    }

    @Override
    public Optional<Owner> getOwner(String ownerId) {
        return propietarioCrudRepository.findById(ownerId).map(mapper::toOwner);
    }

    @Override
    public Owner save(Owner owner) {
        return mapper.toOwner(propietarioCrudRepository.save(mapper.toPropietario(owner)));
    }

    @Override
    public void delete(String ownerId) {
        propietarioCrudRepository.deleteById(ownerId);
    }
}
