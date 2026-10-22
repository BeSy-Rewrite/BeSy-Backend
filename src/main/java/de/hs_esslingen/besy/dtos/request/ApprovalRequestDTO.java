package de.hs_esslingen.besy.dtos.request;

import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/**
 * DTO for {@link de.hs_esslingen.besy.models.Approval}
 */
@Value
@Builder
@Jacksonized
@AllArgsConstructor
public class ApprovalRequestDTO implements Serializable {
    @Builder.Default
    @Schema(description = "Flag indicating if the user has EDV permission", defaultValue = "false")
    Boolean flagEdvPermission = false;
    @Builder.Default
    @Schema(description = "Flag indicating if the user has furniture permission", defaultValue = "false")
    Boolean flagFurniturePermission = false;
    @Builder.Default
    @Schema(description = "Flag indicating if the user has furniture room permission", defaultValue = "false")
    Boolean flagFurnitureRoom = false;
    @Builder.Default
    @Schema(description = "Flag indicating if the user has investment permission", defaultValue = "false")
    Boolean flagInvestmentRoom = false;
    @Builder.Default
    @Schema(description = "Flag indicating if the user has investment structural measures permission", defaultValue = "false")
    Boolean flagInvestmentStructuralMeasures = false;
    @Builder.Default
    @Schema(description = "Flag indicating if the user has media permission", defaultValue = "false")
    Boolean flagMediaPermission = false;
}
