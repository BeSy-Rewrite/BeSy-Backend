package de.hs_esslingen.besy.dtos.response;

import lombok.Value;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * DTO for {@link de.hs_esslingen.besy.models.CostCenter}
 */
@Value
public class CostCenterResponseDTO implements Serializable {
    String id;
    String name;
    LocalDate beginDate;
    LocalDate endDate;
    String comment;

}