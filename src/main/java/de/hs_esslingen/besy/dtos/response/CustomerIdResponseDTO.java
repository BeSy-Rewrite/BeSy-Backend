package de.hs_esslingen.besy.dtos.response;

import lombok.Value;

import java.io.Serializable;

/**
 * DTO for the backwards-compatible GET /suppliers/{id}/customer_ids endpoint.
 * customer_id is sourced from {@link de.hs_esslingen.besy.models.Supplier#getCustomerNumber()}.
 */
@Value
public class CustomerIdResponseDTO implements Serializable {
    Integer supplierId;
    String customerId;
}
