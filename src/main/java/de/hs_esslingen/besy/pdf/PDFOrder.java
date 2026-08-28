package de.hs_esslingen.besy.pdf;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Locale;

import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDCheckBox;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.pdmodel.interactive.form.PDTextField;

import de.hs_esslingen.besy.enums.VatType;
import de.hs_esslingen.besy.exceptions.BadRequestException;
import de.hs_esslingen.besy.models.Item;
import de.hs_esslingen.besy.models.Quotation;
import de.hs_esslingen.besy.services.PriceConversionService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PDFOrder {
    public static final int AMOUNT_ITEM_LINES = 15;

    private PdfSafeFieldWriter fieldWriter;

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
    private PDTextField itemDescription;
    private PDFont itemDescriptionFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
    private float itemDescriptionFontSize = 12f;
    private float itemDescriptionMaxWidth = 200f;

    /**
     * Lazily computed width (in 1000-unit-per-em glyph space) of
     * {@link PdfSafeFieldWriter#PLACEHOLDER}, used as the measured width for
     * any code point {@link #itemDescriptionFont} cannot encode. See
     * {@link #getCodePointWidth(int)}.
     */
    private Float placeholderGlyphWidth;

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
        fieldWriter = new PdfSafeFieldWriter(acroForm);

        constructionAndAssemblyFlag = (PDCheckBox) acroForm
                .getField("Formular1[0].#subform[0].Header[0].Kontrollkästchen1[0]");
        deliveryAndServiceFlag = (PDCheckBox) acroForm.getField("Formular1[0].#subform[0].Kontrollkästchen1[1]");
        orderNumber = acroForm.getField("Formular1[0].#subform[0].Header[0].Rechnungsnummer[0]");
        companyAddress = acroForm.getField("Formular1[0].#subform[0].Header[0].Textfeld1[0]");
        supplierEmail = acroForm.getField("Formular1[0].#subform[0].Header[0].Firma[3]");
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

        for (int i = 0; i < AMOUNT_ITEM_LINES; i++) {
            PDFItem article = new PDFItem(
                    acroForm.getField(String.format("Formular1[0].#subform[0].Body[0].Artikel[%d]", i)),
                    (PDTextField) acroForm
                            .getField(String.format("Formular1[0].#subform[0].Body[0].Beschreibung[%d]", i)),
                    acroForm.getField(String.format("Formular1[0].#subform[0].Body[0].Menge[%d]", i)),
                    acroForm.getField(String.format("Formular1[0].#subform[0].Body[0].Stückpreis[%d]", i)),
                    acroForm.getField(String.format("Formular1[0].#subform[0].Body[0].Betrag[%d]", i)),
                    fieldWriter);
            items.add(article);
        }
        itemDescription = (PDTextField) acroForm.getField("Formular1[0].#subform[0].Body[0].Beschreibung[0]");

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
                    acroForm.getField(String.format("Formular1[0].#subform[1].Dezimalfeld1[%d]", i)),
                    fieldWriter);
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

        retrieveDescriptionFontSize();
        retrieveItemDescriptionMaxWidth();

        return this;
    }

    private void retrieveItemDescriptionMaxWidth() {
        itemDescriptionMaxWidth = itemDescription.getWidgets().get(0).getRectangle().getWidth();
    }

    private void retrieveDescriptionFontSize() {
        try {
            // Try to extract font size from default appearance string
            String daString = itemDescription.getDefaultAppearance();
            if (daString != null && daString.contains("Tf")) {
                String[] parts = daString.split(" ");
                for (int i = 0; i < parts.length - 1; i++) {
                    if ("Tf".equals(parts[i + 1])) {
                        itemDescriptionFontSize = Float.parseFloat(parts[i]);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            itemDescriptionFontSize = 12f;
        }
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
        fieldWriter.setValue(this.companyAddress, address);
    }

    public void setSupplierName(String supplierName) throws IOException {
        quotations.get(0).setCompanyName(supplierName != null ? supplierName : "");
    }

    public void setInvoiceId(String invoiceId) throws IOException {
        fieldWriter.setValue(this.invoiceId, invoiceId);
    }

    public void setDate(String date) throws IOException {
        fieldWriter.setValue(this.date, date);
    }

    public void setOrderer(String orderer) throws IOException {
        fieldWriter.setValue(this.orderer, orderer);
    }

    public void setPhone(String phone) throws IOException {
        fieldWriter.setValue(this.phone, phone);
    }

    public void setMobilePhone(String mobilePhone) throws IOException {
        fieldWriter.setValue(this.mobilePhone, mobilePhone);
    }

    public void setEmail(String email) throws IOException {
        fieldWriter.setValue(this.email, email);
    }

    public void setDeliveryFaculty(String deliveryFaculty) throws IOException {
        fieldWriter.setValue(this.deliveryFaculty, deliveryFaculty);
    }

    public void setDeliveryOrderer(String deliveryOrderer) throws IOException {
        fieldWriter.setValue(this.deliveryOrderer, deliveryOrderer);
    }

    public void setDeliveryStreet(String deliveryStreet) throws IOException {
        fieldWriter.setValue(this.deliveryStreet, deliveryStreet);
    }

    public void setDeliveryAddress(String deliveryAddress) throws IOException {
        fieldWriter.setValue(this.deliveryAddress, deliveryAddress);
    }

    public void setInvoiceFaculty(String invoiceFaculty) throws IOException {
        fieldWriter.setValue(this.invoiceFaculty, invoiceFaculty);
    }

    public void setInvoiceOrderer(String invoiceOrderer) throws IOException {
        fieldWriter.setValue(this.invoiceOrderer, invoiceOrderer);
    }

    public void setInvoiceStreet(String invoiceStreet) throws IOException {
        fieldWriter.setValue(this.invoiceStreet, invoiceStreet);
    }

    public void setInvoiceDeliveryAddress(String invoiceDeliveryAddress) throws IOException {
        fieldWriter.setValue(this.invoiceDeliveryAddress, invoiceDeliveryAddress);
    }

    /**
     * Set the items of a pdf order.
     * The PDF must have no less than {@link PDFOrder.AMOUNT_ITEM_LINES} lines.
     *
     * <p>
     * Takes a defensive copy before sorting — the caller's list is never
     * mutated. A price/VAT combination that cannot be converted for a real
     * (quantity &gt; 0) item is surfaced as a {@link BadRequestException}
     * instead of silently defaulting to a net price of 0. Continuation lines
     * produced by line-wrapping (quantity == 0) never attempt the
     * conversion, since they carry no price by design.
     *
     * @param items The list of items to set
     * @throws IOException
     * @throws BadRequestException if the resulting lines after wrapping
     *                             descriptions exceeds
     *                             {@link PDFOrder.AMOUNT_ITEM_LINES}, or if a
     *                             quantity-bearing item has an invalid
     *                             price/VAT combination
     */
    public void setItems(List<Item> items) throws IOException {
        int amountInitialItems = items.size();

        List<Item> sortedItems = new ArrayList<>(items);
        sortedItems.sort((o1, o2) -> Integer.compare(o1.getId().getItemId(), o2.getId().getItemId()));
        List<Item> wrappedItems = wrapItemLines(sortedItems);

        if (wrappedItems.size() > AMOUNT_ITEM_LINES) {
            if (amountInitialItems > AMOUNT_ITEM_LINES) {
                throw new BadRequestException(
                        "Number of items must be less than or equal to " + AMOUNT_ITEM_LINES + ".");
            } else {
                throw new BadRequestException("The item descriptions are too long which results in more than "
                        + AMOUNT_ITEM_LINES + " lines.");
            }
        }

        int itemPosition = 1;

        for (int i = 0; i < wrappedItems.size(); i++) {
            Item item = wrappedItems.get(i);
            PDFItem pdfItem = this.items.get(i);

            if (item.getQuantity() > 0) {
                BigDecimal netPrice;
                try {
                    netPrice = item.getVatType() == VatType.netto
                            ? item.getPricePerUnit()
                            : PriceConversionService.convertGrossPriceToNetPrice(item.getPricePerUnit(),
                                    item.getVat());
                } catch (IllegalArgumentException e) {
                    throw new BadRequestException(
                            "Invalid price or VAT for item " + item.getId().getItemId() + ": " + e.getMessage(), e);
                }

                pdfItem.setPosition(String.valueOf(itemPosition++));
                pdfItem.setQuantity(String.valueOf(item.getQuantity()));
                pdfItem.setPrice(PdfValueFormatter.formatCurrency(netPrice));
                pdfItem.setAmount(PdfValueFormatter.formatCurrency(
                        BigDecimal.valueOf(item.getQuantity()).multiply(netPrice)));
            }
            pdfItem.setDescription(item.getName());
        }
    }

    private List<Item> wrapItemLines(List<Item> items) {
        Deque<Item> itemStack = new ArrayDeque<>();
        List<Item> wrappedItems = new ArrayList<>();
        List<Item> reversedItems = new ArrayList<>(items);
        java.util.Collections.reverse(reversedItems);
        for (Item item : reversedItems) {
            itemStack.push(item);
        }
        while (!itemStack.isEmpty()) {
            Item item = itemStack.pop();
            try {
                float descriptionWidth = getStringWidth(item.getName());
                if (descriptionWidth <= itemDescriptionMaxWidth) {
                    wrappedItems.add(item);
                } else {
                    // Wrap the description into multiple lines
                    for (Item wrappedItem : wrapItem(item)) {
                        wrappedItems.add(wrappedItem);
                    }
                }
            } catch (IOException e) {
                log.error("Error measuring item description width for item ID {}: {}", item.getId().getItemId(),
                        e.getMessage());
                wrappedItems.add(item);
            }
        }
        return wrappedItems;
    }

    private List<Item> wrapItem(Item item) {
        List<Item> wrappedItems = new ArrayList<>();
        String fullDescription = item.getName();

        try {
            // Calculate how much text fits in the first line (accounting for other columns)
            // Assume the description column takes up about 90% of available width
            float availableWidth = itemDescriptionMaxWidth * 0.9f;

            int fitLength = findMaxFittingPrefixLength(fullDescription, availableWidth);
            fitLength = adjustToWordBoundary(fullDescription, fitLength);

            // If entire description fits, return as is
            if (fitLength >= fullDescription.length()) {
                wrappedItems.add(item);
                return wrappedItems;
            }

            // Create first item with truncated description
            Item firstItem = new Item();
            firstItem.setId(item.getId());
            firstItem.setName(fullDescription.substring(0, fitLength).trim());
            firstItem.setQuantity(item.getQuantity());
            firstItem.setPricePerUnit(item.getPricePerUnit());
            firstItem.setVat(item.getVat());
            firstItem.setVatType(item.getVatType());
            wrappedItems.add(firstItem);

            // Create additional items with remaining description
            String remainingDescription = fullDescription.substring(fitLength).trim();
            while (!remainingDescription.isEmpty()) {
                fitLength = findMaxFittingPrefixLength(remainingDescription, itemDescriptionMaxWidth);
                fitLength = adjustToWordBoundary(remainingDescription, fitLength);

                if (fitLength == 0) {
                    // Ensure progress even with very long words / wide code points --
                    // never chop a leading surrogate pair in half.
                    fitLength = firstCodePointLength(remainingDescription);
                }

                Item continuationItem = new Item();
                continuationItem.setId(item.getId());
                continuationItem.setName(remainingDescription.substring(0, fitLength).trim());
                continuationItem.setQuantity(0l); // No quantity for continuation items
                continuationItem.setVat(item.getVat());
                continuationItem.setVatType(item.getVatType());
                wrappedItems.add(continuationItem);

                remainingDescription = remainingDescription.substring(fitLength).trim();
            }
        } catch (IOException e) {
            log.error("Error wrapping item description for item ID {}: {}", item.getId().getItemId(),
                    e.getMessage());
            wrappedItems.add(item);
        }

        return wrappedItems;
    }

    private int findMaxFittingPrefixLength(String text, float maxWidth) throws IOException {
        int low = 0;
        int high = text.length();
        int bestFit = 0;

        while (low <= high) {
            int mid = low + (high - low) / 2;
            float width = getStringWidth(text.substring(0, mid));

            if (width <= maxWidth) {
                bestFit = mid;
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }

        return avoidSurrogateSplit(text, bestFit);
    }

    private int adjustToWordBoundary(String text, int fitLength) {
        if (fitLength <= 0 || fitLength >= text.length()) {
            return fitLength;
        }

        int lastWhitespace = -1;
        for (int i = fitLength - 1; i >= 0; i--) {
            if (Character.isWhitespace(text.charAt(i))) {
                lastWhitespace = i;
                break;
            }
        }

        // Keep binary-search split for single long words, otherwise cut at whitespace.
        return lastWhitespace > 0 ? lastWhitespace : fitLength;
    }

    /**
     * Moves {@code index} backward by one position if it currently sits
     * between the two UTF-16 chars of a surrogate pair (e.g. an emoji) --
     * such an index would otherwise split a single Unicode code point into
     * two lone, invalid surrogates when used as a {@code substring} boundary.
     *
     * <p>
     * Only ever moves backward, so a caller that arrived at {@code index}
     * via a width-based search never ends up with a wider (out-of-budget)
     * prefix -- shortening a prefix can only reduce its measured width.
     *
     * <p>
     * Note: with the current WinAnsi-only template fonts, every supported
     * character fits in a single UTF-16 char, and any surrogate pair is
     * therefore always measured as a single "unsupported" placeholder width
     * (see {@link #getCodePointWidth(int)}) whether whole or split -- which
     * means {@link #findMaxFittingPrefixLength(String, float)} cannot
     * currently land exactly mid-pair by the width math alone (a lone
     * surrogate and its complete pair happen to tie in measured width). This
     * method removes the reliance on that coincidence: once a Unicode-
     * capable fallback font is embedded (planned separately) and a real
     * supported non-BMP glyph exists, a whole pair and a lone half would
     * measure differently, and this guard becomes load-bearing.
     */
    private int avoidSurrogateSplit(String text, int index) {
        if (index > 0 && index < text.length()
                && Character.isHighSurrogate(text.charAt(index - 1))
                && Character.isLowSurrogate(text.charAt(index))) {
            return index - 1;
        }
        return index;
    }

    /**
     * Length (1 or 2 UTF-16 chars) of the first complete Unicode code point
     * in {@code text}. Used as the minimum forced-progress length in
     * {@link #wrapItem(Item)} so that a single very-wide/unsupported code
     * point (e.g. an emoji, itself a surrogate pair) is never split into two
     * lone, invalid surrogates by an unconditional "advance by 1 char"
     * fallback.
     */
    private int firstCodePointLength(String text) {
        if (text.length() > 1
                && Character.isHighSurrogate(text.charAt(0))
                && Character.isLowSurrogate(text.charAt(1))) {
            return 2;
        }
        return 1;
    }

    /**
     * Measures {@code input} against {@link #itemDescriptionFont} at
     * {@link #itemDescriptionFontSize}, operating per Unicode code point
     * (see {@link #getCodePointWidth(int)} for how unsupported/control
     * characters are handled).
     *
     * <p>
     * Previously this method NFD-normalized the input and stripped
     * everything outside ASCII before measuring. That silently dropped
     * non-decomposable characters entirely (e.g. {@code ß} has no ASCII
     * decomposition and simply vanished, so {@code "Straße"} was measured
     * as {@code "Strae"} — narrower than the real glyph) and collapsed
     * CJK/emoji strings to {@code ""} (width {@code 0.0} — such
     * descriptions were judged to always fit, however long). Latin-1
     * characters that <em>do</em> decompose (e.g. {@code ü} -> {@code u} +
     * combining diaeresis) were silently measured as their base letter
     * instead of their real glyph.
     */
    private float getStringWidth(String input) throws IOException {
        float totalWidth = 0f;
        for (int codePoint : input.codePoints().toArray()) {
            totalWidth += getCodePointWidth(codePoint);
        }
        return totalWidth * itemDescriptionFontSize / 1000f;
    }

    /**
     * Returns the glyph width (in 1000-unit-per-em glyph space, i.e. before
     * scaling by font size) of a single Unicode code point against
     * {@link #itemDescriptionFont}.
     *
     * <p>
     * Control characters (line breaks, tabs, ...) contribute no width —
     * they are structural, never actual glyphs.
     *
     * <p>
     * Code points {@link #itemDescriptionFont} cannot encode (currently:
     * anything outside WinAnsi/Latin-1, e.g. CJK or emoji — see the
     * Unicode diagnostic) are measured as the width of the placeholder
     * glyph ({@link PdfSafeFieldWriter#PLACEHOLDER}) instead of
     * contributing zero width. This keeps the wrap decision consistent
     * with what {@link PdfSafeFieldWriter} will actually write into the
     * field afterwards, instead of silently under-measuring unsupported
     * runs as if they were empty.
     */
    private float getCodePointWidth(int codePoint) throws IOException {
        if (Character.isISOControl(codePoint)) {
            return 0f;
        }
        String glyph = new String(Character.toChars(codePoint));
        try {
            return itemDescriptionFont.getStringWidth(glyph);
        } catch (IllegalArgumentException notEncodable) {
            return getPlaceholderGlyphWidth();
        }
    }

    private float getPlaceholderGlyphWidth() throws IOException {
        if (placeholderGlyphWidth == null) {
            placeholderGlyphWidth = itemDescriptionFont.getStringWidth(String.valueOf(PdfSafeFieldWriter.PLACEHOLDER));
        }
        return placeholderGlyphWidth;
    }

    public void setSubTotal(String subTotal) throws IOException {
        fieldWriter.setValue(this.subTotal, subTotal);
    }

    public void setNetTotal(String netTotal) throws IOException {
        fieldWriter.setValue(this.netTotal, netTotal);
    }

    public void setTotal(String total) throws IOException {
        fieldWriter.setValue(this.total, total);
    }

    /**
     * Prices and dates are now routed through {@link PdfValueFormatter},
     * consistent with row 0 (which the writer feeds pre-formatted strings via
     * {@link #setSupplierQuotationRow(String, String, String)}).
     */
    public void setQuotations(List<Quotation> items, Locale locale) throws IOException {
        // We only have 3 quotation fields in the PDF, so we can only set up to 2
        // quotations as the first one is used for the main supplier info
        int maxQuotations = Math.min(items.size(), this.quotations.size() - 1);
        for (int i = 0; i < maxQuotations; i++) {
            Quotation quotation = items.get(i);
            PDFQuotation pdfQuotation = this.quotations.get(i + 1);
            pdfQuotation.setIndex(Integer.valueOf(quotation.getIndex()));
            pdfQuotation.setPrice(PdfValueFormatter.formatCurrency(quotation.getPrice()));
            pdfQuotation.setCompanyName(quotation.getCompanyName());
            pdfQuotation.setDate(PdfValueFormatter.formatDate(quotation.getQuoteDate(), locale));
        }
    }

    /**
     * Writes the supplier's own quotation row (row 0) explicitly.
     * Previously this row was populated as a hidden side effect of
     * {@code setDate}, {@code setSupplierName} and {@code setNetTotal} — each
     * of which appeared to target an unrelated, single field but silently
     * also wrote into {@code quotations.get(0)}. Row 0 represents the
     * supplier's own quotation, alongside up to two competing quotations
     * written by {@link #setQuotations(List, Locale)} (rows 1-2).
     */
    public void setSupplierQuotationRow(String companyName, String date, String netTotal) throws IOException {
        PDFQuotation supplierQuotation = this.quotations.get(0);
        supplierQuotation.setCompanyName(companyName != null ? companyName : "");
        supplierQuotation.setDate(date != null ? date : "");
        supplierQuotation.setPrice(netTotal != null ? netTotal : "");
    }

    public void setPercentageDiscount(String percentageDiscount) throws IOException {
        fieldWriter.setValue(this.percentageDiscount, percentageDiscount);
    }

    public void setVat(String vat) throws IOException {
        fieldWriter.setValue(this.vat, vat);
    }

    public void setCommentForSupplier(String commentForSupplier) throws IOException {
        fieldWriter.setValue(this.commentForSupplier, commentForSupplier);
    }

    public void setCostCenter(String costCenter) throws IOException {
        fieldWriter.setValue(this.costCenter, costCenter);
    }

    public void setCostCenterSecondary(String costCenterSecondary) throws IOException {
        fieldWriter.setValue(this.costCenterSecondary, costCenterSecondary);
    }

    public void setDfgKey(String dfgKey) throws IOException {
        fieldWriter.setValue(this.dfgKey, dfgKey);
    }

    public void setOrderNumber(String orderNumber) throws IOException {
        fieldWriter.setValue(this.orderNumber, orderNumber);
    }

    public void setSupplierEmail(String supplierEmail) throws IOException {
        fieldWriter.setValue(this.supplierEmail, supplierEmail);
    }

    public void setLfdNr(String lfdNr) throws IOException {
        fieldWriter.setValue(this.lfdNr, lfdNr);
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
        fieldWriter.setValue(this.flagDecisionOtherReasonsDescription, description);
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
