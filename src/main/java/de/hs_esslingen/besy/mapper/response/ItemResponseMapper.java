package de.hs_esslingen.besy.mapper.response;

import de.hs_esslingen.besy.dto.response.ItemResponseDTO;
import de.hs_esslingen.besy.mapper.EntityMapper;
import de.hs_esslingen.besy.model.Item;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface ItemResponseMapper extends EntityMapper<ItemResponseDTO, Item> {
}