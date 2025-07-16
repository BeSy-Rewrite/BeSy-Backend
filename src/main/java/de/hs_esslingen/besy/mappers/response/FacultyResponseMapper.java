package de.hs_esslingen.besy.mappers.response;

import de.hs_esslingen.besy.dtos.response.FacultyResponseDTO;
import de.hs_esslingen.besy.mappers.EntityMapper;
import de.hs_esslingen.besy.models.Faculty;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface FacultyResponseMapper extends EntityMapper<FacultyResponseDTO, Faculty> {
}