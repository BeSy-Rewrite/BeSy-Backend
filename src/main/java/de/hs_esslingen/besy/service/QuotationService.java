package de.hs_esslingen.besy.service;

import de.hs_esslingen.besy.dto.QuotationResponseDTO;
import de.hs_esslingen.besy.mapper.QuotationResponseMapper;
import de.hs_esslingen.besy.model.Quotation;
import de.hs_esslingen.besy.repository.QuotationRepository;
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
