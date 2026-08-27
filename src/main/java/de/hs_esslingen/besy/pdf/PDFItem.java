package de.hs_esslingen.besy.pdf;

import java.io.IOException;

import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.pdmodel.interactive.form.PDTextField;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PDFItem {
    PDField position;
    PDTextField description;
    PDField quantity;
    PDField price;
    PDField amount;
    private final PdfSafeFieldWriter fieldWriter;

    public void setPosition(String pos) throws IOException {
        fieldWriter.setValue(this.position, pos);
    }

    public void setDescription(String desc) throws IOException {
        fieldWriter.setValue(this.description, desc);
    }

    public void setQuantity(String quantity) throws IOException {
        fieldWriter.setValue(this.quantity, quantity);
    }

    public void setPrice(String price) throws IOException {
        fieldWriter.setValue(this.price, price);
    }

    public void setAmount(String amount) throws IOException {
        fieldWriter.setValue(this.amount, amount);
    }
}
