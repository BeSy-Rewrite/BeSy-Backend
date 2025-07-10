package de.hs_esslingen.besy.mapper;

import de.hs_esslingen.besy.dto.UserResponseDTO;
import de.hs_esslingen.besy.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserResponseMapper extends EntityMapper<UserResponseDTO, User> {
}