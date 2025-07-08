package de.hs_esslingen.besy.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "log")
public class Log {
    @Id
    @Column(name = "log_id", columnDefinition = "bigint not null")
    private Long id;

    @Column(name = "log_entry_time", nullable = false)
    private Instant logEntryTime;

    @Lob
    @Column(name = "log_entry", nullable = false)
    private String logEntry;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_name", nullable = false)
    private de.hs_esslingen.besy.models.User userName;

}