package de.hs_esslingen.besy.mapper.response;

import de.hs_esslingen.besy.dto.response.FacultyResponseDTO;
import de.hs_esslingen.besy.mapper.EntityMapper;
import de.hs_esslingen.besy.model.Faculty;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface FacultyResponseMapper extends EntityMapper<FacultyResponseDTO, Faculty> {
}