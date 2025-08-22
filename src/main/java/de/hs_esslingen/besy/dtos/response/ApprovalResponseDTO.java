package de.hs_esslingen.besy.dtos.response;

import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link de.hs_esslingen.besy.models.Approval}
 */
@Value
public class ApprovalResponseDTO implements Serializable {
    Long orderId;
    Boolean flagEdvPermission;
    Boolean flagFurniturePermission;
    Boolean flagFurnitureRoom;
    Boolean flagInvestmentRoom;
    Boolean flagInvestmentStructuralMeasures;
    Boolean flagMediaPermission;
}