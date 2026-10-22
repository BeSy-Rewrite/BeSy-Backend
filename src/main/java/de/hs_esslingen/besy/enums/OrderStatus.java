package de.hs_esslingen.besy.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Schema(enumAsRef = true)
public enum OrderStatus {
    IN_PROGRESS,
    COMPLETED,
    APPROVALS_RECEIVED,
    DEKAN_PENDING,
    APPROVED,
    REJECTED,
    SENT,
    SETTLED,
    ARCHIVED,
    DELETED
}
