package de.hs_esslingen.besy.dtos.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class InsyItemRequestDTO {

    private int itemId;

    private String itemName;

    private double itemPricePerUnit;

}
