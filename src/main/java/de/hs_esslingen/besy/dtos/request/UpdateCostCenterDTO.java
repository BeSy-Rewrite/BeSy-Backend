package de.hs_esslingen.besy.dtos.request;

import java.io.Serializable;
import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import lombok.Value;

/**
 * DTO for {@link de.hs_esslingen.besy.models.CostCenter}
 */
@Value
public class UpdateCostCenterDTO implements Serializable {
    @NotNull
    String name;

    LocalDate beginDate;
    LocalDate endDate;
    String comment;
}
