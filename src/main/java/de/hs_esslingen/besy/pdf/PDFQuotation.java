package de.hs_esslingen.besy.pdf;

import java.io.IOException;

import org.apache.pdfbox.pdmodel.interactive.form.PDField;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(force = true)
public class PDFQuotation {
    Integer index;
    PDField companyName;
    PDField date;
    PDField price;
    private final PdfSafeFieldWriter fieldWriter;

    public void setIndex(Integer index) throws IOException {
        this.index = index;
    }

    public void setCompanyName(String companyName) throws IOException {
        fieldWriter.setValue(this.companyName, companyName);
    }

    public void setDate(String date) throws IOException {
        fieldWriter.setValue(this.date, date);
    }

    public void setPrice(String price) throws IOException {
        fieldWriter.setValue(this.price, price);
    }
}
