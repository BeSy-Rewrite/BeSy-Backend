package de.hs_esslingen.besy.extern.bic.dtos;

import lombok.Value;

import java.io.Serializable;

@Value
public class BicReqDataFlagsDTO implements Serializable {
    Boolean itPermission;
    Boolean furniturePermission;
    Boolean furnitureRoom;
    Boolean investmentRoom;
    Boolean investment_structural_measures;
    Boolean media_permission;
}
