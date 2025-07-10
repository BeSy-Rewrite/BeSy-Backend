package de.hs_esslingen.besy.dto;

import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link de.hs_esslingen.besy.model.CustomerId}
 */
@Value
public class CustomerIdResponseDTO implements Serializable {
    String customerId;
    String supplierName;
    String customerIdComment;
}