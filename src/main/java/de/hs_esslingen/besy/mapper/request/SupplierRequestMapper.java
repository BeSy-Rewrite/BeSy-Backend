package de.hs_esslingen.besy.mapper.request;

import de.hs_esslingen.besy.dto.request.SupplierRequestDTO;
import de.hs_esslingen.besy.mapper.EntityMapper;
import de.hs_esslingen.besy.model.Supplier;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface SupplierRequestMapper extends EntityMapper<SupplierRequestDTO, Supplier> {
}