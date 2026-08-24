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
 *
 * <p>
 * Frozen quirks preserved here: missing addresses yield {@code " "} /
 * trimmed empty strings, and the mixed-VAT branch leaves the total and VAT
 * fields untouched.
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

        Optional<Supplier> supplier = data.supplier();
        if (supplier.isPresent()) {
            writeSupplier(order, supplier.get());
        }

        // Bestell-Nr.
        order.setOrderNumber(orderNumber);

        order.setDate(PdfValueFormatter.formatDate(orderDAO.getCreatedDate(), locale));

        writeQueriesPerson(order, data.queriesPerson());

        // Angebots-Nr.:
        order.setInvoiceId(orderDAO.getQuoteNumber());

        writeDeliveryAddress(order, data);
        writeInvoiceAddress(order, data);

        try {
            order.setItems(data.items());
        } catch (BadRequestException e) {
            throw new BadRequestException("Error while mapping order items for PDF generation: " + e.getMessage());
        }

        order.setSubTotal(PdfValueFormatter.formatCurrency(totals.subTotal()));
        order.setNetTotal(PdfValueFormatter.formatCurrency(totals.netTotal()));

        order.setCommentForSupplier(writeTotalsAndBuildComment(order, orderDAO, totals));

        order.setPercentageDiscount(PdfValueFormatter.formatDecimal(
                orderDAO.getPercentageDiscount() != null ? orderDAO.getPercentageDiscount() : BigDecimal.ZERO));
        order.setCostCenter(orderDAO.getPrimaryCostCenterId());
        order.setCostCenterSecondary(orderDAO.getSecondaryCostCenterId());
        order.setDfgKey(orderDAO.getDfgKey());

        order.setQuotations(data.quotations());

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
        if (data.deliveryPerson().isPresent()) {
            Person person = data.deliveryPerson().get();
            order.setDeliveryOrderer(person.getName() + " " + person.getSurname());
        }
        order.setDeliveryStreet(getStreet(deliveryAddress) + " " + getBuildingNumber(deliveryAddress));
        order.setDeliveryAddress(formatPostalAndTown(deliveryAddress));
    }

    private void writeInvoiceAddress(PDFOrder order, OrderPdfData data) throws IOException {
        Address invoiceAddress = data.invoiceAddress();
        order.setInvoiceFaculty(properties.getDefaultFaculty());
        if (data.invoicePerson().isPresent()) {
            Person person = data.invoicePerson().get();
            order.setInvoiceOrderer(person.getName() + " " + person.getSurname());
        }
        order.setInvoiceStreet(getStreet(invoiceAddress) + " " + getBuildingNumber(invoiceAddress));
        order.setInvoiceDeliveryAddress(formatPostalAndTown(invoiceAddress));
    }

    /**
     * Writes total and VAT rate for the single-VAT case and returns the comment.
     * In the mixed-VAT case both fields stay empty and the comment is prefixed
     * with the VAT hint — unchanged behaviour, see the frozen Vat.equals quirk.
     */
    private String writeTotalsAndBuildComment(PDFOrder order, Order orderDAO, OrderPdfTotals totals)
            throws IOException {

        String comment = orderDAO.getCommentForSupplier() != null ? orderDAO.getCommentForSupplier() : "";

        if (orderDAO.getCustomer() != null) {
            comment = "Kundennummer: " + orderDAO.getCustomer().getCustomerId() + "\n" + comment;
        }

        // TODO: VAT should be stored by the order itself
        if (totals.vats().size() <= 1) {
            order.setTotal(PdfValueFormatter.formatCurrency(totals.total().orElseThrow()));
            order.setVat(PdfValueFormatter.formatVatRate(totals.vatValue().orElseThrow()));
            return comment;
        }

        return "Unterschiedlichen Mehrwertsteuersätze: " + totals.vats().stream()
                .map(Vat::getValue)
                .distinct()
                .sorted()
                .map(PdfValueFormatter::formatPercentage)
                .collect(Collectors.joining(", ")) + "\n" + comment;
    }

    private void writeSupplier(PDFOrder order, Supplier supplier) throws IOException {
        Address supplierAddress = supplier.getAddress();
        String supplierAddressString = """
                %s
                %s %s
                %s %s
                """.formatted(
                supplier.getName(),
                getStreet(supplierAddress),
                getBuildingNumber(supplierAddress),
                getPostalCode(supplierAddress),
                getTown(supplierAddress)).trim();
        order.setCompanyAddress(supplierAddressString);
        order.setSupplierName(supplier.getName());
        // Fax-Nr./E-Mail:
        order.setSupplierEmail(supplier.getEmail());
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

    /** Still assumes a non-null Approval — null-safety is a later step. */
    private void writeApprovalFlags(PDFOrder order, Approval approvals) throws IOException {
        order.setOrderFlagEdvPermission(approvals.getFlagEdvPermission());
        order.setOrderFlagFurniturePermission(approvals.getFlagFurniturePermission());
        order.setOrderFlagFurnitureRoom(approvals.getFlagFurnitureRoom());
        order.setOrderFlagInvestmentRoom(approvals.getFlagInvestmentRoom());
        order.setOrderFlagInvestmentStructuralMeasures(approvals.getFlagInvestmentStructuralMeasures());
        order.setOrderFlagMediaPermission(approvals.getFlagMediaPermission());
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
        return (getPostalCode(address) + " " + getTown(address)).trim();
    }
}
