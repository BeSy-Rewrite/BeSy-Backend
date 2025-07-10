package de.hs_esslingen.besy.dto;

import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link de.hs_esslingen.besy.model.OrderStatus}
 */
@Value
public class OrderStatusResponseDTO implements Serializable {
    String orderStatus;
    String orderStatusComment;
}