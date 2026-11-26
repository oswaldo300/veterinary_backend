package com.tecnm.veterinary.persistence.mapper;

import com.tecnm.veterinary.domain.Veterinarian;
import com.tecnm.veterinary.persistence.entity.Veterinario;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.util.List;

@Mapper(componentModel = "spring")
public interface VeterinarianMapper {

    @Mappings({
            @Mapping(source = "idVeterinario", target = "id"),
            @Mapping(source = "nombre", target = "firstName"),
            @Mapping(source = "apellidos", target = "lastName"),
            @Mapping(source = "especialidad", target = "specialty"),
            @Mapping(source = "numeroLicencia", target = "licenseNumber")
    })
    Veterinarian toVeterinarian(Veterinario veterinario);

    List<Veterinarian> toVeterinarians(List<Veterinario> veterinarios);

    @Mappings({
            @Mapping(source = "id", target = "idVeterinario"),
            @Mapping(source = "firstName", target = "nombre"),
            @Mapping(source = "lastName", target = "apellidos"),
            @Mapping(source = "specialty", target = "especialidad"),
            @Mapping(source = "licenseNumber", target = "numeroLicencia")
    })
    Veterinario toVeterinario(Veterinarian veterinarian);
}
