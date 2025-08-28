package de.hs_esslingen.besy.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.hs_esslingen.besy.interfaces.paperless.Task;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.List;


@Slf4j
@RestController
@RequestMapping("${api.prefix}/paperless")
public class PaperlessController {

    @Value("${paperless.upload.max-attempts}")
    private int MAX_RETRIES;

    @Value("${paperless.upload.max-delay}")
    private int MAX_DELAY;

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

    public PaperlessController(ObjectMapper mapper) {
        this.mapper = mapper;
    }


    @PostMapping
    public ResponseEntity<String> uploadPDF(@RequestParam("file") MultipartFile file) throws IOException, ParseException {

        String paperlessUrl = paperlessBaseUrl + paperlessUploadUrl;
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(paperlessUrl);
            post.addHeader("Authorization", "Token " + authToken);

            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.addBinaryBody("document", file.getBytes(), ContentType.APPLICATION_PDF, file.getOriginalFilename());

            post.setEntity(builder.build());

            try (CloseableHttpResponse response = client.execute(post)) {
                String uuid = EntityUtils.toString(response.getEntity()).replaceAll("\"", "");

                if(response.getCode() == 200) {

                        int retries = 0;
                        String status = "";
                        String documentId = "";

                        while (!status.equals("SUCCESS") && retries < MAX_RETRIES){
                            HttpGet getTask = new HttpGet(paperlessBaseUrl + paperlessTaskUrl + "?task_id=" + uuid);
                            getTask.addHeader("Authorization", "Token " + authToken);

                            try(CloseableHttpResponse taskResponse = client.execute(getTask)) {
                                String responseBody = EntityUtils.toString(taskResponse.getEntity());
                                List<Task> task = mapper.readValue(responseBody, new TypeReference<List<Task>>() {});

                                documentId = task.get(0).getRelated_document();
                                status = task.get(0).getStatus();
                                retries++;
                                Thread.sleep(MAX_DELAY);

                            } catch (InterruptedException e) {
                                log.atError().log("Fehler beim Schlafenlegen des Threads (PaperlessController - uploadPdf()) " + e.getMessage());
                                throw new RuntimeException("Interner Serverfehler.");
                            }
                        }

                        System.out.println("Status : " + status);
                    System.out.println("DocumentId : " + documentId);

                        if(status.equals("SUCCESS")) {
                            System.out.println(documentId);
                        } else{
                            log.atError().log("Fehler beim Abrufen der Dokumenten-ID (PaperlessController - uploadPdf())");
                            throw new RuntimeException("Interner Serverfehler.");
                        }
                }
            }
        }
        return ResponseEntity.ok().build();
    }

}
