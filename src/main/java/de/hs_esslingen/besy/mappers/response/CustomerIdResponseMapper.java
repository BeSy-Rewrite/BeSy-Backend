package de.hs_esslingen.besy.mappers.response;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import de.hs_esslingen.besy.dtos.response.CustomerIdResponseDTO;
import de.hs_esslingen.besy.mappers.EntityMapper;
import de.hs_esslingen.besy.models.CustomerId;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface CustomerIdResponseMapper extends EntityMapper<CustomerIdResponseDTO, CustomerId> {
    @Override
    CustomerIdResponseDTO toDto(CustomerId entity);
}
