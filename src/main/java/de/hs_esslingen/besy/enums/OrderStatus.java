package de.hs_esslingen.besy.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum OrderStatus {
    IN_PROGRESS,
    COMPLETED,
    APPROVALS_RECEIVED,
    APPROVED,
    REJECTED,
    SENT,
    SETTLED,
    ARCHIVED,
    DELETED
}
