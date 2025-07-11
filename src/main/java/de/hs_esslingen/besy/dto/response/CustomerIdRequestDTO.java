package de.hs_esslingen.besy.dto.response;

import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link de.hs_esslingen.besy.model.CustomerId}
 */
@Value
public class CustomerIdRequestDTO implements Serializable {
    String customerId;
    String supplierName;
    String customerIdComment;
}