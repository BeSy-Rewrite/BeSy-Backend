package de.hs_esslingen.besy.dtos.response;

import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link de.hs_esslingen.besy.models.PrimaryCostCenter}
 */
@Value
public class PrimaryCostCenterResponseDTO implements Serializable {
    String costCenterId;
    String facultyAbbr;
}