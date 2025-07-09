package de.hs_esslingen.besy.mapper;

import de.hs_esslingen.besy.dto.AddressDto;
import de.hs_esslingen.besy.dto.AddressDto1;
import de.hs_esslingen.besy.model.Address;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface AddressMapper {
    Address toEntity(AddressDto1 addressDto1);

    AddressDto1 toDto(Address address);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Address partialUpdate(AddressDto1 addressDto1, @MappingTarget Address address);

    Address toEntity(AddressDto addressDto);

    AddressDto toDto1(Address address);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Address partialUpdate(AddressDto addressDto, @MappingTarget Address address);
}