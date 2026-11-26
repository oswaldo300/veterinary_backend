package com.tecnm.veterinary.domain.repository;

import com.tecnm.veterinary.domain.Owner;

import java.util.List;
import java.util.Optional;

public interface OwnerRepository {

    List<Owner> getAll();

    Optional<Owner> getOwner(String ownerId);

    Owner save(Owner owner);

    void delete(String ownerId);
}
