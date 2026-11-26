package com.tecnm.veterinary.domain.service;

import com.tecnm.veterinary.domain.Owner;
import com.tecnm.veterinary.domain.repository.OwnerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class OwnerService {

    @Autowired
    private OwnerRepository ownerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<Owner> getAll() {
        return ownerRepository.getAll();
    }

    @Transactional(readOnly = true)
    public Optional<Owner> getOwner(String ownerId) {
        return ownerRepository.getOwner(ownerId);
    }

    @Transactional
    public Owner save(Owner owner) {
        String rawPassword = owner.getPassword();
        if (rawPassword != null && !rawPassword.isBlank()) {
            // Never store plain text: hash the password with BCrypt.
            owner.setPassword(passwordEncoder.encode(rawPassword));
        } else if (getOwner(owner.getId()).isPresent()) {
            // Update without a password in the body: keep the stored hash.
            owner.setPassword(getOwner(owner.getId()).get().getPassword());
        } else {
            // A new owner without a password could never log in.
            throw new IllegalArgumentException("Password is required for a new owner");
        }
        return ownerRepository.save(owner);
    }

    @Transactional
    public Optional<Owner> update(String ownerId, Owner owner) {
        if (getOwner(ownerId).isPresent()) {
            owner.setId(ownerId);
            return Optional.of(save(owner));
        }
        return Optional.empty();
    }

    @Transactional
    public boolean delete(String ownerId) {
        if (getOwner(ownerId).isPresent()) {
            ownerRepository.delete(ownerId);
            return true;
        }
        return false;
    }
}
