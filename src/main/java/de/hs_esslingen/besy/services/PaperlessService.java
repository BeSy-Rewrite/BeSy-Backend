package de.hs_esslingen.besy.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.hs_esslingen.besy.exceptions.StatusNotSuccessException;
import de.hs_esslingen.besy.interfaces.paperless.Task;
import de.hs_esslingen.besy.models.Invoice;
import de.hs_esslingen.besy.repositories.InvoiceRepository;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@EnableRetry
public class PaperlessService {

    private final InvoiceRepository invoiceRepository;
    private final PaperlessRetryService retryService;

    @Value("${paperless.api.base-url}")
    private String paperlessBaseUrl;

    @Value("${paperless.api.upload-url}")
    private String paperlessUploadUrl;

    @Value("${paperless.api-task-url}")
    private String paperlessTaskUrl;

    @Value("${paperless.api.token}")
    private String authToken;

    @Autowired
    private final ObjectMapper mapper;

    public PaperlessService(InvoiceRepository invoiceRepository, PaperlessRetryService retryService, ObjectMapper mapper) {
        this.invoiceRepository = invoiceRepository;
        this.retryService = retryService;
        this.mapper = mapper;
    }


    public ResponseEntity<Long> uploadPdfToPaperless(MultipartFile file, String invoiceId) throws IOException, ParseException {

        Invoice invoice = invoiceRepository.findById(invoiceId).get();

        // Upload the pdf to paperless
        String uuid = uploadDocument(file);
        if (uuid == null) throw new RuntimeException("Fehler beim Hochladen des Dokumentes.");

        // Check task's status and retry if necessary
        Long documentId = Long.parseLong(retryService.getDocumentIdWithRetry(uuid));

        if(documentId == null) throw new RuntimeException("Fehler beim Hochladen des Dokumentes.");
        invoice.setPaperlessId(documentId);
        return ResponseEntity.ok(documentId);
    }



    private String uploadDocument(MultipartFile file) throws IOException, ParseException {

        try (CloseableHttpClient client = HttpClients.createDefault()) {

            HttpPost post = new HttpPost(paperlessBaseUrl + paperlessUploadUrl);
            post.addHeader("Authorization", "Token " + authToken);
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.addBinaryBody("document", file.getBytes(), ContentType.APPLICATION_PDF, file.getOriginalFilename());
            post.setEntity(builder.build());

            try (CloseableHttpResponse response = client.execute(post)) {
                if (response.getCode() == 200) {
                    return EntityUtils.toString(response.getEntity()).replaceAll("\"", "");
                } else {
                    return null;
                }
            }
        }
    }



}
