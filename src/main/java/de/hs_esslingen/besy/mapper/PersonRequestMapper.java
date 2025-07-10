package de.hs_esslingen.besy.mapper;

import de.hs_esslingen.besy.dto.PersonRequestDTO;
import de.hs_esslingen.besy.model.Person;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)public interface PersonRequestMapper extends EntityMapper<PersonRequestDTO, Person> {
  }