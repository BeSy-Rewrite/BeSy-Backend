package de.hs_esslingen.besy.mappers.request;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import de.hs_esslingen.besy.dtos.request.UserPreferencesRequestDTO;
import de.hs_esslingen.besy.mappers.EntityMapper;
import de.hs_esslingen.besy.mappers.JsonNodeMapper;
import de.hs_esslingen.besy.models.UserPreferences;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING, uses = {
    JsonNodeMapper.class })
public interface UserPreferencesRequestMapper extends EntityMapper<UserPreferencesRequestDTO, UserPreferences> {
}
