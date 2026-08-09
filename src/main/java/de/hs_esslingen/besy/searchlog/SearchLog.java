package de.hs_esslingen.besy.searchlog;

import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "search_log")
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class SearchLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "query", nullable = false)
    private String query;

    @Column(name = "filters", columnDefinition = "TEXT")
    private String filters;

    @Column(name = "result_count", nullable = false)
    private Long resultCount;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "searched_at", nullable = false)
    private OffsetDateTime searchedAt;
}
