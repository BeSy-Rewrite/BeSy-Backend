package de.hs_esslingen.besy.pdf;

import java.io.IOException;
import java.io.InputStream;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import de.hs_esslingen.besy.exceptions.PdfTemplateNotFoundException;
import lombok.RequiredArgsConstructor;

/**
 * Loads the AcroForm template from the classpath. Reading the full byte array
 * before handing it to PDFBox is kept deliberately: it works inside a jar,
 * where random access on a classpath stream does not.
 *
 * <p>
 * The caller owns the returned document and must close it. The
 * {@link PdfTemplateNotFoundException} is a {@code FileNotFoundException}
 * subclass and carries the same message as before, so the signature and the
 * observable failure stay unchanged.
 */
@Component
@RequiredArgsConstructor
public class PdfTemplateLoader {

    private final OrderPdfProperties properties;

    public PDDocument loadOrderTemplate() throws IOException {
        String location = properties.getTemplateLocation();
        ClassPathResource resource = new ClassPathResource(location);
        if (!resource.exists()) {
            throw new PdfTemplateNotFoundException(location);
        }
        try (InputStream pdfStream = resource.getInputStream()) {
            return Loader.loadPDF(pdfStream.readAllBytes());
        }
    }
}
