package de.hs_esslingen.besy.extern.bic;

import de.hs_esslingen.besy.extern.bic.dtos.BicReqDataDTO;
import de.hs_esslingen.besy.extern.bic.dtos.BicRequestDTO;
import de.hs_esslingen.besy.extern.bic.dtos.BicVariablesDTO;
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
public class BicService {

    private static final Logger logger = LoggerFactory.getLogger(BicService.class);

    @Value("${bic.api.base-url}")
    private String bicBaseUrl;

    @Value("${bic.api.token}")
    private String authToken;

    @Value("${besy-frontend-url}")
    private String besyFrontendUrl;


    public BicService() {
    }


    public void sendBicStartRequest(Order order) {
        BicRequestDTO bicRequestDTO = createPayload(order);
        sendBicRequestAsync(bicRequestDTO);
    }

    @Async
    protected void sendBicRequestAsync(BicRequestDTO bicRequestDTO) {
        try {
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Api-Key", authToken);
            headers.set("Accept", "*/*");
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<BicRequestDTO> request = new HttpEntity<>(bicRequestDTO, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    bicBaseUrl,
                    request,
                    String.class
            );
            logger.info("BIC request sent successfully. Status: {}, Response: {}", response.getStatusCode(), response.getBody());
        } catch (Exception e) {
            logger.error("Error sending BIC request: {}", e.getMessage(), e);
        }
    }

    private BicRequestDTO createPayload(Order order) {
        String caseName = String.format("Bestellung IT%s/%s/%03d", order.getPrimaryCostCenter().getId(), order.getBookingYear(), order.getAutoIndex());
        String caseDueDate = Instant.now().plus(2, ChronoUnit.DAYS).toString();
        boolean isAutoRun = true;
        String id = String.format("ID_IT_%s", order.getId());
        String email = order.getDeliveryPerson().getEmail();
        String intranetUrl = String.format("%s/orders/%s-%s-%03d", besyFrontendUrl, order.getPrimaryCostCenter().getId(), order.getBookingYear(), order.getAutoIndex());
        String responseUrl = "https://calm-valley-90.webhook.cool";
        boolean flag_edv_permission = order.getApproval().getFlagEdvPermission();
        boolean flag_furniture_permission = order.getApproval().getFlagFurniturePermission();
        boolean flag_furniture_room = order.getApproval().getFlagFurnitureRoom();
        boolean flag_investment_room = order.getApproval().getFlagInvestmentRoom();
        boolean flag_investment_structural_measures = order.getApproval().getFlagInvestmentStructuralMeasures();
        boolean flag_media_permission = order.getApproval().getFlagMediaPermission();

        BicReqDataDTO bicReqDataDTO = new BicReqDataDTO(
                isAutoRun,
                caseName,
                id,
                email,
                intranetUrl,
                responseUrl,
                flag_edv_permission,
                flag_furniture_permission,
                flag_furniture_room,
                flag_investment_room,
                flag_investment_structural_measures,
                flag_media_permission
        );
        BicVariablesDTO bicVariablesDTO = new BicVariablesDTO(caseName, caseDueDate, bicReqDataDTO);
        return new BicRequestDTO(bicVariablesDTO);
    }

}