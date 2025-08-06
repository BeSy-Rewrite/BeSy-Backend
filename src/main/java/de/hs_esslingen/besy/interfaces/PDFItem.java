package de.hs_esslingen.besy.interfaces;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;

@Getter @Setter @NoArgsConstructor
public class PDFItem {
    PDField position;
    PDField description;
    PDField quantity;
    PDField price;
}
