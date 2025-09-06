package de.hs_esslingen.besy.dtos.response;

import de.hs_esslingen.besy.enums.OrderStatus;
import lombok.Value;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * DTO for {@link de.hs_esslingen.besy.models.OrderStatusHistory}
 */
@Value
public class OrderStatusHistoryResponseDTO implements Serializable {
    OrderStatus status;
    LocalDateTime timestamp;
}