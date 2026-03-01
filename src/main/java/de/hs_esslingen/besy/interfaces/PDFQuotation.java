package de.hs_esslingen.besy.interfaces;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;

import java.io.IOException;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PDFQuotation {
    Integer index;
    PDField companyName;
    PDField date;
    PDField price;

    public void setIndex(Integer index) throws IOException {
        this.index = index;
    }

    public void setCompanyName(String companyName) throws IOException {
        if (this.companyName != null) this.companyName.setValue(companyName != null ? companyName : "");
    }

    public void setDate(String date) throws IOException {
        if (this.date != null) this.date.setValue(date != null ? date : "");
    }

    public void setPrice(String price) throws IOException {
        if (this.price != null) this.price.setValue(price != null ? price : "");
    }
}
