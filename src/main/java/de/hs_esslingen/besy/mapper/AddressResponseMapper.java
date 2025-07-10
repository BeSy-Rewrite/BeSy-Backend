package de.hs_esslingen.besy.mapper;

import de.hs_esslingen.besy.dto.AddressResponseDTO;
import de.hs_esslingen.besy.model.Address;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface AddressResponseMapper extends EntityMapper<AddressResponseDTO, Address> {
}