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
        if (this.position != null) this.position.setValue(pos != null ? pos : "");
    }

    public void setDescription(String desc) throws IOException {
        if (this.description != null) this.description.setValue(desc != null ? desc : "");
    }

    public void setQuantity(String quantity) throws IOException {
        if (this.quantity != null) this.quantity.setValue(quantity != null ? quantity : "");
    }

    public void setPrice(String price) throws IOException {
        if (this.price != null) this.price.setValue(price != null ? price : "");
    }

    public void setAmount(String amount) throws IOException {
        if (this.amount != null) this.amount.setValue(amount != null ? amount : "");
    }
}
