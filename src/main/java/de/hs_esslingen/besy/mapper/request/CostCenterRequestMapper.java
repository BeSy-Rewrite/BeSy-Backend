package de.hs_esslingen.besy.mapper.request;

import de.hs_esslingen.besy.dto.request.CostCenterRequestDTO;
import de.hs_esslingen.besy.mapper.EntityMapper;
import de.hs_esslingen.besy.model.CostCenter;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface CostCenterRequestMapper extends EntityMapper<CostCenterRequestDTO, CostCenter> {
}