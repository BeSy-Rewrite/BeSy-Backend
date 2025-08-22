package de.hs_esslingen.besy.dtos.request;

import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link de.hs_esslingen.besy.models.Approval}
 */
@Value
public class ApprovalRequestDTO implements Serializable {
    Boolean flagEdvPermission;
    Boolean flagFurniturePermission;
    Boolean flagFurnitureRoom;
    Boolean flagInvestmentRoom;
    Boolean flagInvestmentStructuralMeasures;
    Boolean flagMediaPermission;
}