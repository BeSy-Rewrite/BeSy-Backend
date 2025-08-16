package de.hs_esslingen.besy.dtos.request;

import lombok.Value;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * DTO for {@link de.hs_esslingen.besy.models.CostCenter}
 */
@Value
public class CostCenterRequestDTO implements Serializable {
    String id; // Gets assigned by user
    String name;
    LocalDate beginDate;
    LocalDate endDate;
    String comment;
}