package de.hs_esslingen.besy.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.hs_esslingen.besy.dtos.response.InvoiceResponseDTO;
import de.hs_esslingen.besy.exceptions.StatusNotSuccessException;
import de.hs_esslingen.besy.interfaces.paperless.Task;
import de.hs_esslingen.besy.mappers.response.InvoiceResponseMapper;
import de.hs_esslingen.besy.models.Invoice;
import de.hs_esslingen.besy.repositories.InvoiceRepository;
import org.apache.coyote.BadRequestException;
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
    private final InvoiceResponseMapper invoiceResponseMapper;

    @Value("${paperless.api.base-url}")
    private String paperlessBaseUrl;

    @Value("${paperless.api.upload-url}")
    private String paperlessUploadUrl;

    @Value("${paperless.api.download-url}")
    private String paperlessDownloadUrl;

    @Value("${paperless.api.token}")
    private String authToken;

    public PaperlessService(InvoiceRepository invoiceRepository, PaperlessRetryService retryService, ObjectMapper mapper, InvoiceResponseMapper invoiceResponseMapper) {
        this.invoiceRepository = invoiceRepository;
        this.retryService = retryService;
        this.invoiceResponseMapper = invoiceResponseMapper;
    }


    public ResponseEntity<InvoiceResponseDTO> uploadPdfToPaperless(MultipartFile file, String invoiceId) throws IOException, ParseException {

        Invoice invoice = invoiceRepository.findById(invoiceId).get();

        if(invoice.getPaperlessId() != null) throw new BadRequestException("Diese Rechnung besitzt bereits ein verknüpftes Dokument mit der id: " + invoice.getPaperlessId());

        // Upload the pdf to paperless
        String uuid = uploadDocument(file);
        if (uuid == null) throw new RuntimeException("Fehler beim Hochladen des Dokumentes.");

        // Check task's status and retry if necessary
        Long documentId = Long.parseLong(retryService.getDocumentIdWithRetry(uuid));

        if(documentId == null) throw new RuntimeException("Fehler beim Hochladen des Dokumentes.");
        invoice.setPaperlessId(documentId);
        Invoice savedInvoice = invoiceRepository.save(invoice);

        return ResponseEntity.ok(invoiceResponseMapper.toDto(savedInvoice));
    }


    public ResponseEntity<byte[]> getPdfOfInvoice(String invoiceId) throws IOException {
        Invoice invoice = invoiceRepository.findById(invoiceId).get();
        Long paperlessId = invoice.getPaperlessId();

        try (CloseableHttpClient client = HttpClients.createDefault()) {

            HttpGet get = new HttpGet(paperlessBaseUrl + paperlessDownloadUrl.replace("{id}", String.valueOf(paperlessId)));
            get.setHeader("Authorization", "Bearer " + authToken);

            try (CloseableHttpResponse response = client.execute(get)) {
                if (response.getCode() == 200) {
                    return ResponseEntity.ok(response.getEntity().getContent().readAllBytes());
                } else {
                    throw new RuntimeException("Fehler beim Herunterladen der PDF.");
                }
            }
        }
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
