package de.hs_esslingen.besy.dto.request;

import lombok.Value;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * DTO for {@link de.hs_esslingen.besy.model.CostCenter}
 */
@Value
public class CostCenterRequestDTO implements Serializable {
    String costCenterId;
    String costCenterName;
    LocalDate costCenterBeginDate;
    LocalDate costCenterEndDate;
    String costCenterComment;
}