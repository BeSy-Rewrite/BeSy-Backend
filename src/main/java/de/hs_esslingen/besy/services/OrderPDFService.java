package de.hs_esslingen.besy.services;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import de.hs_esslingen.besy.dtos.response.ItemResponseDTO;
import de.hs_esslingen.besy.exceptions.NotFoundException;
import de.hs_esslingen.besy.interfaces.PDFOrder;
import de.hs_esslingen.besy.mappers.response.ItemResponseMapper;
import de.hs_esslingen.besy.models.Address;
import de.hs_esslingen.besy.models.Approval;
import de.hs_esslingen.besy.models.Invoice;
import de.hs_esslingen.besy.models.Item;
import de.hs_esslingen.besy.models.Order;
import de.hs_esslingen.besy.models.Person;
import de.hs_esslingen.besy.models.Quotation;
import de.hs_esslingen.besy.models.Supplier;
import de.hs_esslingen.besy.repositories.InvoiceRepository;
import de.hs_esslingen.besy.repositories.ItemRepository;
import de.hs_esslingen.besy.repositories.OrderRepository;
import de.hs_esslingen.besy.repositories.PersonRepository;
import de.hs_esslingen.besy.repositories.QuotationRepository;
import de.hs_esslingen.besy.repositories.SupplierRepository;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class OrderPDFService {

    private final OrderRepository orderRepository;
    private final SupplierRepository supplierRepository;
    private final ItemRepository itemRepository;
    private final InvoiceRepository invoiceRepository;
    private final PersonRepository personRepository;
    private final QuotationRepository quotationRepository;

    private final ItemResponseMapper itemResponseMapper;

    static final String FORMULAR_URI = "static/Bestellformular_V01_empty.pdf";

    static final String ANSCHRIFT_FAKULTAET_DEFAULT = "IT";
    static final String ANSCHRIFT_STRASSE_DEFAULT = "Flandernstraße 101";
    static final String ANSCHRIFT_PLZ_ORT_DEFAULT = "73732 Esslingen";
    static final String LAUFENDE_NUMMER_DEFAULT = "1";
    static final String MEHRWERTSTEUER_DEFAULT = "19";

    // TODO: Ensure that this method is only called when the Order is in a state
    // where all necessary constraints and relationships are satisfied
    public ResponseEntity<byte[]> generateOrderPDF(Long orderId) throws IOException {

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

            // Print form fields for debugging
            // printFormFields(acroForm);

            // Retrieve Order and necessary relations for PDF
            Optional<Order> orderOpt = orderRepository.findById(Long.valueOf(orderId));
            if (orderOpt.isEmpty())
                throw new NotFoundException("Order with id " + orderId + " does not exist.");

            Order orderDAO = orderOpt.get();
            Optional<Supplier> supplierDAO = supplierRepository.findById(orderDAO.getSupplierId());

            Approval approvals = orderDAO.getApproval();

            List<Item> itemsDAO = itemRepository.findByOrder_Id(orderDAO.getId());
            Optional<Invoice> invoiceOpt = invoiceRepository.findByOrderId(Long.valueOf(orderId));
            Optional<Person> deliveryPersonOpt = personRepository
                    .findById(Long.valueOf(orderDAO.getDeliveryPersonId()));
            Optional<Person> invoicePersonOpt = personRepository.findById(Long.valueOf(orderDAO.getInvoicePersonId()));
            List<Quotation> quotations = quotationRepository.getQuotationByOrderId(Long.valueOf(orderId));

            Address deliveryAddress = orderDAO.getDeliveryAddress();
            Address invoiceAddress = orderDAO.getInvoiceAddress();

            // Write to PDF

            // nach VOB (Bau-/Montageleistung)
            order.setConstructionAndAssemblyFlag(false);
            // nach VOL/UVgO (Liefer-/Dienstleistung)
            order.setDeliveryAndServiceFlag(true);

            if (supplierDAO.isPresent()) {
                Supplier supplier = supplierDAO.get();
                Address supplierAddress = supplier.getAddress();

                order.setCompanyAddress("""
                        %s
                        %s %s
                        %s %s
                        """.formatted(
                        supplier.getName(),
                        supplierAddress.getStreet(),
                        supplierAddress.getBuildingNumber() != null ? supplierAddress.getBuildingNumber() : "",
                        supplierAddress.getPostalCode(), supplierAddress.getTown()));
                // Fax-Nr./E-Mail:
                order.setSupplierEmail(supplier.getEmail());

            }

            // Bestell-Nr.
            order.setOrderNumber(OrderPDFService.generateOrderNumber(orderDAO.getPrimaryCostCenterId(),
                    orderDAO.getBookingYear(), orderDAO.getAutoIndex()));
            // Datum:
            order.setDate(orderDAO.getCreatedDate().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)));
            // Besteller:in
            order.setOrderer(orderDAO.getOwner().getName() + " " + orderDAO.getOwner().getSurname());

            // Angebots-Nr.:
            if (invoiceOpt.isPresent())
                order.setInvoiceId(invoiceOpt.get().getId());

            // Lieferanschrift
            order.setDeliveryFaculty(ANSCHRIFT_FAKULTAET_DEFAULT);
            if (deliveryPersonOpt.isPresent())
                order.setDeliveryOrderer(deliveryPersonOpt.get().getName());
            order.setDeliveryStreet(deliveryAddress.getStreet());
            order.setDeliveryAddress(deliveryAddress.getPostalCode() + " " + deliveryAddress.getTown());

            // Rechnungsanschrift
            order.setInvoiceFaculty(ANSCHRIFT_FAKULTAET_DEFAULT);
            if (invoicePersonOpt.isPresent())
                order.setInvoiceOrderer(invoicePersonOpt.get().getName());
            order.setInvoiceStreet(invoiceAddress.getStreet());
            order.setInvoiceDeliveryAddress(invoiceAddress.getPostalCode() + " " + invoiceAddress.getTown());

            List<ItemResponseDTO> itemResponseDTOS = itemResponseMapper.toDto(itemsDAO);
            order.setItems(itemResponseDTOS);

            BigDecimal subTotal = itemResponseDTOS
                    .stream()
                    .map(item -> item.getPricePerUnit().multiply(BigDecimal.valueOf(item.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            order.setSubTotal(String.valueOf(
                    subTotal)
                    .replace('.', ',')
                    .concat(" €"));

            BigDecimal netTotal = subTotal.multiply((BigDecimal.valueOf(100).subtract(orderDAO.getPercentageDiscount()))
                    .divide(BigDecimal.valueOf(100))).setScale(2, RoundingMode.HALF_UP);
            order.setNetTotal(String.valueOf(netTotal).replace('.', ',').concat(" €"));

            // TODO: VAT should be stored by the order itself
            BigDecimal total = netTotal.multiply(
                    (BigDecimal.valueOf(100).add(itemsDAO.get(0).getVatValue())).divide(BigDecimal.valueOf(100)))
                    .setScale(2, RoundingMode.HALF_UP);
            order.setTotal(String.valueOf(total).replace('.', ',').concat(" €"));

            order.setCommentForSupplier(orderDAO.getCommentForSupplier());
            order.setPercentageDiscount(String.valueOf(orderDAO.getPercentageDiscount()));
            order.setVat(MEHRWERTSTEUER_DEFAULT);
            order.setCostCenter(orderDAO.getPrimaryCostCenterId());
            order.setCostCenterSecondary(orderDAO.getSecondaryCostCenterId());
            order.setDfgKey(orderDAO.getDfgKey());

            order.setQuotations(quotations);

            // lfd.Nr.
            order.setLfdNr(LAUFENDE_NUMMER_DEFAULT);

            order.setFlagDecisionCheapestOffer(orderDAO.getFlagDecisionCheapestOffer());
            order.setFlagDecisionMostEconomicalOffer(orderDAO.getFlagDecisionMostEconomicalOffer());
            order.setFlagDecisionSoleSupplier(orderDAO.getFlagDecisionSoleSupplier());
            order.setFlagDecisionContractPartner(orderDAO.getFlagDecisionContractPartner());
            order.setFlagDecisionPreferredSupplierList(orderDAO.getFlagDecisionPreferredSupplierList());
            order.setFlagDecisionOtherReasons(orderDAO.getFlagDecisionOtherReasons());
            order.setFlagDecisionOtherReasonsDescription(orderDAO.getDecisionOtherReasonsDescription());

            order.setOrderFlagEdvPermission(approvals.getFlagEdvPermission());
            order.setOrderFlagFurniturePermission(approvals.getFlagFurniturePermission());
            order.setOrderFlagFurnitureRoom(approvals.getFlagFurnitureRoom());
            order.setOrderFlagInvestmentRoom(approvals.getFlagInvestmentRoom());
            order.setOrderFlagInvestmentStructuralMeasures(approvals.getFlagInvestmentStructuralMeasures());
            order.setOrderFlagMediaPermission(approvals.getFlagMediaPermission());

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            return ResponseEntity
                    .ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(baos.toByteArray());
        } finally {
            if (document != null) {
                document.close();
            }
        }
    }

    private void printFormFields(PDAcroForm acroForm) {
        Iterable<PDField> fieldTree = acroForm.getFieldTree();

        List<PDField> allFields = new ArrayList<>();
        fieldTree.forEach(allFields::add);

        System.out.println("Total fields (including nested): " + allFields.size());

        for (PDField field : allFields) {
            System.out.println("Name:  " + field.getFullyQualifiedName());
            System.out.println("Type:  " + field.getClass().getSimpleName());
            System.out.println("Value: " + field.getValueAsString());
            System.out.println("----------------------------");
        }
    }

    public static String generateOrderNumber(String primaryCostCenterId, String bookingYear, Short autoIndex) {
        return ("""
                IT%s_%s_%s
                """.formatted(
                primaryCostCenterId, bookingYear, autoIndex)).trim();
    }
}
