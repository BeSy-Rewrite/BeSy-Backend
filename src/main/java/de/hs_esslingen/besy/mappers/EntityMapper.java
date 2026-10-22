package de.hs_esslingen.besy.mappers;

import java.util.List;

import org.mapstruct.BeanMapping;
import org.mapstruct.MapperConfig;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@MapperConfig(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface EntityMapper<D, E> {
    D toDto(E entity);

    E toEntity(D dto);

    List<D> toDto(List<E> entityList);

    List<E> toEntity(List<D> dtoList);

    @Named("partialUpdate")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void partialUpdate(@MappingTarget E entity, D dto);

}
