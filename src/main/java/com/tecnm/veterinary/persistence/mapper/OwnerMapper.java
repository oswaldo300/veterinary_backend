package com.tecnm.veterinary.persistence.mapper;

import com.tecnm.veterinary.domain.Owner;
import com.tecnm.veterinary.persistence.entity.Propietario;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OwnerMapper {

    @Mappings({
            @Mapping(source = "nombre", target = "firstName"),
            @Mapping(source = "apellidos", target = "lastName"),
            @Mapping(source = "telefono", target = "phone"),
            @Mapping(source = "correoElectronico", target = "email"),
            @Mapping(source = "contrasena", target = "password")
    })
    Owner toOwner(Propietario propietario);

    List<Owner> toOwners(List<Propietario> propietarios);

    @Mappings({
            @Mapping(source = "firstName", target = "nombre"),
            @Mapping(source = "lastName", target = "apellidos"),
            @Mapping(source = "phone", target = "telefono"),
            @Mapping(source = "email", target = "correoElectronico"),
            @Mapping(source = "password", target = "contrasena")
    })
    Propietario toPropietario(Owner owner);
}
