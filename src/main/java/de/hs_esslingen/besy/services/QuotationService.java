package de.hs_esslingen.besy.services;

import de.hs_esslingen.besy.dtos.request.QuotationRequestDTO;
import de.hs_esslingen.besy.dtos.response.QuotationResponseDTO;
import de.hs_esslingen.besy.mappers.request.QuotationRequestMapper;
import de.hs_esslingen.besy.mappers.response.QuotationResponseMapper;
import de.hs_esslingen.besy.models.Order;
import de.hs_esslingen.besy.models.Quotation;
import de.hs_esslingen.besy.models.QuotationId;
import de.hs_esslingen.besy.repositories.OrderRepository;
import de.hs_esslingen.besy.repositories.QuotationRepository;
import de.hs_esslingen.besy.repositories.SupplierRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@AllArgsConstructor
public class QuotationService {

    private final QuotationRepository quotationRepository;
    private final OrderRepository orderRepository;
    private final SupplierRepository supplierRepository;
    private final QuotationRequestMapper quotationRequestMapper;
    private final QuotationResponseMapper quotationResponseMapper;

    public ResponseEntity<List<QuotationResponseDTO>> getQuotationsOfOrder(Long orderId) {
        List<Quotation> quotations = quotationRepository.findByOrderId(orderId);
        List<QuotationResponseDTO> quotationResponseDTOS = quotationResponseMapper.toDto(quotations);
        return ResponseEntity.ok(quotationResponseDTOS);
    }

    public ResponseEntity<List<QuotationResponseDTO>> createQuotation(Long orderId, List<QuotationRequestDTO> dtos) {
        List<Quotation> quotations = quotationRequestMapper.toEntity(dtos);
        Order order = orderRepository.getReferenceById(orderId);

        // Server-side id generation
        int smallestIndex = quotationRepository.findByOrderId(orderId).size();
        AtomicInteger indexCounter = new AtomicInteger(smallestIndex);

        quotations.forEach(quotation -> {
            QuotationId quotationId = new QuotationId(orderId, (short) indexCounter.incrementAndGet());

            quotation.setId(quotationId);
            quotation.setOrder(order);
        });
        List<Quotation> quotationsPersisted = quotationRepository.saveAll(quotations);
        List<QuotationResponseDTO> quotationResponseDTOS = quotationResponseMapper.toDto(quotationsPersisted);
        return ResponseEntity.ok(quotationResponseDTOS);
    }

    public ResponseEntity<String> deleteQuotation(Long orderId, Short index) {
        quotationRepository.deleteById(new QuotationId(orderId, index));
        return ResponseEntity.noContent().build();
    }

    public boolean existsQuotation(Long orderId, Short index) {
        return quotationRepository.existsById(new QuotationId(orderId, index));
    }
}

