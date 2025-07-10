package de.hs_esslingen.besy.repository;

import de.hs_esslingen.besy.dto.PersonResponseDTO;
import de.hs_esslingen.besy.mapper.EntityMapper;
import de.hs_esslingen.besy.model.Address;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface AddressPersonResponseMapper extends EntityMapper<PersonResponseDTO.AddressPersonResponseDTO, Address> {
}