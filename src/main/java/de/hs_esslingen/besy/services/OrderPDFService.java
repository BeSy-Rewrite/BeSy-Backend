package de.hs_esslingen.besy.services;

import de.hs_esslingen.besy.dtos.response.ItemResponseDTO;
import de.hs_esslingen.besy.exceptions.NotFoundException;
import de.hs_esslingen.besy.interfaces.PDFOrder;
import de.hs_esslingen.besy.mappers.response.ItemResponseMapper;
import de.hs_esslingen.besy.models.*;
import de.hs_esslingen.besy.repositories.*;
import lombok.AllArgsConstructor;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class OrderPDFService {

    private final OrderRepository orderRepository;
    private final SupplierRepository supplierRepository;
    private final ItemRepository itemRepository;
    private final InvoiceRepository invoiceRepository;
    private final PersonRepository personRepository;

    private final ItemResponseMapper itemResponseMapper;

    static final String FORMULAR_URI = "src/main/resources/static/Bestellformular_V01_empty.pdf";

    static final String ANSCHRIFT_FAKULTAET_DEFAULT = "IT";
    static final String ANSCHRIFT_STRASSE_DEFAULT = "Flandernstraße 101";
    static final String ANSCHRIFT_PLZ_ORT_DEFAULT = "73732 Esslingen";
    static final String LAUFENDE_NUMMER_DEFAULT = "1";
    static final String MEHRWERTSTEUER_DEFAULT = "19";


    // TODO: Ensure that this method is only called when the Order is in a state where all necessary constraints and relationships are satisfied
    public ResponseEntity<byte[]> generateOrderPDF(Integer orderId) throws IOException {

        // Parse empty Order PDF's acro form elements
        PDDocument document = Loader.loadPDF(new File(FORMULAR_URI));
        PDAcroForm acroForm = document.getDocumentCatalog().getAcroForm();
        PDFOrder order = new PDFOrder();
        order.parseOrder(acroForm);
        
        // Retrieve Order and necessary relations for PDF
        Optional<Order> orderOpt = orderRepository.findById(Long.valueOf(orderId));
        if (orderOpt.isEmpty()) throw new NotFoundException("Order with id " + orderId + " does not exist.");

        Order orderDAO = orderOpt.get();
        Supplier supplierDAO = supplierRepository.findById(orderDAO.getSupplierId())
                .orElseThrow(() -> new NotFoundException("Supplier with id " + orderDAO.getSupplierId() + " does not exist."));

        Approval approvals = orderDAO.getApprovals();

        List<Item> itemsDAO = itemRepository.findByOrder_Id(orderDAO.getId());
        Optional<Invoice> invoiceOpt = invoiceRepository.findByOrderId(Long.valueOf(orderId));
        Optional<Person> deliveryPersonOpt = personRepository.findById(Long.valueOf(orderDAO.getDeliveryPersonId()));
        Optional<Person> invoicePersonOpt = personRepository.findById(Long.valueOf(orderDAO.getInvoicePersonId()));
        Address supplierAddress = supplierDAO.getAddress();


        // Write to PDF

        // nach VOB (Bau-/Montageleistung)
        order.setConstructionAndAssemblyFlag(false);
        // nach VOL/UVgO (Liefer-/Dienstleistung)
        order.setDeliveryAndServiceFlag(true);

        // ToDo: Replace supplier address by address relation
        order.setCompanyAddress("""
        %s
        %s %s
        %s %s
        """.formatted(
                supplierDAO.getName(),
                supplierAddress.getStreet(), supplierAddress.getBuildingNumber() != null ? supplierAddress.getBuildingNumber() : "",
                supplierAddress.getPostalCode(), supplierAddress.getTown())
        );

        // Bestell-Nr.
        order.setOrderNumber("""
        IT%s_%s_%s
        """.formatted(
                orderDAO.getPrimaryCostCenterId(), orderDAO.getBookingYear(), orderDAO.getAutoIndex())
        );
        // Datum:
        order.setDate(orderDAO.getCreatedDate().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)));
        // Besteller:in
        order.setOrderer(orderDAO.getOwner().getName() + " " + orderDAO.getOwner().getSurname());
        // Fax-Nr./E-Mail:
        order.setSupplierEmail(supplierDAO.getEmail());
        // Angebots-Nr.:
        if(invoiceOpt.isPresent()) order.setInvoiceId(invoiceOpt.get().getId());

        // Lieferanschrift
        order.setDeliveryFaculty(ANSCHRIFT_FAKULTAET_DEFAULT);
        if(deliveryPersonOpt.isPresent()) order.setDeliveryOrderer(deliveryPersonOpt.get().getName());
        order.setDeliveryStreet(ANSCHRIFT_STRASSE_DEFAULT);
        order.setDeliveryAddress(ANSCHRIFT_PLZ_ORT_DEFAULT);

        // Rechnungsanschrift
        order.setInvoiceFaculty(ANSCHRIFT_FAKULTAET_DEFAULT);
        if(invoicePersonOpt.isPresent()) order.setInvoiceOrderer(invoicePersonOpt.get().getName());
        order.setInvoiceStreet(ANSCHRIFT_STRASSE_DEFAULT);
        order.setInvoiceDeliveryAddress(ANSCHRIFT_PLZ_ORT_DEFAULT);

        List<ItemResponseDTO> itemResponseDTOS = itemResponseMapper.toDto(itemsDAO);
        order.setItems(itemResponseDTOS);
        order.setCommentForSupplier(orderDAO.getCommentForSupplier());
        order.setPercentageDiscount(String.valueOf(orderDAO.getPercentageDiscount()));
        order.setVat(MEHRWERTSTEUER_DEFAULT);
        order.setCostCenter(orderDAO.getPrimaryCostCenterId());
        order.setCostCenterSecondary(orderDAO.getSecondaryCostCenterId());
        order.setDfgKey(orderDAO.getDfgKey());

        // ToDo Angebot/Preisvergleiche

        // lfd.Nr.
        order.setLfdNr(LAUFENDE_NUMMER_DEFAULT);

        order.setFlagDecisionCheapestOffer(orderDAO.getFlagDecisionCheapestOffer());
        //ToDo wirtschaftlichstes Angebot
        order.setFlagDecisionSoleSupplier(orderDAO.getFlagDecisionSoleSupplier());
        order.setFlagDecisionContractPartner(orderDAO.getFlagDecisionContractPartner());
        //ToDo in der Vorzugsliste RZ (EDV) oder FM (Möbel) enthalten ist.
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
    }
}
