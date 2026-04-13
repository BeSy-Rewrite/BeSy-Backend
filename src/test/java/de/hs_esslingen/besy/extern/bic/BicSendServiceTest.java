package de.hs_esslingen.besy.extern.bic;

import de.hs_esslingen.besy.extern.bic.dtos.BicRequestDTO;
import de.hs_esslingen.besy.helper.OrderNumberHelper;
import de.hs_esslingen.besy.models.Approval;
import de.hs_esslingen.besy.models.Order;
import de.hs_esslingen.besy.models.Person;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BicSendServiceTest {

    @Mock
    private OrderNumberHelper orderNumberHelper;

    @Mock
    private RestTemplate restTemplate;

    private BicSendService bicSendService;

    private final String bicUrl = "http://bic-api.com";
    private final String authToken = "test-token";
    private final String besyFrontendUrl = "http://frontend.com";
    private final String responseUrlData = "http://callback-data.com";
    private final String responseUrlFile = "http://callback-file.com";

    @BeforeEach
    void setUp() {
        bicSendService = new BicSendService(orderNumberHelper, restTemplate);
        ReflectionTestUtils.setField(bicSendService, "bicUrl", bicUrl);
        ReflectionTestUtils.setField(bicSendService, "authToken", authToken);
        ReflectionTestUtils.setField(bicSendService, "besyFrontendUrl", besyFrontendUrl);
        ReflectionTestUtils.setField(bicSendService, "responseUrlData", responseUrlData);
        ReflectionTestUtils.setField(bicSendService, "responseUrlFile", responseUrlFile);
        ReflectionTestUtils.setField(bicSendService, "isAutoRun", true);
    }

    @Test
    void sendBicStartRequest_Success() {
        // Arrange
        Order order = createTestOrder();
        when(orderNumberHelper.generateOrderNumber(order)).thenReturn("2024/001");

        ResponseEntity<String> responseEntity = new ResponseEntity<>("Success", HttpStatus.OK);
        when(restTemplate.postForEntity(eq(bicUrl), any(HttpEntity.class), eq(String.class)))
                .thenReturn(responseEntity);

        // Act
        bicSendService.sendBicStartRequest(order);

        // Assert
        ArgumentCaptor<HttpEntity<BicRequestDTO>> argumentCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).postForEntity(eq(bicUrl), argumentCaptor.capture(), eq(String.class));

        HttpEntity<BicRequestDTO> capturedRequest = argumentCaptor.getValue();
        assertEquals(authToken, capturedRequest.getHeaders().getFirst("X-Api-Key"));
        assertEquals("application/json", capturedRequest.getHeaders().getContentType().toString());

        BicRequestDTO payload = capturedRequest.getBody();
        assertNotNull(payload);
        assertEquals("Bestellung 2024/001", payload.getVariables().getCaseName());
        assertEquals("test@example.com", payload.getVariables().getReqData().getEmail());
        // URL is generated as: intranetUrl = String.format("%s/orders/%s", besyFrontendUrl, oderNumber.replace('/', '-').substring(2));
        // For "2024/001" -> "2024-001" -> substring(2) -> "24-001"
        assertEquals(besyFrontendUrl + "/orders/24-001", payload.getVariables().getReqData().getIntranetUrl());
        assertTrue(payload.getVariables().getReqData().getAutoRun());

        // Verify flags
        assertTrue(payload.getVariables().getReqData().getFlags().getItPermission());
        assertFalse(payload.getVariables().getReqData().getFlags().getFurniturePermission());
    }

    @Test
    void sendBicStartRequest_NullOrder_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> bicSendService.sendBicStartRequest(null));
    }

    @Test
    void sendBicRequestAsync_HandlesException() {
        // Arrange
        Order order = createTestOrder();
        when(orderNumberHelper.generateOrderNumber(order)).thenReturn("2024/001");

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenThrow(new RuntimeException("API Down"));

        // Act & Assert (Should not throw exception because it's caught in the method)
        assertDoesNotThrow(() -> bicSendService.sendBicStartRequest(order));
        verify(restTemplate).postForEntity(anyString(), any(HttpEntity.class), eq(String.class));
    }

    private Order createTestOrder() {
        Person deliveryPerson = Person.builder()
                .email("test@example.com")
                .build();

        Approval approval = Approval.builder()
                .flagEdvPermission(true)
                .flagFurniturePermission(false)
                .flagFurnitureRoom(true)
                .flagInvestmentRoom(false)
                .flagInvestmentStructuralMeasures(true)
                .flagMediaPermission(false)
                .build();

        Order order = Order.builder()
                .id(1L)
                .deliveryPerson(deliveryPerson)
                .approval(approval)
                .build();

        approval.setOrder(order);
        return order;
    }
}