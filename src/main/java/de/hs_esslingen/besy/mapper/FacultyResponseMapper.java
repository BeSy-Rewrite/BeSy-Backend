package de.hs_esslingen.besy.mapper;

import de.hs_esslingen.besy.dto.FacultyResponseDTO;
import de.hs_esslingen.besy.model.Faculty;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface FacultyResponseMapper extends EntityMapper<FacultyResponseDTO, Faculty> {
}