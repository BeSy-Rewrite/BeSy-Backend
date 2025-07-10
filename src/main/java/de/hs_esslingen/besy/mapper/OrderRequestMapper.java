package de.hs_esslingen.besy.mapper;

import de.hs_esslingen.besy.dto.OrderRequestDTO;
import de.hs_esslingen.besy.model.Order;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface OrderRequestMapper extends EntityMapper<OrderRequestDTO, Order> {
}