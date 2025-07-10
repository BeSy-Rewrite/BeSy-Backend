package de.hs_esslingen.besy.mapper;

import de.hs_esslingen.besy.dto.CurrencyResponseDTO;
import de.hs_esslingen.besy.model.Currency;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface CurrencyResponseMapper extends EntityMapper<CurrencyResponseDTO, Currency> {
}