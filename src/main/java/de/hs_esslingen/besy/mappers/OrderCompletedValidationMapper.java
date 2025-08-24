package de.hs_esslingen.besy.mappers;

import de.hs_esslingen.besy.interfaces.OrderCompletedValidationDAO;
import de.hs_esslingen.besy.models.Order;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface OrderCompletedValidationMapper extends EntityMapper<Order, OrderCompletedValidationDAO> {
}