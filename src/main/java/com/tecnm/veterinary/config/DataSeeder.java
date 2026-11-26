package com.tecnm.veterinary.config;

import com.tecnm.veterinary.domain.ClinicService;
import com.tecnm.veterinary.domain.Owner;
import com.tecnm.veterinary.domain.Pet;
import com.tecnm.veterinary.domain.Veterinarian;
import com.tecnm.veterinary.domain.repository.ClinicServiceRepository;
import com.tecnm.veterinary.domain.repository.OwnerRepository;
import com.tecnm.veterinary.domain.repository.PetRepository;
import com.tecnm.veterinary.domain.repository.VeterinarianRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Demo seed data. Runs only when the owners table is empty, so it never
 * duplicates data on restart. The demo owner password is "Vet1234!" — the
 * hash below was produced by BCryptPasswordEncoder (never write a hash
 * you did not generate with the encoder).
 */
@Component
@Transactional
public class DataSeeder implements CommandLineRunner {

    // BCrypt hash of "Vet1234!" (generated with BCryptPasswordEncoder).
    private static final String DEMO_OWNER_PASSWORD_HASH =
            "$2a$10$wVkCAr2TNcFdOtWEqt33RejSZTaKH2X14k40Zt9UHHBagewmeRZ2m";

    @Autowired
    private OwnerRepository ownerRepository;

    @Autowired
    private VeterinarianRepository veterinarianRepository;

    @Autowired
    private ClinicServiceRepository clinicServiceRepository;

    @Autowired
    private PetRepository petRepository;

    @Override
    public void run(String... args) {
        if (!ownerRepository.getAll().isEmpty()) {
            return;
        }

        Owner owner = new Owner();
        owner.setId("OWN-001");
        owner.setFirstName("Carlos");
        owner.setLastName("Mendoza");
        owner.setPhone("9991234567");
        owner.setEmail("carlos.mendoza@example.com");
        owner.setPassword(DEMO_OWNER_PASSWORD_HASH);
        // The hash is already a BCrypt hash: bypass the service hashing by
        // saving through the repository directly.
        ownerRepository.save(owner);

        Veterinarian veterinarian = new Veterinarian();
        veterinarian.setFirstName("Ana");
        veterinarian.setLastName("García");
        veterinarian.setSpecialty("Cirugía");
        veterinarian.setLicenseNumber("VET-1001");
        veterinarianRepository.save(veterinarian);

        createService("Consulta general", "Revisión general de salud", 350.0);
        createService("Vacunación", "Vacuna antirrábica anual", 450.0);
        createService("Desparasitación", "Desparasitación interna y externa", 250.0);
        createService("Análisis de laboratorio", "Análisis de sangre y orina", 800.0);
        createService("Cirugía menor", "Cirugía menor con anestesia local", 2500.0);

        Pet pet = new Pet();
        pet.setName("Firulais");
        pet.setSpecies("Perro");
        pet.setBreed("Labrador");
        pet.setAge(3);
        pet.setWeightKg(22.5);
        pet.setOwnerId("OWN-001");
        petRepository.save(pet);
    }

    private void createService(String name, String description, Double price) {
        ClinicService service = new ClinicService();
        service.setName(name);
        service.setDescription(description);
        service.setPrice(price);
        clinicServiceRepository.save(service);
    }
}
