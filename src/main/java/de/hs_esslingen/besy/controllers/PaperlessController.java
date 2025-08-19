package de.hs_esslingen.besy.controllers;

import org.apache.commons.io.IOUtils;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;


@RestController
@RequestMapping("${api.prefix}/paperless")
public class PaperlessController {

    @Value("${paperless.api.base-url}")
    private String paperlessBaseUrl;

    @Value("${paperless.api.upload-url}")
    private String paperlessUploadUrl;

    @Value("${paperless.api.token}")
    private String authToken;


    @PostMapping
    public String uploadPDF(@RequestParam("file") MultipartFile file) throws IOException, ParseException {
        // Save MultipartFile contents to a temp file
        File tempFile = File.createTempFile("upload-", ".pdf");
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(file.getBytes());
        }

        String paperlessUrl = "http://localhost:8000/api/documents/post_document/";
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(paperlessUrl);
            post.addHeader("Authorization", "Token " + authToken);

            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.addBinaryBody("document", tempFile, ContentType.APPLICATION_PDF, file.getOriginalFilename());

            post.setEntity(builder.build());

            try (CloseableHttpResponse response = client.execute(post)) {
                String responseString = EntityUtils.toString(response.getEntity());
                if (response.getCode() == 200 || response.getCode() == 201) {
                    return "Upload successful: " + responseString;
                } else {
                    return "Upload failed: " + response.getCode() + " - " + responseString;
                }
            }
        } finally {
            tempFile.delete(); // Clean up the temp file
        }
    }

}
