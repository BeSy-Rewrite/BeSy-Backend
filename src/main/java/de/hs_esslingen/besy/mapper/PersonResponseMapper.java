package de.hs_esslingen.besy.mapper;

import de.hs_esslingen.besy.dto.PersonResponseDTO;
import de.hs_esslingen.besy.model.Person;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface PersonResponseMapper extends EntityMapper<PersonResponseDTO, Person> {
}