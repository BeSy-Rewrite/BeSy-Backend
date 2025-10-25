package de.hs_esslingen.besy.interfaces;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;

import java.io.IOException;

@Getter @Setter @AllArgsConstructor
public class PDFItem {
    PDField position;
    PDField description;
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
