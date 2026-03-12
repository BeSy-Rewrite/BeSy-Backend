package de.hs_esslingen.besy.interfaces;

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

    public void setPosition(String pos) throws IOException {
        this.position.setValue(pos);
    }

    public void setDescription(String desc) throws IOException {
        this.description.setValue(desc);
    }

    public void setQuantity(String quantity) throws IOException {
        this.quantity.setValue(quantity);
    }

    public void setPrice(String price) throws IOException {
        this.price.setValue(price);
    }

    public void setAmount(String amount) throws IOException {
        this.amount.setValue(amount);
    }
}
