package de.hs_esslingen.besy.mappers;

import de.hs_esslingen.besy.dtos.response.AddressTypeResponseDTO;
import de.hs_esslingen.besy.models.AddressType;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface AddressTypeMapper extends EntityMapper<AddressTypeResponseDTO, AddressType> {
}