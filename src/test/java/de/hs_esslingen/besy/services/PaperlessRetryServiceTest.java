package de.hs_esslingen.besy.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.hs_esslingen.besy.exceptions.PaperlessException;
import de.hs_esslingen.besy.exceptions.StatusNotSuccessException;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@Disabled
@SpringBootTest(classes = {PaperlessRetryService.class, PaperlessRetryServiceTest.RetryTestConfig.class})
@TestPropertySource(properties = {
        "paperless.api.base-url=http://localhost",
        "paperless.api.upload-url=/upload",
        "paperless.api-task-url=/tasks",
        "paperless.api.token=test-token",
        "retry.maxAttempts=3",
        "retry.maxDelay=0"
})
class PaperlessRetryServiceTest {

    @Configuration
    @EnableRetry
    static class RetryTestConfig {
        @Bean
        ObjectMapper objectMapper() {
            return new ObjectMapper();
        }
    }

    @Autowired
    private PaperlessRetryService paperlessRetryService;

    @SpyBean
    private PaperlessRetryService spyPaperlessRetryService;

    @Test
    void should_return_document_id_without_retry() throws Exception {
        String uuid = "uuid-1";
        doReturn("DOC-1").when(spyPaperlessRetryService).getDocumentIdWithRetry(uuid);

        String result = paperlessRetryService.getDocumentIdWithRetry(uuid);

        assertEquals("DOC-1", result);
        verify(spyPaperlessRetryService, times(1)).getDocumentIdWithRetry(uuid);
        verify(spyPaperlessRetryService, never()).recover(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void should_return_document_id_after_retries() throws Exception {
        String uuid = "uuid-2";
        int attempts = 3;

        doThrow(new StatusNotSuccessException("not ready"))
                .doThrow(new StatusNotSuccessException("not ready"))
                .doReturn("DOC-2")
                .when(spyPaperlessRetryService).getDocumentIdWithRetry(uuid);

        String result = paperlessRetryService.getDocumentIdWithRetry(uuid);

        assertEquals("DOC-2", result);
        verify(spyPaperlessRetryService, times(attempts)).getDocumentIdWithRetry(uuid);
    }

    @Test
    void should_recover_after_max_retries() throws Exception {
        String uuid = "uuid-3";

        doThrow(new StatusNotSuccessException("not ready"))
                .when(spyPaperlessRetryService).getDocumentIdWithRetry(uuid);

        PaperlessException ex = assertThrows(PaperlessException.class,
                () -> paperlessRetryService.getDocumentIdWithRetry(uuid));

        assertEquals("Fehler beim Hochladen des Dokumentes.", ex.getMessage());
        verify(spyPaperlessRetryService, times(1)).recover(org.mockito.ArgumentMatchers.any());
        verify(spyPaperlessRetryService, times(3)).getDocumentIdWithRetry(uuid);
    }

    @Nested
    @SpringBootTest(classes = {PaperlessRetryService.class, RetryTestConfig.class})
    @TestPropertySource(properties = {
            "paperless.api.base-url=http://localhost",
            "paperless.api.upload-url=/upload",
            "paperless.api-task-url=/tasks",
            "paperless.api.token=test-token",
            "retry.maxAttempts=5",
            "retry.maxDelay=0"
    })
    @DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
    class RetryConfigOverrideTest {

        @Autowired
        private PaperlessRetryService paperlessRetryService;

        @SpyBean
        private PaperlessRetryService spyPaperlessRetryService;

        @Test
        void should_respect_overridden_retry_max_attempts() throws Exception {
            String uuid = "uuid-4";

            doThrow(new StatusNotSuccessException("not ready"))
                    .when(spyPaperlessRetryService).getDocumentIdWithRetry(uuid);

            assertThrows(PaperlessException.class,
                    () -> paperlessRetryService.getDocumentIdWithRetry(uuid));

            verify(spyPaperlessRetryService, times(5)).getDocumentIdWithRetry(uuid);
        }
    }
}
