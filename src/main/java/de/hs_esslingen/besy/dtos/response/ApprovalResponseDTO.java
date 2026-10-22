package de.hs_esslingen.besy.dtos.response;

import java.io.Serializable;

import jakarta.validation.constraints.NotNull;
import lombok.Value;

/**
 * DTO for {@link de.hs_esslingen.besy.models.Approval}
 */
@Value
public class ApprovalResponseDTO implements Serializable {
    @NotNull
    Long orderId;
    @NotNull
    Boolean flagEdvPermission;
    @NotNull
    Boolean flagFurniturePermission;
    @NotNull
    Boolean flagFurnitureRoom;
    @NotNull
    Boolean flagInvestmentRoom;
    @NotNull
    Boolean flagInvestmentStructuralMeasures;
    @NotNull
    Boolean flagMediaPermission;
}
