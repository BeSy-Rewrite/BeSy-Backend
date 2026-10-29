package de.hs_esslingen.besy.mappers.response;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import de.hs_esslingen.besy.dtos.response.UserPreferencesResponseDTO;
import de.hs_esslingen.besy.mappers.EntityMapper;
import de.hs_esslingen.besy.mappers.JsonNodeMapper;
import de.hs_esslingen.besy.models.UserPreferences;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING, uses = {
    JsonNodeMapper.class })
public interface UserPreferencesResponseMapper extends EntityMapper<UserPreferencesResponseDTO, UserPreferences> {
}
