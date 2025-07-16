package de.hs_esslingen.besy.dtos.response;

import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link de.hs_esslingen.besy.models.OrderStatus}
 */
@Value
public class OrderStatusResponseDTO implements Serializable {
    String orderStatus;
    String orderStatusComment;
}