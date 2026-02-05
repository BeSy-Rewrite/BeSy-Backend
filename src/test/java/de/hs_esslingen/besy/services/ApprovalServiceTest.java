package de.hs_esslingen.besy.services;

import de.hs_esslingen.besy.dtos.request.ApprovalRequestDTO;
import de.hs_esslingen.besy.dtos.response.ApprovalResponseDTO;
import de.hs_esslingen.besy.mappers.request.ApprovalRequestMapper;
import de.hs_esslingen.besy.mappers.response.ApprovalResponseMapper;
import de.hs_esslingen.besy.models.Approval;
import de.hs_esslingen.besy.repositories.ApprovalRepository;
import de.hs_esslingen.besy.repositories.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApprovalServiceTest {

    @Mock
    private ApprovalRepository approvalRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ApprovalRequestMapper approvalRequestMapper;

    @Mock
    private ApprovalResponseMapper approvalResponseMapper;

    @InjectMocks
    private ApprovalService approvalService;

    @Test
    void should_return_approval_dto_when_getting_approval_of_order() {
        Long orderId = 10L;
        Approval approval = new Approval();
        approval.setOrderId(orderId);
        ApprovalResponseDTO responseDto = new ApprovalResponseDTO(orderId, true, false, false, true, false, true);

        when(approvalRepository.getById(orderId)).thenReturn(approval);
        when(approvalResponseMapper.toDto(approval)).thenReturn(responseDto);

        ResponseEntity<ApprovalResponseDTO> response = approvalService.getApprovalOfOrder(orderId);

        assertSame(responseDto, response.getBody());
        verify(approvalRepository).getById(orderId);
        verify(approvalResponseMapper).toDto(approval);
        verifyNoInteractions(orderRepository);
        verify(approvalRepository, never()).delete(any(Approval.class));
        verify(approvalRepository, never()).deleteById(anyLong());
        verify(approvalRepository, never()).deleteAll();
    }

    @Test
    void should_apply_partial_update_and_save_approval() {
        Long orderId = 11L;
        ApprovalRequestDTO dto = new ApprovalRequestDTO(true, false, true, false, true, false);
        Approval approval = new Approval();
        approval.setOrderId(orderId);
        Approval persisted = new Approval();
        persisted.setOrderId(orderId);
        ApprovalResponseDTO responseDto = new ApprovalResponseDTO(orderId, true, false, true, false, true, false);

        when(approvalRepository.getById(orderId)).thenReturn(approval);
        when(approvalRepository.saveAndFlush(approval)).thenReturn(persisted);
        when(approvalResponseMapper.toDto(persisted)).thenReturn(responseDto);

        ResponseEntity<ApprovalResponseDTO> response = approvalService.updateApprovalOfOrder(orderId, dto);

        InOrder inOrder = inOrder(approvalRepository, approvalRequestMapper, approvalResponseMapper);
        inOrder.verify(approvalRepository).getById(orderId);
        inOrder.verify(approvalRequestMapper).partialUpdate(approval, dto);
        inOrder.verify(approvalRepository).saveAndFlush(approval);
        inOrder.verify(approvalResponseMapper).toDto(persisted);

        assertSame(responseDto, response.getBody());
        verifyNoInteractions(orderRepository);
        verify(approvalRepository, never()).delete(any(Approval.class));
        verify(approvalRepository, never()).deleteById(anyLong());
        verify(approvalRepository, never()).deleteAll();
    }
}
