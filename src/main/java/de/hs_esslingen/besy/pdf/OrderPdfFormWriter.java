package de.hs_esslingen.besy.pdf;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import de.hs_esslingen.besy.exceptions.BadRequestException;
import de.hs_esslingen.besy.models.Address;
import de.hs_esslingen.besy.models.Approval;
import de.hs_esslingen.besy.models.Order;
import de.hs_esslingen.besy.models.Person;
import de.hs_esslingen.besy.models.Supplier;
import de.hs_esslingen.besy.models.Vat;
import lombok.RequiredArgsConstructor;

/**
 * Writes all order values into the parsed AcroForm binding ({@link PDFOrder}).
 *
 * <p>
 * Pure sink: no repository access, no PDDocument handling, no calculation.
 * The write sequence is intentionally identical to the previous inline code in
 * {@link OrderPDFService}, because a few {@link PDFOrder} setters have side
 * effects on the first quotation row ({@code setDate}, {@code setSupplierName},
 * {@code setNetTotal}).
 */
@Component
@RequiredArgsConstructor
public class OrderPdfFormWriter {

    private final Locale locale;
    private final OrderPdfProperties properties;

    public void write(PDFOrder order, OrderPdfData data, OrderPdfTotals totals, String orderNumber)
            throws IOException {

        Order orderDAO = data.order();

        // nach VOB (Bau-/Montageleistung)
        order.setConstructionAndAssemblyFlag(false);
        // nach VOL/UVgO (Liefer-/Dienstleistung)
        order.setDeliveryAndServiceFlag(true);

        String supplierName = writeSupplier(order, data.supplier());

        // Bestell-Nr.
        order.setOrderNumber(orderNumber);

        String formattedDate = PdfValueFormatter.formatDate(orderDAO.getCreatedDate(), locale);
        order.setDate(formattedDate);

        writeQueriesPerson(order, data.queriesPerson());

        // Angebots-Nr.:
        order.setInvoiceId(orderDAO.getQuoteNumber());

        writeDeliveryAddress(order, data);
        writeInvoiceAddress(order, data);

        try {
            order.setItems(data.items());
        } catch (BadRequestException e) {
            throw new BadRequestException("Error while mapping order items for PDF generation: " + e.getMessage(), e);
        }

        order.setSubTotal(PdfValueFormatter.formatCurrency(totals.subTotal()));
        String formattedNetTotal = PdfValueFormatter.formatCurrency(totals.netTotal());
        order.setNetTotal(formattedNetTotal);

        // The supplier's quotation row is now written explicitly in one
        // place, once all three of its values are known, instead of as a
        // side effect of setDate/setSupplierName/setNetTotal.
        order.setSupplierQuotationRow(supplierName, formattedDate, formattedNetTotal);

        order.setCommentForSupplier(writeTotalsAndBuildComment(order, orderDAO, totals));

        order.setPercentageDiscount(PdfValueFormatter.formatDecimal(
                orderDAO.getPercentageDiscount() != null ? orderDAO.getPercentageDiscount() : BigDecimal.ZERO));
        order.setCostCenter(orderDAO.getPrimaryCostCenterId());
        order.setCostCenterSecondary(orderDAO.getSecondaryCostCenterId());
        order.setDfgKey(orderDAO.getDfgKey());

        order.setQuotations(data.quotations(), locale);

        order.setLfdNr(properties.getDefaultLfdNr());

        writeDecisionFlags(order, orderDAO);
        writeApprovalFlags(order, data.approval());
    }

    private void writeQueriesPerson(PDFOrder order, Optional<Person> queriesPersonOpt) throws IOException {
        if (queriesPersonOpt.isEmpty()) {
            return;
        }
        Person queriesPerson = queriesPersonOpt.get();
        String fullName = String.format("%s %s",
                queriesPerson.getName() != null ? queriesPerson.getName() : "",
                queriesPerson.getSurname() != null ? queriesPerson.getSurname() : "").trim();
        order.setOrderer(fullName);
        order.setPhone(queriesPerson.getPhone());
        order.setEmail(queriesPerson.getEmail());
    }

    private void writeDeliveryAddress(PDFOrder order, OrderPdfData data) throws IOException {
        Address deliveryAddress = data.deliveryAddress();
        order.setDeliveryFaculty(properties.getDefaultFaculty());

        order.setDeliveryOrderer(data.deliveryPerson()
                .map(person -> joinNonBlank(person.getName(), person.getSurname()))
                .orElse(""));

        order.setDeliveryStreet(joinNonBlank(getStreet(deliveryAddress), getBuildingNumber(deliveryAddress)));
        order.setDeliveryAddress(formatPostalAndTown(deliveryAddress));
    }

    private void writeInvoiceAddress(PDFOrder order, OrderPdfData data) throws IOException {
        Address invoiceAddress = data.invoiceAddress();
        order.setInvoiceFaculty(properties.getDefaultFaculty());

        order.setInvoiceOrderer(data.invoicePerson()
                .map(person -> joinNonBlank(person.getName(), person.getSurname()))
                .orElse(""));

        order.setInvoiceStreet(joinNonBlank(getStreet(invoiceAddress), getBuildingNumber(invoiceAddress)));
        order.setInvoiceDeliveryAddress(formatPostalAndTown(invoiceAddress));
    }

    /**
     * Writes the total (always known now, computed per VAT rate and summed)
     * and the VAT rate for the single-VAT case, and returns the comment. In the
     * mixed-VAT case the VAT rate field stays blank — there is no single rate —
     * and the comment is prefixed with the VAT hint.
     */
    private String writeTotalsAndBuildComment(PDFOrder order, Order orderDAO, OrderPdfTotals totals)
            throws IOException {

        String comment = orderDAO.getCommentForSupplier() != null ? orderDAO.getCommentForSupplier() : "";

        if (orderDAO.getCustomer() != null) {
            comment = "Kundennummer: " + orderDAO.getCustomer().getCustomerId() + "\n" + comment;
        }

        order.setTotal(PdfValueFormatter.formatCurrency(totals.total().orElseThrow()));

        // TODO: VAT should be stored by the order itself
        if (totals.vats().size() <= 1) {
            order.setVat(PdfValueFormatter.formatVatRate(totals.vatValue().orElseThrow()));
            return comment;
        }

        return "Unterschiedliche Mehrwertsteuersätze: " + totals.vats().stream()
                .map(Vat::getValue)
                .distinct()
                .sorted()
                .map(PdfValueFormatter::formatPercentage)
                .collect(Collectors.joining(", ")) + "\n" + comment;
    }

    /**
     * Writes the supplier's address/email fields, explicitly {@code ""} when
     * there is no supplier — never leaves stale template state in
     * {@code Textfeld1[0]}/{@code Firma[3]}. Returns the supplier's name (or
     * {@code ""} if absent) for {@link PDFOrder#setSupplierQuotationRow},
     * which writes it into quotation row 0 (Q7).
     */
    private String writeSupplier(PDFOrder order, Optional<Supplier> supplierOpt) throws IOException {
        if (supplierOpt.isEmpty()) {
            order.setCompanyAddress("");
            order.setSupplierEmail("");
            return "";
        }

        Supplier supplier = supplierOpt.get();
        Address supplierAddress = supplier.getAddress();
        String supplierAddressString = """
                %s
                %s
                %s
                """.formatted(
                supplier.getName(),
                joinNonBlank(getStreet(supplierAddress), getBuildingNumber(supplierAddress)),
                formatPostalAndTown(supplierAddress)).trim();
        order.setCompanyAddress(supplierAddressString);
        // Fax-Nr./E-Mail:
        order.setSupplierEmail(supplier.getEmail());
        return supplier.getName();
    }

    private void writeDecisionFlags(PDFOrder order, Order orderDAO) throws IOException {
        order.setFlagDecisionCheapestOffer(orderDAO.getFlagDecisionCheapestOffer());
        order.setFlagDecisionMostEconomicalOffer(orderDAO.getFlagDecisionMostEconomicalOffer());
        order.setFlagDecisionSoleSupplier(orderDAO.getFlagDecisionSoleSupplier());
        order.setFlagDecisionContractPartner(orderDAO.getFlagDecisionContractPartner());
        order.setFlagDecisionPreferredSupplierList(orderDAO.getFlagDecisionPreferredSupplierList());
        order.setFlagDecisionOtherReasons(orderDAO.getFlagDecisionOtherReasons());
        order.setFlagDecisionOtherReasonsDescription(orderDAO.getDecisionOtherReasonsDescription());
    }

    /**
     * An order without an {@link Approval} is valid — no approvals have
     * been recorded yet. Falls back to a fresh {@link Approval}, whose flags
     * default to {@code false}, so every approval checkbox ends up unchecked
     * instead of throwing an NPE.
     */
    private void writeApprovalFlags(PDFOrder order, Approval approval) throws IOException {
        Approval effective = approval != null ? approval : new Approval();
        order.setOrderFlagEdvPermission(effective.getFlagEdvPermission());
        order.setOrderFlagFurniturePermission(effective.getFlagFurniturePermission());
        order.setOrderFlagFurnitureRoom(effective.getFlagFurnitureRoom());
        order.setOrderFlagInvestmentRoom(effective.getFlagInvestmentRoom());
        order.setOrderFlagInvestmentStructuralMeasures(effective.getFlagInvestmentStructuralMeasures());
        order.setOrderFlagMediaPermission(effective.getFlagMediaPermission());
    }

    private String getStreet(Address address) {
        return address != null && address.getStreet() != null ? address.getStreet() : "";
    }

    private String getBuildingNumber(Address address) {
        return address != null && address.getBuildingNumber() != null ? address.getBuildingNumber() : "";
    }

    private String getPostalCode(Address address) {
        return address != null && address.getPostalCode() != null ? address.getPostalCode() : "";
    }

    private String getTown(Address address) {
        return address != null && address.getTown() != null ? address.getTown() : "";
    }

    private String formatPostalAndTown(Address address) {
        return joinNonBlank(getPostalCode(address), getTown(address));
    }

    /**
     * Joins only the non-blank parts with a single space; never yields a stray
     * space.
     */
    private String joinNonBlank(String... parts) {
        return java.util.Arrays.stream(parts)
                .filter(p -> p != null && !p.isBlank())
                .collect(Collectors.joining(" "));
    }
}
