package de.hs_esslingen.besy.mapper;

import de.hs_esslingen.besy.dto.response.AddressTypeResponseDTO;
import de.hs_esslingen.besy.model.AddressType;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface AddressTypeMapper extends EntityMapper<AddressTypeResponseDTO, AddressType> {
}