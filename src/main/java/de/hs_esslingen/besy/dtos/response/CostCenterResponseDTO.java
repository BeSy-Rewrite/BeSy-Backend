package de.hs_esslingen.besy.dtos.response;

import java.io.Serializable;
import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import lombok.Value;

/**
 * DTO for {@link de.hs_esslingen.besy.models.CostCenter}
 */
@Value
public class CostCenterResponseDTO implements Serializable {
    @NotNull
    String id;
    String name;
    LocalDate beginDate;
    LocalDate endDate;
    String comment;

}
