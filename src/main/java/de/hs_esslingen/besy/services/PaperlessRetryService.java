package de.hs_esslingen.besy.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.hs_esslingen.besy.exceptions.StatusNotSuccessException;
import de.hs_esslingen.besy.interfaces.paperless.Task;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;


/**
 * Service responsible for retrying the retrieval of the processed document ID from Paperless.
 * <p>
 * This class is separated from {@link PaperlessService} to allow Spring's {@link org.springframework.retry.annotation.Retryable}
 * to function correctly. Spring AOP-based retry mechanisms only work when the retryable method is invoked through a
 * Spring-managed proxy (i.e., from another bean), not through internal method calls within the same class.
 */


@EnableRetry
@Service
public class PaperlessRetryService {

    private final static int MAX_RETRIES = 6;

    private final static int MAX_DELAY = 5000;

    @Value("${paperless.api.base-url}")
    private String paperlessBaseUrl;

    @Value("${paperless.api.upload-url}")
    private String paperlessUploadUrl;

    @Value("${paperless.api-task-url}")
    private String paperlessTaskUrl;

    @Value("${paperless.api.token}")
    private String authToken;

    private final ObjectMapper mapper;

    public PaperlessRetryService(ObjectMapper mapper) {
        this.mapper = mapper;
    }


    @Retryable(maxAttempts = MAX_RETRIES, backoff = @Backoff(delay = MAX_DELAY), retryFor = StatusNotSuccessException.class)
    public String getDocumentIdWithRetry(String uuid) throws IOException, ParseException {

        try(CloseableHttpClient client = HttpClients.createDefault()) {

            HttpGet get = new HttpGet(paperlessBaseUrl + paperlessTaskUrl + "?task_id=" + uuid);
            get.addHeader("Authorization", "Token " + authToken);

            try(CloseableHttpResponse response = client.execute(get)) {
                if (response.getCode() != 200) throw new RuntimeException("Interner Serverfehler.");

                String responseBody = EntityUtils.toString(response.getEntity());
                List<Task> tasks = mapper.readValue(responseBody, new TypeReference<List<Task>>() {});

                if(tasks.size() != 1) throw new RuntimeException("Interner Serverfehler.");

                Task task = tasks.get(0);

                if(!task.getStatus().equals("SUCCESS")) throw new StatusNotSuccessException("Dokument noch nicht verarbeitet.");

                return task.getRelated_document();
            }
        }
    }

}
