package de.hs_esslingen.besy.services;

import de.hs_esslingen.besy.dtos.response.QuotationResponseDTO;
import de.hs_esslingen.besy.mappers.response.QuotationResponseMapper;
import de.hs_esslingen.besy.models.Quotation;
import de.hs_esslingen.besy.repositories.QuotationRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class QuotationService {

    private final QuotationRepository quotationRepository;
    private final QuotationResponseMapper quotationResponseMapper;

    public ResponseEntity<List<QuotationResponseDTO>> getQuotationsOfOrder(Long orderId) {
        List<Quotation> quotations = quotationRepository.findByOrder_OrderId(orderId);
        List<QuotationResponseDTO> quotationResponseDTOS = quotationResponseMapper.toDto(quotations);
        return ResponseEntity.ok(quotationResponseDTOS);
    }
}
