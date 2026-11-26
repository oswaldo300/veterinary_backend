package com.tecnm.veterinary.persistence.mapper;

import com.tecnm.veterinary.domain.ClinicService;
import com.tecnm.veterinary.persistence.entity.Servicio;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ClinicServiceMapper {

    @Mappings({
            @Mapping(source = "idServicio", target = "id"),
            @Mapping(source = "nombre", target = "name"),
            @Mapping(source = "descripcion", target = "description"),
            @Mapping(source = "precio", target = "price")
    })
    ClinicService toClinicService(Servicio servicio);

    List<ClinicService> toClinicServices(List<Servicio> servicios);

    @Mappings({
            @Mapping(source = "id", target = "idServicio"),
            @Mapping(source = "name", target = "nombre"),
            @Mapping(source = "description", target = "descripcion"),
            @Mapping(source = "price", target = "precio")
    })
    Servicio toServicio(ClinicService service);
}
