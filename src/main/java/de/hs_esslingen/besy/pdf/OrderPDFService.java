package de.hs_esslingen.besy.pdf;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import de.hs_esslingen.besy.exceptions.BadRequestException;
import de.hs_esslingen.besy.models.Address;
import de.hs_esslingen.besy.models.Approval;
import de.hs_esslingen.besy.models.Item;
import de.hs_esslingen.besy.models.Order;
import de.hs_esslingen.besy.models.Person;
import de.hs_esslingen.besy.models.Quotation;
import de.hs_esslingen.besy.models.Supplier;
import de.hs_esslingen.besy.models.Vat;
import de.hs_esslingen.besy.services.OrderService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderPDFService {

    private final OrderPdfDataLoader dataLoader;
    private final OrderService orderService;

    private final Locale locale;

    static final String FORMULAR_URI = "static/Bestellformular_V01_empty.pdf";

    static final String ANSCHRIFT_FAKULTAET_DEFAULT = "IT";
    static final String ANSCHRIFT_STRASSE_DEFAULT = "Flandernstraße 101";
    static final String ANSCHRIFT_PLZ_ORT_DEFAULT = "73732 Esslingen";
    static final String LAUFENDE_NUMMER_DEFAULT = "1";
    static final String MEHRWERTSTEUER_DEFAULT = "19";

    // TODO: Ensure that this method is only called when the Order is in a state
    // where all necessary constraints and relationships are satisfied
    public byte[] generateOrderPDF(Long orderId) throws IOException {

        // Parse empty Order PDF's acro form elements (load from classpath stream for
        // jar compatibility)
        ClassPathResource pdfResource = new ClassPathResource(FORMULAR_URI);
        if (!pdfResource.exists()) {
            throw new FileNotFoundException("Order PDF template not found at classpath: " + FORMULAR_URI);
        }

        PDDocument document = null;
        try (InputStream pdfStream = pdfResource.getInputStream()) {
            document = Loader.loadPDF(pdfStream.readAllBytes());
            PDAcroForm acroForm = document.getDocumentCatalog().getAcroForm();
            PDFOrder order = new PDFOrder();
            order.parseOrder(acroForm);
            acroForm.setXFA(null);

            // Retrieve Order and necessary relations for PDF
            OrderPdfData data = dataLoader.load(orderId);

            Order orderDAO = data.order();
            Optional<Supplier> supplierDAO = data.supplier();
            Approval approvals = data.approval();
            List<Item> itemsDAO = data.items();
            Optional<Person> deliveryPersonOpt = data.deliveryPerson();
            Optional<Person> invoicePersonOpt = data.invoicePerson();
            Optional<Person> queriesPersonOpt = data.queriesPerson();
            List<Quotation> quotations = data.quotations();
            Address deliveryAddress = data.deliveryAddress();
            Address invoiceAddress = data.invoiceAddress();

            // Write to PDF

            // nach VOB (Bau-/Montageleistung)
            order.setConstructionAndAssemblyFlag(false);
            // nach VOL/UVgO (Liefer-/Dienstleistung)
            order.setDeliveryAndServiceFlag(true);

            if (supplierDAO.isPresent())
                setSupplier(order, supplierDAO.get());

            // Bestell-Nr.
            order.setOrderNumber(orderService.getOrderNumber(orderDAO).orElse(""));
            // Datum:
            order.setDate(orderDAO.getCreatedDate()
                    .format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale)));
            // Besteller:in
            if (queriesPersonOpt.isPresent()) {
                Person queriesPerson = queriesPersonOpt.get();
                String fullName = String.format("%s %s",
                        queriesPerson.getName() != null ? queriesPerson.getName() : "",
                        queriesPerson.getSurname() != null ? queriesPerson.getSurname() : "").trim();
                order.setOrderer(fullName);
                order.setPhone(queriesPerson.getPhone());
                order.setEmail(queriesPerson.getEmail());
            }

            // Angebots-Nr.:
            order.setInvoiceId(orderDAO.getQuoteNumber());

            // Lieferanschrift
            order.setDeliveryFaculty(ANSCHRIFT_FAKULTAET_DEFAULT);
            if (deliveryPersonOpt.isPresent())
                order.setDeliveryOrderer(
                        deliveryPersonOpt.get().getName() + " " + deliveryPersonOpt.get().getSurname());
            order.setDeliveryStreet(getStreet(deliveryAddress) + " " + getBuildingNumber(deliveryAddress));
            order.setDeliveryAddress(formatPostalAndTown(deliveryAddress));

            // Rechnungsanschrift
            order.setInvoiceFaculty(ANSCHRIFT_FAKULTAET_DEFAULT);
            if (invoicePersonOpt.isPresent())
                order.setInvoiceOrderer(invoicePersonOpt.get().getName() + " " + invoicePersonOpt.get().getSurname());
            order.setInvoiceStreet(getStreet(invoiceAddress) + " " + getBuildingNumber(invoiceAddress));
            order.setInvoiceDeliveryAddress(formatPostalAndTown(invoiceAddress));

            try {
                order.setItems(itemsDAO);
            } catch (BadRequestException e) {
                throw new BadRequestException("Error while mapping order items for PDF generation: " + e.getMessage());
            }

            OrderPdfTotals totals = OrderPdfCalculator.calculate(
                    itemsDAO,
                    orderDAO.getPercentageDiscount(),
                    MEHRWERTSTEUER_DEFAULT);

            order.setSubTotal(String.valueOf(totals.subTotal())
                    .replace('.', ',')
                    .concat(" €"));

            order.setNetTotal(String.valueOf(totals.netTotal()).replace('.', ',').concat(" €"));

            String comment = orderDAO.getCommentForSupplier() != null ? orderDAO.getCommentForSupplier() : "";

            if (orderDAO.getCustomer() != null) {
                comment = "Kundennummer: " + orderDAO.getCustomer().getCustomerId() + "\n" + comment;
            }

            // TODO: VAT should be stored by the order itself
            if (totals.vats().size() <= 1) {
                BigDecimal vatValue = totals.vatValue().orElseThrow();
                order.setTotal(String.valueOf(totals.total().orElseThrow())
                        .replace('.', ',')
                        .concat(" €"));
                order.setVat(String.valueOf(vatValue.intValue()));
            } else {
                comment = "Unterschiedlichen Mehrwertsteuersätze: " + totals.vats().stream()
                        .map(Vat::getValue)
                        .distinct()
                        .sorted()
                        .map(value -> value.setScale(0, RoundingMode.HALF_UP).toString().replace('.', ',') + "%")
                        .collect(Collectors.joining(", ")) + "\n" + comment;
            }

            order.setCommentForSupplier(comment);

            order.setPercentageDiscount(String.valueOf(
                    orderDAO.getPercentageDiscount() != null ? orderDAO.getPercentageDiscount() : BigDecimal.ZERO)
                    .replace('.', ','));
            order.setCostCenter(orderDAO.getPrimaryCostCenterId());
            order.setCostCenterSecondary(orderDAO.getSecondaryCostCenterId());
            order.setDfgKey(orderDAO.getDfgKey());

            order.setQuotations(quotations);

            // lfd.Nr.
            order.setLfdNr(LAUFENDE_NUMMER_DEFAULT);

            setDecisionFlags(order, orderDAO);

            setApprovalFlags(order, approvals);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            return baos.toByteArray();
        } finally {
            if (document != null) {
                try {
                    document.close();
                } catch (IOException e) {
                    // Suppress close exception to avoid masking original exception
                }
            }
        }
    }

    private void setSupplier(PDFOrder order, Supplier supplier) throws IOException {
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

    private void setDecisionFlags(PDFOrder order, Order orderDAO) throws IOException {
        order.setFlagDecisionCheapestOffer(orderDAO.getFlagDecisionCheapestOffer());
        order.setFlagDecisionMostEconomicalOffer(orderDAO.getFlagDecisionMostEconomicalOffer());
        order.setFlagDecisionSoleSupplier(orderDAO.getFlagDecisionSoleSupplier());
        order.setFlagDecisionContractPartner(orderDAO.getFlagDecisionContractPartner());
        order.setFlagDecisionPreferredSupplierList(orderDAO.getFlagDecisionPreferredSupplierList());
        order.setFlagDecisionOtherReasons(orderDAO.getFlagDecisionOtherReasons());
        order.setFlagDecisionOtherReasonsDescription(orderDAO.getDecisionOtherReasonsDescription());
    }

    private void setApprovalFlags(PDFOrder order, Approval approvals) throws IOException {
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
