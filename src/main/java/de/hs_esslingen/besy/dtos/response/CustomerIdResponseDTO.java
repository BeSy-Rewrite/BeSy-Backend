package de.hs_esslingen.besy.dtos.response;

import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link de.hs_esslingen.besy.models.CustomerId}
 */
@Value
public class CustomerIdResponseDTO implements Serializable {
    String customerId;
    String supplierName;
    String customerIdComment;
}