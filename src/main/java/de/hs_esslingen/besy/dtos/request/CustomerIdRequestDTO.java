package de.hs_esslingen.besy.dtos.request;

import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link de.hs_esslingen.besy.models.CustomerId}
 */
@Value
public class CustomerIdRequestDTO implements Serializable {
    String customerId;
    String comment;
}