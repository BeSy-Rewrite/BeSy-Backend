package de.hs_esslingen.besy.controllers;

import de.hs_esslingen.besy.interfaces.PDFItem;
import lombok.AllArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.*;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.apache.pdfbox.Loader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("${api.prefix}/pdf")
public class OrderFormController {

    @PostMapping
    public ResponseEntity<byte[]> createOrderForm(@RequestParam("file") MultipartFile file) throws IOException {
        PDDocument document = Loader.loadPDF(file.getBytes());
        PDAcroForm acroForm = document.getDocumentCatalog().getAcroForm();
       

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        document.save(baos);
        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_PDF)
                .body(baos.toByteArray());
    }
}
