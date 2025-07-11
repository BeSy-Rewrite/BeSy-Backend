package de.hs_esslingen.besy.dto.response;

import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link de.hs_esslingen.besy.model.PrimaryCostCenter}
 */
@Value
public class PrimaryCostCenterResponseDTO implements Serializable {
    String costCenterId;
    String facultyAbbr;
}