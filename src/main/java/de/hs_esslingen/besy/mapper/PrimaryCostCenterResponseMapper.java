package de.hs_esslingen.besy.mapper;

import de.hs_esslingen.besy.dto.PrimaryCostCenterResponseDTO;
import de.hs_esslingen.besy.model.PrimaryCostCenter;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface PrimaryCostCenterResponseMapper extends EntityMapper<PrimaryCostCenterResponseDTO, PrimaryCostCenter> {
}