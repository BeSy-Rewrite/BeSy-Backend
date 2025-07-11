package de.hs_esslingen.besy.mapper.request;

import de.hs_esslingen.besy.dto.request.AddressRequestDTO;
import de.hs_esslingen.besy.mapper.EntityMapper;
import de.hs_esslingen.besy.model.Address;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface AddressRequestMapper extends EntityMapper<AddressRequestDTO, Address> {
}