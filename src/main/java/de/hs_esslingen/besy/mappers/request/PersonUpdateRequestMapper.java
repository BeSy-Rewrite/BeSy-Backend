package de.hs_esslingen.besy.mappers.request;

import de.hs_esslingen.besy.dtos.request.PersonUpdateRequestDTO;
import de.hs_esslingen.besy.mappers.EntityMapper;
import de.hs_esslingen.besy.models.Person;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface PersonUpdateRequestMapper extends EntityMapper<PersonUpdateRequestDTO, Person> {
}
