package de.hs_esslingen.besy.interfaces;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDCheckBox;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.pdmodel.interactive.form.PDFieldTree;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PDFOrder {

    private PDCheckBox constructionAndAssemblyFlag;
    private PDCheckBox deliveryAndServiceFlag;
    private PDField companyAddress;
    private PDField orderId;
    private PDField date;
    private PDField orderer;
    private PDField phone;
    private PDField mobilePhone;
    private PDField email;
    private PDField deliveryFaculty;
    private PDField deliveryOrderer;
    private PDField deliveryStreet;
    private PDField deliveryAddress;
    private PDField invoiceFaculty;
    private PDField invoiceOrderer;
    private PDField invoiceStreet;
    private PDField invoiceDeliveryAddress;
    private List<PDFItem> articles = new ArrayList<>();
    private PDField percentageDiscount;
    private PDField vat;
    private PDField commentForSupplier;
    private PDField costCenter;
    private PDField costCenterSecondary;
    private PDField dfgKey;

    // ToDo: Vergleichsangebote

    private PDField customerId;
    private PDCheckBox flagDecisionCheapestOffer;
    // ToDo: wirtschaftlichstes Angebot
    private PDCheckBox flagDecisionSoleSupplier;
    private PDCheckBox flagDecisionContractPartner;
    // ToDo: in der Vorzugsliste RZ (EDV) oder FM (Möbel) enthalten ist.
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
        PDFOrder order = new PDFOrder();
        PDFieldTree fieldTree = acroForm.getFieldTree();

        order.constructionAndAssemblyFlag = (PDCheckBox) acroForm.getField("Formular1[0].#subform[0].Header[0].Kontrollkästchen1[0]");
        order.deliveryAndServiceFlag = (PDCheckBox) acroForm.getField("Formular1[0].#subform[0].Kontrollkästchen1[1]");
        order.companyAddress = acroForm.getField("Formular1[0].#subform[0].Header[0].Textfeld1[0]");
        order.orderId = acroForm.getField("Formular1[0].#subform[0].Header[0].Rechnungsnummer[0]");
        order.date = acroForm.getField("Formular1[0].#subform[0].Header[0].Rechnungsdatum[0]");
        order.orderer = acroForm.getField("Formular1[0].#subform[0].Header[0].Firma[1]");
        order.phone = acroForm.getField("Formular1[0].#subform[0].Header[0].Telefon[1]");
        order.mobilePhone = acroForm.getField("Formular1[0].#subform[0].Header[0].Fax[1]");
        order.email = acroForm.getField("Formular1[0].#subform[0].Header[0].Postleitzahl[0]");
        order.deliveryFaculty = acroForm.getField("Formular1[0].#subform[0].Header[0].Firma[0]");
        order.deliveryOrderer = acroForm.getField("Formular1[0].#subform[0].Header[0].Telefon[3]");
        order.deliveryStreet = acroForm.getField("Formular1[0].#subform[0].Header[0].Telefon[0]");
        order.deliveryAddress = acroForm.getField("Formular1[0].#subform[0].Header[0].Fax[0]");
        order.invoiceFaculty = acroForm.getField("Formular1[0].#subform[0].Header[0].Firma[2]");
        order.invoiceOrderer = acroForm.getField("Formular1[0].#subform[0].Header[0].Telefon[4]");
        order.invoiceStreet = acroForm.getField("Formular1[0].#subform[0].Header[0].Telefon[2]");
        order.invoiceDeliveryAddress = acroForm.getField("Formular1[0].#subform[0].Header[0].Fax[2]");

        for(int i = 0; i < 14; i++){
            PDFItem article = new PDFItem();
            article.setPosition(acroForm.getField(String.format("Formular1[0].#subform[0].Body[0].Artikel[%d]", i)));
            article.setDescription(acroForm.getField(String.format("Formular1[0].#subform[0].Body[0].Beschreibung[%d]", i)));
            article.setQuantity(acroForm.getField(String.format("Formular1[0].#subform[0].Body[0].Menge[%d]", i)));
            article.setPrice(acroForm.getField(String.format("Formular1[0].#subform[0].Body[0].Stückpreis[%d]", i)));
            order.articles.add(article);
        }

        order.percentageDiscount = acroForm.getField("Formular1[0].#subform[0].Body[0].RabattText[0]");
        order.vat = acroForm.getField("Formular1[0].#subform[0].Body[0].MwStSatz[0]");
        order.commentForSupplier = acroForm.getField("Formular1[0].#subform[0].Body[0].Textfeld1[1]");
        order.costCenter = acroForm.getField("Formular1[0].#subform[1].Textfeld1[2]");
        order.costCenterSecondary = acroForm.getField("Formular1[0].#subform[1].Textfeld1[4]");
        order.dfgKey = acroForm.getField("Formular1[0].#subform[1].Textfeld1[3]");

        // ToDo: Vergleichsangebote

        order.customerId = (PDCheckBox) acroForm.getField("Formular1[0].#subform[1].Textfeld4[0]");
        order.flagDecisionCheapestOffer = (PDCheckBox) acroForm.getField("Formular1[0].#subform[1].Kontrollkästchen1[2]");
        // ToDo: wirtschaftlichstes Angebot
        order.flagDecisionSoleSupplier = (PDCheckBox) acroForm.getField("Formular1[0].#subform[1].Kontrollkästchen1[4]");
        order.flagDecisionContractPartner = (PDCheckBox) acroForm.getField("Formular1[0].#subform[1].Kontrollkästchen1[5]");
        // ToDo: in der Vorzugsliste RZ (EDV) oder FM (Möbel) enthalten ist.
        order.flagDecisionOtherReasons = (PDCheckBox) acroForm.getField("Formular1[0].#subform[1].Kontrollkästchen1[7]");
        order.flagDecisionOtherReasonsDescription = acroForm.getField("Formular1[0].#subform[1].Textfeld5[0]");

        // 4. Zustimmung bei Bestellung von DV-Komponenten (Hardware/ Software)
        order.orderFlagEdvPermission = (PDCheckBox) acroForm.getField("Formular1[0].#subform[1].Kontrollkästchen1[8]");

        // 5. Zustimmung bei Bestellung von Möbeln
        order.orderFlagFurniturePermission = (PDCheckBox) acroForm.getField("Formular1[0].#subform[1].Kontrollkästchen1[9]");
        // 2nd flag
        order.orderFlagFurnitureRoom = (PDCheckBox) acroForm.getField("Formular1[0].#subform[1].Kontrollkästchen1[10]");

        // 6. Zustimmung bei der Bestellung von Geräten (baulich-infrastrukturell relevant
        order.orderFlagInvestmentRoom = (PDCheckBox) acroForm.getField("Formular1[0].#subform[1].Kontrollkästchen1[11]");
        // 2nd flag
        order.orderFlagInvestmentStructuralMeasures = (PDCheckBox) acroForm.getField("Formular1[0].#subform[1].Kontrollkästchen1[12]");

        // 7. Zustimmung bei Bestellung von medientechnischen Einrichtungen und Geräten:
        order.orderFlagMediaPermission = (PDCheckBox) acroForm.getField("Formular1[0].#subform[1].Kontrollkästchen1[13]");

        return order;
    }

    public void setConstructionAndAssemblyFlag(boolean flag) throws IOException {
        if (flag) {
            this.constructionAndAssemblyFlag.check();
        } else {
            this.constructionAndAssemblyFlag.unCheck();
        }
    }

    private void  setDeliveryAndServiceFlag(boolean flag) throws IOException {
        if (flag) {
            this.deliveryAndServiceFlag.check();
        }else {
            this.deliveryAndServiceFlag.unCheck();
        }
    }

    private void setCompanyAddress(String address) throws IOException {
        this.companyAddress.setValue(address);
    }

    public void setOrderId(String orderId) throws IOException {
        this.orderId.setValue(orderId);
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

    public void setArticles(List<PDFItem> items) throws IOException {
        if(items.size() < 14){
            this.articles.addAll(items);
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

    public void setCustomerId(String customerId) throws IOException {
        this.customerId.setValue(customerId);
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
