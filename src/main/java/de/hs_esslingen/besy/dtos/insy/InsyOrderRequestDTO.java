package de.hs_esslingen.besy.dtos.insy;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class InsyOrderRequestDTO {

    private Long besyId;

    private String orderNumber;

    private String description;

    private LocalDateTime orderCreatedDate;

    private String supplierName;

    private String costCenter;

    private String userName;

    private BigDecimal orderQuotePrice;

    private List<InsyItemRequestDTO> items;
}
