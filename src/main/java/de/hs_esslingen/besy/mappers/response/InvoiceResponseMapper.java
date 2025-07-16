package de.hs_esslingen.besy.mappers.response;

import de.hs_esslingen.besy.dtos.response.InvoiceResponseDTO;
import de.hs_esslingen.besy.mappers.EntityMapper;
import de.hs_esslingen.besy.models.Invoice;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface InvoiceResponseMapper extends EntityMapper<InvoiceResponseDTO, Invoice> {
}