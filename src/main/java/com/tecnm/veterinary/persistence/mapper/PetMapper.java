package com.tecnm.veterinary.persistence.mapper;

import com.tecnm.veterinary.domain.Pet;
import com.tecnm.veterinary.persistence.entity.Mascota;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PetMapper {

    @Mappings({
            @Mapping(source = "idMascota", target = "id"),
            @Mapping(source = "nombre", target = "name"),
            @Mapping(source = "especie", target = "species"),
            @Mapping(source = "raza", target = "breed"),
            @Mapping(source = "edad", target = "age"),
            @Mapping(source = "pesoKg", target = "weightKg"),
            @Mapping(source = "propietario.id", target = "ownerId")
    })
    Pet toPet(Mascota mascota);

    List<Pet> toPets(List<Mascota> mascotas);

    // The owner association is resolved by the repository (it must exist),
    // never built from a bare id here.
    @Mappings({
            @Mapping(source = "id", target = "idMascota"),
            @Mapping(source = "name", target = "nombre"),
            @Mapping(source = "species", target = "especie"),
            @Mapping(source = "breed", target = "raza"),
            @Mapping(source = "age", target = "edad"),
            @Mapping(source = "weightKg", target = "pesoKg"),
            @Mapping(target = "propietario", ignore = true)
    })
    Mascota toMascota(Pet pet);
}
