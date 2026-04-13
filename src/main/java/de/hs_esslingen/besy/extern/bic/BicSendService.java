package de.hs_esslingen.besy.extern.bic;

import de.hs_esslingen.besy.extern.bic.dtos.BicReqDataDTO;
import de.hs_esslingen.besy.extern.bic.dtos.BicReqDataFlagsDTO;
import de.hs_esslingen.besy.extern.bic.dtos.BicRequestDTO;
import de.hs_esslingen.besy.extern.bic.dtos.BicVariablesDTO;
import de.hs_esslingen.besy.helper.OrderNumberHelper;
import de.hs_esslingen.besy.models.Approval;
import de.hs_esslingen.besy.models.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@EnableRetry
public class BicSendService {

    private static final Logger logger = LoggerFactory.getLogger(BicSendService.class);

    @Value("${bic.api.url}")
    private String bicUrl;

    @Value("${bic.api.token}")
    private String authToken;

    @Value("${besy-frontend-url}")
    private String besyFrontendUrl;

    @Value("${bic.api.response-url-data}")
    private String responseUrlData;

    @Value("${bic.api.response-url-file}")
    private String responseUrlFile;

    @Value("${bic.is-auto-run}")
    private boolean isAutoRun;

    @Value("${bic.req-data-auth-flag}")
    private String authFlag;

    private final OrderNumberHelper orderNumberHelper;
    private final RestTemplate restTemplate;

    public BicSendService(OrderNumberHelper orderNumberHelper, RestTemplate restTemplate) {
        this.orderNumberHelper = orderNumberHelper;
        this.restTemplate = restTemplate;
    }

    public void sendBicStartRequest(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("Order cannot be null");
        }
        BicRequestDTO bicRequestDTO = createPayload(order);
        sendBicRequestAsync(bicRequestDTO);
    }

    @Async
    protected void sendBicRequestAsync(BicRequestDTO bicRequestDTO) {
        if (isAutoRun) logger.warn("BIC is set to auto run.");

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Api-Key", authToken);
            headers.set("Accept", "*/*");
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<BicRequestDTO> request = new HttpEntity<>(bicRequestDTO, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    bicUrl,
                    request,
                    String.class
            );
            logger.info("BIC request sent successfully. Status: {}, Response: {}", response.getStatusCode(), response.getBody());
        } catch (Exception e) {
            logger.error("Error sending BIC request: {}", e.getMessage(), e);
        }
    }

    private BicRequestDTO createPayload(Order order) {
        String oderNumber = orderNumberHelper.generateOrderNumber(order);
        String oderNumberForUrl = oderNumber.replace('/', '-');

        String caseName = String.format("Bestellung %s", oderNumber);
        String caseDueDate = Instant.now().plus(2, ChronoUnit.DAYS).toString();
        String id = String.format("ID_%s", oderNumberForUrl);
        String email = order.getDeliveryPerson().getEmail();
        String intranetUrl = String.format("%s/orders/%s", besyFrontendUrl, oderNumberForUrl.substring(2));
        BicReqDataFlagsDTO bicReqDataFlagsDTO = getBicReqDataFlagsDTO(order.getApproval());

        BicReqDataDTO bicReqDataDTO = new BicReqDataDTO(
                isAutoRun,
                caseName,
                id,
                email,
                intranetUrl,
                responseUrlData.replace("{order-number}", oderNumberForUrl),
                responseUrlFile.replace("{order-number}", oderNumberForUrl),
                authFlag,
                bicReqDataFlagsDTO
        );
        BicVariablesDTO bicVariablesDTO = new BicVariablesDTO(caseName, caseDueDate, bicReqDataDTO);
        return new BicRequestDTO(bicVariablesDTO);
    }

    private static BicReqDataFlagsDTO getBicReqDataFlagsDTO(Approval approval) {
        boolean flag_edv_permission = approval.getFlagEdvPermission();
        boolean flag_furniture_permission = approval.getFlagFurniturePermission();
        boolean flag_furniture_room = approval.getFlagFurnitureRoom();
        boolean flag_investment_room = approval.getFlagInvestmentRoom();
        boolean flag_investment_structural_measures = approval.getFlagInvestmentStructuralMeasures();
        boolean flag_media_permission = approval.getFlagMediaPermission();

        return new BicReqDataFlagsDTO(
                flag_edv_permission,
                flag_furniture_permission,
                flag_furniture_room,
                flag_investment_room,
                flag_investment_structural_measures,
                flag_media_permission
        );
    }

}