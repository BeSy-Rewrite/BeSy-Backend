package de.hs_esslingen.besy.searchlog;

import java.time.OffsetDateTime;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SearchLogService {

    private final SearchLogRepository searchLogRepository;

    @Async
    @Transactional
    public void log(String query, String filters, long resultCount, Long userId) {
        if (query == null || query.isBlank()) {
            return;
        }
        searchLogRepository.save(SearchLog.builder()
                .query(query.trim())
                .filters(filters)
                .resultCount(resultCount)
                .userId(userId)
                .searchedAt(OffsetDateTime.now())
                .build());
    }
}
