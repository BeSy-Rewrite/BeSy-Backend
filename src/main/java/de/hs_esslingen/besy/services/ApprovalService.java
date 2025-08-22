package de.hs_esslingen.besy.services;

import de.hs_esslingen.besy.dtos.response.ApprovalResponseDTO;
import de.hs_esslingen.besy.mappers.response.ApprovalResponseMapper;
import de.hs_esslingen.besy.models.Approval;
import de.hs_esslingen.besy.repositories.ApprovalRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ApprovalService {

    private ApprovalRepository approvalRepository;
    private final ApprovalResponseMapper approvalResponseMapper;

    public ResponseEntity<ApprovalResponseDTO> getApprovalOfOrder(Long orderId) {
        Approval approval = approvalRepository.getById(orderId);
        return ResponseEntity.ok(approvalResponseMapper.toDto(approval));
    }

}
