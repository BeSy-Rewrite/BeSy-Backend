package de.hs_esslingen.besy.dtos.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class InsyOrderRequestDTO {

    private int orderId;

    private LocalDateTime orderCreatedDate;

    private String supplierName;

    private String costCenterName;

    private String userName;

    private double orderQuotePrice;

    private List<InsyItemRequestDTO> items;
}
