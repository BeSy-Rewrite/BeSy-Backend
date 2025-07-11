package de.hs_esslingen.besy.dto.response;

import lombok.Value;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * DTO for {@link de.hs_esslingen.besy.model.CostCenter}
 */
@Value
public class CostCenterResponseDTO implements Serializable {
    String costCenterId;
    String costCenterName;
    LocalDate costCenterBeginDate;
    LocalDate costCenterEndDate;
    String costCenterComment;
}