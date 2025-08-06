package de.hs_esslingen.besy.services;

import de.hs_esslingen.besy.dtos.response.ItemResponseDTO;
import de.hs_esslingen.besy.dtos.response.VatResponseDTO;
import de.hs_esslingen.besy.enums.PreferredList;
import de.hs_esslingen.besy.enums.VatType;
import de.hs_esslingen.besy.exceptions.NotFoundException;
import de.hs_esslingen.besy.interfaces.PDFItem;
import de.hs_esslingen.besy.interfaces.PDFOrder;
import de.hs_esslingen.besy.mappers.response.ItemResponseMapper;
import de.hs_esslingen.besy.models.Invoice;
import de.hs_esslingen.besy.models.Item;
import de.hs_esslingen.besy.models.Order;
import de.hs_esslingen.besy.models.Supplier;
import de.hs_esslingen.besy.repositories.InvoiceRepository;
import de.hs_esslingen.besy.repositories.ItemRepository;
import de.hs_esslingen.besy.repositories.OrderRepository;
import de.hs_esslingen.besy.repositories.SupplierRepository;
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
    private final ItemResponseMapper itemResponseMapper;

    static final String FORMULAR_URI = "src/main/resources/static/Bestellformular_V01_empty.pdf";

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
        Supplier supplierDAO = supplierRepository.findById(orderDAO.getSupplierId()).get();
        List<Item> itemsDAO = itemRepository.findByOrder_Id(orderDAO.getId());



        // Write to PDF
        // ToDo: Replace supplier address by address relation
        order.setCompanyAddress("""
        %s
        %s %s
        %s %s
        %s
        """.formatted(
                supplierDAO.getName(),
                supplierDAO.getStreet(),
                supplierDAO.getBuildingNumber() != null ? supplierDAO.getBuildingNumber() : "",
                supplierDAO.getPostalCode(),
                supplierDAO.getTown(),
                supplierDAO.getCountry())
        );

        order.setDate(orderDAO.getCreatedDate().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)));
        order.setOrderer(orderDAO.getOwner().getName() + " " + orderDAO.getOwner().getSurname());
        List<ItemResponseDTO> itemResponseDTOS = itemResponseMapper.toDto(itemsDAO);
        order.setItems(itemResponseDTOS);



        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        document.save(baos);
        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_PDF)
                .body(baos.toByteArray());
    }
}
