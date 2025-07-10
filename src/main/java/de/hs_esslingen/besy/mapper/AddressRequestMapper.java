package de.hs_esslingen.besy.mapper;

import de.hs_esslingen.besy.dto.AddressRequestDTO;
import de.hs_esslingen.besy.model.Address;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface AddressRequestMapper extends EntityMapper<AddressRequestDTO, Address> {
}