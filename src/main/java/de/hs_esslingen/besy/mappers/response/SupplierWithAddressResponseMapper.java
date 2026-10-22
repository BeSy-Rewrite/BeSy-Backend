package de.hs_esslingen.besy.mappers.response;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import de.hs_esslingen.besy.dtos.response.SupplierWithAddressResponseDTO;
import de.hs_esslingen.besy.mappers.EntityMapper;
import de.hs_esslingen.besy.models.Supplier;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING, uses = {
        SupplierResponseMapper.class })
public interface SupplierWithAddressResponseMapper extends EntityMapper<SupplierWithAddressResponseDTO, Supplier> {

    @Override
    @Mapping(target = "supplier", source = ".")
    SupplierWithAddressResponseDTO toDto(Supplier entity);
}
