package de.hs_esslingen.besy.controllers;

import de.hs_esslingen.besy.interfaces.PDFItem;
import de.hs_esslingen.besy.interfaces.PDFOrder;
import lombok.AllArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.*;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.apache.pdfbox.Loader;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

@RestController
@AllArgsConstructor
@RequestMapping("${api.prefix}/pdf")
public class OrderFormController {

    static final String FORMULAR_URI = "src/main/resources/static/Bestellformular_V01_empty.pdf";

    @PostMapping
    public ResponseEntity<byte[]> createOrderForm() throws IOException {
        PDDocument document = Loader.loadPDF(new File(FORMULAR_URI));
        PDAcroForm acroForm = document.getDocumentCatalog().getAcroForm();

        PDFOrder order = new PDFOrder();
        order.parseOrder(acroForm);
        order.setConstructionAndAssemblyFlag(true);
        order.setDeliveryAndServiceFlag(true);
        order.setCompanyAddress("""
                notebooksbilliger.de AG
                Wiedemannstraße 3
                D-31157 Sarstedt
                """);
        order.setOrderId("ASDN84132");
        order.setDate("06.08.2025");
        order.setOrderer("Sandro L.");
        order.setPhone("+49 8427834687");
        order.setMobilePhone("+078134141234");
        order.setDeliveryFaculty("IT");
        order.setDeliveryOrderer("Sandro Lipinski");
        order.setDeliveryStreet("Flandernstraße 101");
        order.setDeliveryAddress("Esslingen");
        order.setInvoiceStreet("73728, Flandernstraße 101");

        order.setVat("9");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        document.save(baos);
        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_PDF)
                .body(baos.toByteArray());
    }
}
