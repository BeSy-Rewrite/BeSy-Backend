package de.hs_esslingen.besy.interfaces;

import de.hs_esslingen.besy.dtos.response.ItemResponseDTO;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDCheckBox;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PDFOrder {

    // nach VOB (Bau-/Montageleistung)
    private PDCheckBox constructionAndAssemblyFlag;

    // nach VOL/UVgO (Liefer-/Dienstleistung)
    private PDCheckBox deliveryAndServiceFlag;

    // an Firma mit Anschrift:
    private PDField companyAddress;

    // Bestell-Nr
    private PDField orderNumber;

    // Datum:
    private PDField date;

    // Besteller:in
    private PDField orderer;

    // Telefon:
    private PDField phone;

    // Mobil-Nr.:
    private PDField mobilePhone;

    // E-Mail:
    private PDField email;

    // Fax-Nr./E-Mail:
    private PDField supplierEmail;

    // Angebots-Nr.:
    private PDField invoiceId;

    // Lieferanschrift: Fakultät/Bereich:
    private PDField deliveryFaculty;

    // Lieferanschrift: Besteller:in/Name:
    private PDField deliveryOrderer;

    // Lieferanschrift: Straße:
    private PDField deliveryStreet;

    // Lieferanschrift: PLZ und Ort:
    private PDField deliveryAddress;

    // Rechnungsanschrift: Fakultät/Bereich:
    private PDField invoiceFaculty;

    // Rechnungsanschrift: Besteller:in/Name:
    private PDField invoiceOrderer;

    // Rechnungsanschrift: Straße:
    private PDField invoiceStreet;

    // Rechnungsanschrift: PLZ und Ort:
    private PDField invoiceDeliveryAddress;

    // Artikel
    private List<PDFItem> items = new ArrayList<>();

    // % Rabatt
    private PDField percentageDiscount;

    // % MwSt
    private PDField vat;

    // Bemerkung:
    private PDField commentForSupplier;

    // Kostenstelle:
    private PDField costCenter;

    // anteilig auch:
    private PDField costCenterSecondary;

    // DFG-Schlüssel
    private PDField dfgKey;

    // ToDo: Vergleichsangebote

    // Der Auftrag wird der oben unter der lfd.Nr. genannten Firma erteilt, da diese Firma...
    private PDField lfdNr;

    // das preisgünstigste Angebot abgegeben hat
    private PDCheckBox flagDecisionCheapestOffer;

    // das wirtschaftlichste Angebot abgegeben hat
    // ToDo: wirtschaftlichstes Angebot

    // Einziger Anbieter am Markt ist.
    private PDCheckBox flagDecisionSoleSupplier;

    // Rahmenvertragspartner ist. Der Rahmenvertrag liegt der FIN vor.
    private PDCheckBox flagDecisionContractPartner;

    // in der Vorzugsliste RZ (EDV) oder FM (Möbel) enthalten ist.
    // ToDo: in der Vorzugsliste RZ (EDV) oder FM (Möbel) enthalten ist.

    // aus folgendem Sachgrund ausgewählt wurde:
    private PDCheckBox flagDecisionOtherReasons;
    private PDField flagDecisionOtherReasonsDescription;

    // 4. Zustimmung bei Bestellung von DV-Komponenten (Hardware/ Software)
    private PDCheckBox orderFlagEdvPermission;

    // 5. Zustimmung bei Bestellung von Möbeln
    private PDCheckBox orderFlagFurniturePermission;
    // 2nd flag
    private PDCheckBox orderFlagFurnitureRoom;

    // 6. Zustimmung bei der Bestellung von Geräten (baulich-infrastrukturell relevant
    private PDCheckBox orderFlagInvestmentRoom;
    // 2nd flag
    private PDCheckBox orderFlagInvestmentStructuralMeasures;

    // 7. Zustimmung bei Bestellung von medientechnischen Einrichtungen und Geräten:
    private PDCheckBox orderFlagMediaPermission;


    public PDFOrder parseOrder(PDAcroForm acroForm) {
        constructionAndAssemblyFlag = (PDCheckBox) acroForm.getField("Formular1[0].#subform[0].Header[0].Kontrollkästchen1[0]");
        deliveryAndServiceFlag = (PDCheckBox) acroForm.getField("Formular1[0].#subform[0].Kontrollkästchen1[1]");
        orderNumber = acroForm.getField("Formular1[0].#subform[0].Header[0].Rechnungsnummer[0]");
        companyAddress = acroForm.getField("Formular1[0].#subform[0].Header[0].Textfeld1[0]");
        supplierEmail = acroForm.getField("Formular1[0].#subform[0].Header[0].Fax[2]");
        invoiceId = acroForm.getField("Formular1[0].#subform[0].Body[0].Zwischensumme[0]");
        date = acroForm.getField("Formular1[0].#subform[0].Header[0].Rechnungsdatum[0]");
        orderer = acroForm.getField("Formular1[0].#subform[0].Header[0].Firma[1]");
        phone = acroForm.getField("Formular1[0].#subform[0].Header[0].Telefon[1]");
        mobilePhone = acroForm.getField("Formular1[0].#subform[0].Header[0].Fax[1]");
        email = acroForm.getField("Formular1[0].#subform[0].Header[0].Postleitzahl[0]");
        deliveryFaculty = acroForm.getField("Formular1[0].#subform[0].Header[0].Firma[0]");
        deliveryOrderer = acroForm.getField("Formular1[0].#subform[0].Header[0].Telefon[3]");
        deliveryStreet = acroForm.getField("Formular1[0].#subform[0].Header[0].Telefon[0]");
        deliveryAddress = acroForm.getField("Formular1[0].#subform[0].Header[0].Fax[0]");
        invoiceFaculty = acroForm.getField("Formular1[0].#subform[0].Header[0].Firma[2]");
        invoiceOrderer = acroForm.getField("Formular1[0].#subform[0].Header[0].Telefon[4]");
        invoiceStreet = acroForm.getField("Formular1[0].#subform[0].Header[0].Telefon[2]");
        invoiceDeliveryAddress = acroForm.getField("Formular1[0].#subform[0].Header[0].Fax[2]");

        for(int i = 0; i < 14; i++){
            PDFItem article = new PDFItem(
                    acroForm.getField(String.format("Formular1[0].#subform[0].Body[0].Artikel[%d]", i)),
                    acroForm.getField(String.format("Formular1[0].#subform[0].Body[0].Beschreibung[%d]", i)),
                    acroForm.getField(String.format("Formular1[0].#subform[0].Body[0].Menge[%d]", i)),
                    acroForm.getField(String.format("Formular1[0].#subform[0].Body[0].Stückpreis[%d]", i))
            );
            items.add(article);
        }

        percentageDiscount = acroForm.getField("Formular1[0].#subform[0].Body[0].RabattText[0]");
        vat = acroForm.getField("Formular1[0].#subform[0].Body[0].MwStSatz[0]");
        commentForSupplier = acroForm.getField("Formular1[0].#subform[0].Body[0].Textfeld1[1]");
        costCenter = acroForm.getField("Formular1[0].#subform[1].Textfeld1[2]");
        costCenterSecondary = acroForm.getField("Formular1[0].#subform[1].Textfeld1[4]");
        dfgKey = acroForm.getField("Formular1[0].#subform[1].Textfeld1[3]");

        // ToDo: Vergleichsangebote

        lfdNr = acroForm.getField("Formular1[0].#subform[1].Textfeld4[0]");
        flagDecisionCheapestOffer = (PDCheckBox) acroForm.getField("Formular1[0].#subform[1].Kontrollkästchen1[2]");
        // ToDo: wirtschaftlichstes Angebot
        flagDecisionSoleSupplier = (PDCheckBox) acroForm.getField("Formular1[0].#subform[1].Kontrollkästchen1[4]");
        flagDecisionContractPartner = (PDCheckBox) acroForm.getField("Formular1[0].#subform[1].Kontrollkästchen1[5]");
        // ToDo: in der Vorzugsliste RZ (EDV) oder FM (Möbel) enthalten ist.
        flagDecisionOtherReasons = (PDCheckBox) acroForm.getField("Formular1[0].#subform[1].Kontrollkästchen1[7]");
        flagDecisionOtherReasonsDescription = acroForm.getField("Formular1[0].#subform[1].Textfeld5[0]");

        // 4. Zustimmung bei Bestellung von DV-Komponenten (Hardware/ Software)
        orderFlagEdvPermission = (PDCheckBox) acroForm.getField("Formular1[0].#subform[1].Kontrollkästchen1[8]");

        // 5. Zustimmung bei Bestellung von Möbeln
        orderFlagFurniturePermission = (PDCheckBox) acroForm.getField("Formular1[0].#subform[1].Kontrollkästchen1[9]");
        // 2nd flag
        orderFlagFurnitureRoom = (PDCheckBox) acroForm.getField("Formular1[0].#subform[1].Kontrollkästchen1[10]");

        // 6. Zustimmung bei der Bestellung von Geräten (baulich-infrastrukturell relevant
        orderFlagInvestmentRoom = (PDCheckBox) acroForm.getField("Formular1[0].#subform[1].Kontrollkästchen1[11]");
        // 2nd flag
        orderFlagInvestmentStructuralMeasures = (PDCheckBox) acroForm.getField("Formular1[0].#subform[1].Kontrollkästchen1[12]");

        // 7. Zustimmung bei Bestellung von medientechnischen Einrichtungen und Geräten:
        orderFlagMediaPermission = (PDCheckBox) acroForm.getField("Formular1[0].#subform[1].Kontrollkästchen1[13]");

        return this;
    }

    public void setConstructionAndAssemblyFlag(boolean flag) throws IOException {
        if (flag) {
            this.constructionAndAssemblyFlag.check();
        } else {
            this.constructionAndAssemblyFlag.unCheck();
        }
    }

    public void  setDeliveryAndServiceFlag(boolean flag) throws IOException {
        if (flag) {
            this.deliveryAndServiceFlag.check();
        }else {
            this.deliveryAndServiceFlag.unCheck();
        }
    }

    public void setCompanyAddress(String address) throws IOException {
        this.companyAddress.setValue(address);
    }

    public void setInvoiceId(String invoiceId) throws IOException {
        this.invoiceId.setValue(invoiceId);
    }

    public void setDate(String date) throws IOException {
        this.date.setValue(date);
    }

    public void setOrderer(String orderer) throws IOException {
        this.orderer.setValue(orderer);
    }

    public void setPhone(String phone) throws IOException {
        this.phone.setValue(phone);
    }

    public void setMobilePhone(String mobilePhone) throws IOException {
        this.mobilePhone.setValue(mobilePhone);
    }

    public void setEmail(String email) throws IOException {
        this.email.setValue(email);
    }

    public void setDeliveryFaculty(String deliveryFaculty) throws IOException {
        this.deliveryFaculty.setValue(deliveryFaculty);
    }

    public void setDeliveryOrderer(String deliveryOrderer) throws IOException {
        this.deliveryOrderer.setValue(deliveryOrderer);
    }

    public void setDeliveryStreet(String deliveryStreet) throws IOException {
        this.deliveryStreet.setValue(deliveryStreet);
    }

    public void setDeliveryAddress(String deliveryAddress) throws IOException {
        this.deliveryAddress.setValue(deliveryAddress);
    }

    public void setInvoiceFaculty(String invoiceFaculty) throws IOException {
        this.invoiceFaculty.setValue(invoiceFaculty);
    }

    public void setInvoiceOrderer(String invoiceOrderer) throws IOException {
        this.invoiceOrderer.setValue(invoiceOrderer);
    }

    public void setInvoiceStreet(String invoiceStreet) throws IOException {
        this.invoiceStreet.setValue(invoiceStreet);
    }

    public void setInvoiceDeliveryAddress(String invoiceDeliveryAddress) throws IOException {
        this.invoiceDeliveryAddress.setValue(invoiceDeliveryAddress);
    }

    public void setItems(List<ItemResponseDTO> items) throws IOException {
        if(items.size() < 14){
                for(int i = 0; i < items.size(); i++){
                    ItemResponseDTO itemDTO = items.get(i);
                    PDFItem pdfItem = this.items.get(i);
                    pdfItem.setPosition(String.valueOf(itemDTO.getItemId()));
                    pdfItem.setDescription(itemDTO.getName());
                    pdfItem.setQuantity(String.valueOf(itemDTO.getQuantity()));
                    pdfItem.setPrice(String.valueOf(itemDTO.getPricePerUnit()));
                }
    }else {
            throw new RuntimeException("Number of items must be less than 14.");
        }
    }

    public void setPercentageDiscount(String percentageDiscount) throws IOException {
        this.percentageDiscount.setValue(percentageDiscount);
    }

    public void setVat(String vat) throws IOException {
        this.vat.setValue(vat);
    }

    public void setCommentForSupplier(String commentForSupplier) throws IOException {
        this.commentForSupplier.setValue(commentForSupplier);
    }

    public void setCostCenter(String costCenter) throws IOException {
        this.costCenter.setValue(costCenter);
    }

    public void setCostCenterSecondary(String costCenterSecondary) throws IOException {
        this.costCenterSecondary.setValue(costCenterSecondary);
    }

    public void setDfgKey(String dfgKey) throws IOException {
        this.dfgKey.setValue(dfgKey);
    }

    public void setOrderNumber(String orderNumber) throws IOException {
        this.orderNumber.setValue(orderNumber);
    }

    public void setSupplierEmail(String supplierEmail) throws IOException {
        this.supplierEmail.setValue(supplierEmail);
    }

    public void setLfdNr(String lfdNr) throws IOException {
        this.lfdNr.setValue(lfdNr);
    }

    public void setFlagDecisionCheapestOffer(boolean flag) throws IOException {
        if (flag) {
            this.flagDecisionCheapestOffer.check();
        } else {
            this.flagDecisionCheapestOffer.unCheck();
        }
    }

    public void setFlagDecisionSoleSupplier(boolean flag) throws IOException {
        if (flag) {
            this.flagDecisionSoleSupplier.check();
        } else {
            this.flagDecisionSoleSupplier.unCheck();
        }
    }

    public void setFlagDecisionContractPartner(boolean flag) throws IOException {
        if (flag) {
            this.flagDecisionContractPartner.check();
        } else {
            this.flagDecisionContractPartner.unCheck();
        }
    }

    public void setFlagDecisionOtherReasons(boolean flag) throws IOException {
        if (flag) {
            this.flagDecisionOtherReasons.check();
        } else {
            this.flagDecisionOtherReasons.unCheck();
        }
    }

    public void setFlagDecisionOtherReasonsDescription(String description) throws IOException {
        this.flagDecisionOtherReasonsDescription.setValue(description);
    }

    public void setOrderFlagEdvPermission(boolean flag) throws IOException {
        if (flag) {
            this.orderFlagEdvPermission.check();
        } else {
            this.orderFlagEdvPermission.unCheck();
        }
    }

    public void setOrderFlagFurniturePermission(boolean flag) throws IOException {
        if (flag) {
            this.orderFlagFurniturePermission.check();
        } else {
            this.orderFlagFurniturePermission.unCheck();
        }
    }

    public void setOrderFlagFurnitureRoom(boolean flag) throws IOException {
        if (flag) {
            this.orderFlagFurnitureRoom.check();
        } else {
            this.orderFlagFurnitureRoom.unCheck();
        }
    }

    public void setOrderFlagInvestmentRoom(boolean flag) throws IOException {
        if (flag) {
            this.orderFlagInvestmentRoom.check();
        } else {
            this.orderFlagInvestmentRoom.unCheck();
        }
    }

    public void setOrderFlagInvestmentStructuralMeasures(boolean flag) throws IOException {
        if (flag) {
            this.orderFlagInvestmentStructuralMeasures.check();
        } else {
            this.orderFlagInvestmentStructuralMeasures.unCheck();
        }
    }

    public void setOrderFlagMediaPermission(boolean flag) throws IOException {
        if (flag) {
            this.orderFlagMediaPermission.check();
        } else {
            this.orderFlagMediaPermission.unCheck();
        }
    }


}
