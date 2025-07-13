package de.hs_esslingen.besy.mappers;

import org.mapstruct.*;

import java.util.List;

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
