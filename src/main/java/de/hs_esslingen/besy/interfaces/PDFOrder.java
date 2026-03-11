package de.hs_esslingen.besy.interfaces;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDCheckBox;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;

import de.hs_esslingen.besy.dtos.response.ItemResponseDTO;
import de.hs_esslingen.besy.enums.VatType;
import de.hs_esslingen.besy.models.Quotation;
import de.hs_esslingen.besy.services.PriceConversionService;

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

    // Zwischensumme
    private PDField subTotal;

    // Nettosumme
    private PDField netTotal;

    // Gesamtsumme
    private PDField total;

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

    private List<PDFQuotation> quotations = new ArrayList<>();

    // Der Auftrag wird der oben unter der lfd.Nr. genannten Firma erteilt, da diese
    // Firma...
    private PDField lfdNr;

    // das preisgünstigste Angebot abgegeben hat
    private PDCheckBox flagDecisionCheapestOffer;

    // das wirtschaftlichste Angebot abgegeben hat
    private PDCheckBox flagDecisionMostEconomicalOffer;

    // Einziger Anbieter am Markt ist.
    private PDCheckBox flagDecisionSoleSupplier;

    // Rahmenvertragspartner ist. Der Rahmenvertrag liegt der FIN vor.
    private PDCheckBox flagDecisionContractPartner;

    // in der Vorzugsliste RZ (EDV) oder FM (Möbel) enthalten ist.
    private PDCheckBox flagDecisionPreferredSupplierList;

    // aus folgendem Sachgrund ausgewählt wurde:
    private PDCheckBox flagDecisionOtherReasons;
    private PDField flagDecisionOtherReasonsDescription;

    // 4. Zustimmung bei Bestellung von DV-Komponenten (Hardware/ Software)
    private PDCheckBox orderFlagEdvPermission;

    // 5. Zustimmung bei Bestellung von Möbeln
    private PDCheckBox orderFlagFurniturePermission;
    // 2nd flag
    private PDCheckBox orderFlagFurnitureRoom;

    // 6. Zustimmung bei der Bestellung von Geräten (baulich-infrastrukturell
    // relevant
    private PDCheckBox orderFlagInvestmentRoom;
    // 2nd flag
    private PDCheckBox orderFlagInvestmentStructuralMeasures;

    // 7. Zustimmung bei Bestellung von medientechnischen Einrichtungen und Geräten:
    private PDCheckBox orderFlagMediaPermission;

    public PDFOrder parseOrder(PDAcroForm acroForm) {
        constructionAndAssemblyFlag = (PDCheckBox) acroForm
                .getField("Formular1[0].#subform[0].Header[0].Kontrollkästchen1[0]");
        deliveryAndServiceFlag = (PDCheckBox) acroForm.getField("Formular1[0].#subform[0].Kontrollkästchen1[1]");
        orderNumber = acroForm.getField("Formular1[0].#subform[0].Header[0].Rechnungsnummer[0]");
        companyAddress = acroForm.getField("Formular1[0].#subform[0].Header[0].Textfeld1[0]");
        supplierEmail = acroForm.getField("Formular1[0].#subform[0].Header[0].Fax[2]");
        invoiceId = acroForm.getField("Formular1[0].#subform[0].Firma[4]");
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

        for (int i = 0; i < 14; i++) {
            PDFItem article = new PDFItem(
                    acroForm.getField(String.format("Formular1[0].#subform[0].Body[0].Artikel[%d]", i)),
                    acroForm.getField(String.format("Formular1[0].#subform[0].Body[0].Beschreibung[%d]", i)),
                    acroForm.getField(String.format("Formular1[0].#subform[0].Body[0].Menge[%d]", i)),
                    acroForm.getField(String.format("Formular1[0].#subform[0].Body[0].Stückpreis[%d]", i)),
                    acroForm.getField(String.format("Formular1[0].#subform[0].Body[0].Betrag[%d]", i)));
            items.add(article);
        }

        subTotal = acroForm.getField("Formular1[0].#subform[0].Body[0].Zwischensumme[0]");
        netTotal = acroForm.getField("Formular1[0].#subform[0].Body[0].Nettosumme[1]");
        total = acroForm.getField("Formular1[0].#subform[0].Body[0].Gesamtsumme[0]");

        percentageDiscount = acroForm.getField("Formular1[0].#subform[0].Body[0].RabattText[0]");
        vat = acroForm.getField("Formular1[0].#subform[0].Body[0].MwStSatz[0]");
        commentForSupplier = acroForm.getField("Formular1[0].#subform[0].Body[0].Textfeld1[1]");
        costCenter = acroForm.getField("Formular1[0].#subform[1].Textfeld1[2]");
        costCenterSecondary = acroForm.getField("Formular1[0].#subform[1].Textfeld1[4]");
        dfgKey = acroForm.getField("Formular1[0].#subform[1].Textfeld1[3]");

        for (int i = 0; i < 3; i++) {
            PDFQuotation quotation = new PDFQuotation(
                    i + 1, // Index
                    acroForm.getField(String.format("Formular1[0].#subform[1].Textfeld7[%d]", i)),
                    acroForm.getField(String.format("Formular1[0].#subform[1].DateField3[%d]", i)),
                    acroForm.getField(String.format("Formular1[0].#subform[1].Dezimalfeld1[%d]", i)));
            quotations.add(quotation);
        }

        lfdNr = acroForm.getField("Formular1[0].#subform[1].Textfeld4[0]");
        flagDecisionCheapestOffer = (PDCheckBox) acroForm.getField("Formular1[0].#subform[1].Kontrollkästchen1[2]");
        flagDecisionMostEconomicalOffer = (PDCheckBox) acroForm
                .getField("Formular1[0].#subform[1].Kontrollkästchen1[3]");
        flagDecisionSoleSupplier = (PDCheckBox) acroForm.getField("Formular1[0].#subform[1].Kontrollkästchen1[4]");
        flagDecisionContractPartner = (PDCheckBox) acroForm.getField("Formular1[0].#subform[1].Kontrollkästchen1[5]");
        flagDecisionPreferredSupplierList = (PDCheckBox) acroForm
                .getField("Formular1[0].#subform[1].Kontrollkästchen1[6]");
        flagDecisionOtherReasons = (PDCheckBox) acroForm.getField("Formular1[0].#subform[1].Kontrollkästchen1[7]");
        flagDecisionOtherReasonsDescription = acroForm.getField("Formular1[0].#subform[1].Textfeld5[0]");

        // 4. Zustimmung bei Bestellung von DV-Komponenten (Hardware/ Software)
        orderFlagEdvPermission = (PDCheckBox) acroForm.getField("Formular1[0].#subform[1].Kontrollkästchen1[8]");

        // 5. Zustimmung bei Bestellung von Möbeln
        orderFlagFurniturePermission = (PDCheckBox) acroForm.getField("Formular1[0].#subform[1].Kontrollkästchen1[9]");
        // 2nd flag
        orderFlagFurnitureRoom = (PDCheckBox) acroForm.getField("Formular1[0].#subform[1].Kontrollkästchen1[10]");

        // 6. Zustimmung bei der Bestellung von Geräten (baulich-infrastrukturell
        // relevant
        orderFlagInvestmentRoom = (PDCheckBox) acroForm.getField("Formular1[0].#subform[1].Kontrollkästchen1[11]");
        // 2nd flag
        orderFlagInvestmentStructuralMeasures = (PDCheckBox) acroForm
                .getField("Formular1[0].#subform[1].Kontrollkästchen1[12]");

        // 7. Zustimmung bei Bestellung von medientechnischen Einrichtungen und Geräten:
        orderFlagMediaPermission = (PDCheckBox) acroForm.getField("Formular1[0].#subform[1].Kontrollkästchen1[13]");

        return this;
    }

    public void setConstructionAndAssemblyFlag(Boolean flag) throws IOException {
        if (Boolean.TRUE.equals(flag)) {
            this.constructionAndAssemblyFlag.check();
        } else {
            this.constructionAndAssemblyFlag.unCheck();
        }
    }

    public void setDeliveryAndServiceFlag(Boolean flag) throws IOException {
        if (Boolean.TRUE.equals(flag)) {
            this.deliveryAndServiceFlag.check();
        } else {
            this.deliveryAndServiceFlag.unCheck();
        }
    }

    public void setCompanyAddress(String address) throws IOException {
        this.companyAddress.setValue(address != null ? address : "");
    }

    public void setInvoiceId(String invoiceId) throws IOException {
        this.invoiceId.setValue(invoiceId != null ? invoiceId : "");
    }

    public void setDate(String date) throws IOException {
        this.date.setValue(date != null ? date : "");
    }

    public void setOrderer(String orderer) throws IOException {
        this.orderer.setValue(orderer != null ? orderer : "");
    }

    public void setPhone(String phone) throws IOException {
        this.phone.setValue(phone != null ? phone : "");
    }

    public void setMobilePhone(String mobilePhone) throws IOException {
        this.mobilePhone.setValue(mobilePhone != null ? mobilePhone : "");
    }

    public void setEmail(String email) throws IOException {
        this.email.setValue(email != null ? email : "");
    }

    public void setDeliveryFaculty(String deliveryFaculty) throws IOException {
        this.deliveryFaculty.setValue(deliveryFaculty != null ? deliveryFaculty : "");
    }

    public void setDeliveryOrderer(String deliveryOrderer) throws IOException {
        this.deliveryOrderer.setValue(deliveryOrderer != null ? deliveryOrderer : "");
    }

    public void setDeliveryStreet(String deliveryStreet) throws IOException {
        this.deliveryStreet.setValue(deliveryStreet != null ? deliveryStreet : "");
    }

    public void setDeliveryAddress(String deliveryAddress) throws IOException {
        this.deliveryAddress.setValue(deliveryAddress != null ? deliveryAddress : "");
    }

    public void setInvoiceFaculty(String invoiceFaculty) throws IOException {
        this.invoiceFaculty.setValue(invoiceFaculty != null ? invoiceFaculty : "");
    }

    public void setInvoiceOrderer(String invoiceOrderer) throws IOException {
        this.invoiceOrderer.setValue(invoiceOrderer != null ? invoiceOrderer : "");
    }

    public void setInvoiceStreet(String invoiceStreet) throws IOException {
        this.invoiceStreet.setValue(invoiceStreet != null ? invoiceStreet : "");
    }

    public void setInvoiceDeliveryAddress(String invoiceDeliveryAddress) throws IOException {
        this.invoiceDeliveryAddress.setValue(invoiceDeliveryAddress != null ? invoiceDeliveryAddress : "");
    }

    public void setItems(List<ItemResponseDTO> items) throws IOException {
        if (items.size() < 14) {
            for (int i = 0; i < items.size(); i++) {
                ItemResponseDTO itemDTO = items.get(i);
                PDFItem pdfItem = this.items.get(i);

                BigDecimal netPrice = itemDTO.getVatType() == VatType.netto ? itemDTO.getPricePerUnit()
                        : PriceConversionService.convertGrossPriceToNetPrice(itemDTO.getPricePerUnit(),
                                itemDTO.getVat());

                pdfItem.setPosition(String.valueOf(itemDTO.getItemId()));
                pdfItem.setDescription(itemDTO.getName());
                pdfItem.setQuantity(String.valueOf(itemDTO.getQuantity()));
                pdfItem.setPrice((netPrice + " €").replace('.', ','));
                pdfItem.setAmount((BigDecimal.valueOf(itemDTO.getQuantity()).multiply(netPrice) + " €")
                        .replace('.', ','));
            }
        } else {
            throw new RuntimeException("Number of items must be less than 14.");
        }
    }

    public void setSubTotal(String subTotal) throws IOException {
        this.subTotal.setValue(subTotal != null ? subTotal : "");
    }

    public void setNetTotal(String netTotal) throws IOException {
        this.netTotal.setValue(netTotal != null ? netTotal : "");
    }

    public void setTotal(String total) throws IOException {
        this.total.setValue(total != null ? total : "");
    }

    public void setQuotations(List<Quotation> items) throws IOException {
        for (int i = 0; i < items.size(); i++) {
            Quotation quotation = items.get(i);
            PDFQuotation pdfQuotation = this.quotations.get(i);
            pdfQuotation.setIndex(Integer.valueOf(quotation.getIndex()));
            pdfQuotation.setPrice(String.valueOf(quotation.getPrice()));
            pdfQuotation.setCompanyName(quotation.getCompanyName());
            pdfQuotation.setDate(String.valueOf(quotation.getQuoteDate()));
        }
    }

    public void setPercentageDiscount(String percentageDiscount) throws IOException {
        this.percentageDiscount.setValue(percentageDiscount != null ? percentageDiscount : "");
    }

    public void setVat(String vat) throws IOException {
        this.vat.setValue(vat != null ? vat : "");
    }

    public void setCommentForSupplier(String commentForSupplier) throws IOException {
        this.commentForSupplier.setValue(commentForSupplier != null ? commentForSupplier : "");
    }

    public void setCostCenter(String costCenter) throws IOException {
        this.costCenter.setValue(costCenter != null ? costCenter : "");
    }

    public void setCostCenterSecondary(String costCenterSecondary) throws IOException {
        this.costCenterSecondary.setValue(costCenterSecondary != null ? costCenterSecondary : "");
    }

    public void setDfgKey(String dfgKey) throws IOException {
        this.dfgKey.setValue(dfgKey != null ? dfgKey : "");
    }

    public void setOrderNumber(String orderNumber) throws IOException {
        this.orderNumber.setValue(orderNumber != null ? orderNumber : "");
    }

    public void setSupplierEmail(String supplierEmail) throws IOException {
        this.supplierEmail.setValue(supplierEmail != null ? supplierEmail : "");
    }

    public void setLfdNr(String lfdNr) throws IOException {
        this.lfdNr.setValue(lfdNr != null ? lfdNr : "");
    }

    public void setFlagDecisionCheapestOffer(Boolean flag) throws IOException {
        if (Boolean.TRUE.equals(flag)) {
            this.flagDecisionCheapestOffer.check();
        } else {
            this.flagDecisionCheapestOffer.unCheck();
        }
    }

    public void setFlagDecisionMostEconomicalOffer(Boolean flag) throws IOException {
        if (Boolean.TRUE.equals(flag)) {
            this.flagDecisionMostEconomicalOffer.check();
        } else {
            this.flagDecisionMostEconomicalOffer.unCheck();
        }
    }

    public void setFlagDecisionSoleSupplier(Boolean flag) throws IOException {
        if (Boolean.TRUE.equals(flag)) {
            this.flagDecisionSoleSupplier.check();
        } else {
            this.flagDecisionSoleSupplier.unCheck();
        }
    }

    public void setFlagDecisionContractPartner(Boolean flag) throws IOException {
        if (Boolean.TRUE.equals(flag)) {
            this.flagDecisionContractPartner.check();
        } else {
            this.flagDecisionContractPartner.unCheck();
        }
    }

    public void setFlagDecisionPreferredSupplierList(Boolean flag) throws IOException {
        if (Boolean.TRUE.equals(flag)) {
            this.flagDecisionPreferredSupplierList.check();
        } else {
            this.flagDecisionPreferredSupplierList.unCheck();
        }
    }

    public void setFlagDecisionOtherReasons(Boolean flag) throws IOException {
        if (Boolean.TRUE.equals(flag)) {
            this.flagDecisionOtherReasons.check();
        } else {
            this.flagDecisionOtherReasons.unCheck();
        }
    }

    public void setFlagDecisionOtherReasonsDescription(String description) throws IOException {
        this.flagDecisionOtherReasonsDescription.setValue(description != null ? description : "");
    }

    public void setOrderFlagEdvPermission(Boolean flag) throws IOException {
        if (Boolean.TRUE.equals(flag)) {
            this.orderFlagEdvPermission.check();
        } else {
            this.orderFlagEdvPermission.unCheck();
        }
    }

    public void setOrderFlagFurniturePermission(Boolean flag) throws IOException {
        if (Boolean.TRUE.equals(flag)) {
            this.orderFlagFurniturePermission.check();
        } else {
            this.orderFlagFurniturePermission.unCheck();
        }
    }

    public void setOrderFlagFurnitureRoom(Boolean flag) throws IOException {
        if (Boolean.TRUE.equals(flag)) {
            this.orderFlagFurnitureRoom.check();
        } else {
            this.orderFlagFurnitureRoom.unCheck();
        }
    }

    public void setOrderFlagInvestmentRoom(Boolean flag) throws IOException {
        if (Boolean.TRUE.equals(flag)) {
            this.orderFlagInvestmentRoom.check();
        } else {
            this.orderFlagInvestmentRoom.unCheck();
        }
    }

    public void setOrderFlagInvestmentStructuralMeasures(Boolean flag) throws IOException {
        if (Boolean.TRUE.equals(flag)) {
            this.orderFlagInvestmentStructuralMeasures.check();
        } else {
            this.orderFlagInvestmentStructuralMeasures.unCheck();
        }
    }

    public void setOrderFlagMediaPermission(Boolean flag) throws IOException {
        if (Boolean.TRUE.equals(flag)) {
            this.orderFlagMediaPermission.check();
        } else {
            this.orderFlagMediaPermission.unCheck();
        }
    }

    public List<PDFItem> getItems() {
        return this.items;
    }

}
