package de.hs_esslingen.besy.services;

import de.hs_esslingen.besy.dtos.response.ItemResponseDTO;
import de.hs_esslingen.besy.dtos.response.VatResponseDTO;
import de.hs_esslingen.besy.enums.PreferredList;
import de.hs_esslingen.besy.enums.VatType;
import de.hs_esslingen.besy.exceptions.NotFoundException;
import de.hs_esslingen.besy.mappers.response.ItemResponseMapper;
import de.hs_esslingen.besy.models.Address;
import de.hs_esslingen.besy.models.Approval;
import de.hs_esslingen.besy.models.Invoice;
import de.hs_esslingen.besy.models.Item;
import de.hs_esslingen.besy.models.Order;
import de.hs_esslingen.besy.models.Person;
import de.hs_esslingen.besy.models.Quotation;
import de.hs_esslingen.besy.models.Supplier;
import de.hs_esslingen.besy.models.User;
import de.hs_esslingen.besy.repositories.InvoiceRepository;
import de.hs_esslingen.besy.repositories.ItemRepository;
import de.hs_esslingen.besy.repositories.OrderRepository;
import de.hs_esslingen.besy.repositories.PersonRepository;
import de.hs_esslingen.besy.repositories.QuotationRepository;
import de.hs_esslingen.besy.repositories.SupplierRepository;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderPDFServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private PersonRepository personRepository;

    @Mock
    private QuotationRepository quotationRepository;

    @Mock
    private ItemResponseMapper itemResponseMapper;

    @InjectMocks
    private OrderPDFService orderPDFService;

    private Order order;
    private Supplier supplier;
    private User owner;
    private Address supplierAddress;
    private Address deliveryAddress;
    private Address invoiceAddress;
    private Person deliveryPerson;
    private Person invoicePerson;
    private Approval approval;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setName("Jane");
        owner.setSurname("Doe");

        supplierAddress = address("Supplier Street", "10", "12345", "SupplierTown");
        deliveryAddress = address("Delivery Street", "1", "11111", "DeliveryTown");
        invoiceAddress = address("Invoice Street", "2", "22222", "InvoiceTown");

        supplier = new Supplier();
        supplier.setId(10);
        supplier.setName("Supplier GmbH");
        supplier.setEmail("supplier@example.com");
        supplier.setAddress(supplierAddress);

        deliveryPerson = new Person();
        deliveryPerson.setId(201L);
        deliveryPerson.setName("Delivery");

        invoicePerson = new Person();
        invoicePerson.setId(202L);
        invoicePerson.setName("Invoice");

        approval = new Approval();
        approval.setFlagEdvPermission(false);
        approval.setFlagFurniturePermission(false);
        approval.setFlagFurnitureRoom(false);
        approval.setFlagInvestmentRoom(false);
        approval.setFlagInvestmentStructuralMeasures(false);
        approval.setFlagMediaPermission(false);

        order = new Order();
        order.setId(100L);
        order.setPrimaryCostCenterId("CC-1");
        order.setSecondaryCostCenterId("CC-2");
        order.setBookingYear("25");
        order.setAutoIndex((short) 7);
        order.setCreatedDate(LocalDateTime.of(2025, 1, 15, 10, 30));
        order.setContentDescription("Test Order");
        order.setSupplierId(10);
        order.setOwnerId(1);
        order.setOwner(owner);
        order.setQuotePrice(BigDecimal.valueOf(100));
        order.setCommentForSupplier("Comment");
        order.setPercentageDiscount(BigDecimal.valueOf(10));
        order.setDeliveryPersonId(201L);
        order.setInvoicePersonId(202L);
        order.setDeliveryAddress(deliveryAddress);
        order.setInvoiceAddress(invoiceAddress);
        order.setDfgKey("DFG-1");
        order.setFlagDecisionCheapestOffer(false);
        order.setFlagDecisionMostEconomicalOffer(false);
        order.setFlagDecisionSoleSupplier(false);
        order.setFlagDecisionContractPartner(false);
        order.setFlagDecisionPreferredSupplierList(false);
        order.setFlagDecisionOtherReasons(false);
        order.setDecisionOtherReasonsDescription("");
        order.setApproval(approval);
    }

    @Test
    void should_throw_not_found_when_order_missing() {
        Long orderId = 999L;
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> orderPDFService.generateOrderPDF(orderId));

        verify(orderRepository).findById(orderId);
        verifyNoInteractions(supplierRepository, itemRepository, invoiceRepository, personRepository, quotationRepository, itemResponseMapper);
    }

    @Test
    void should_generate_order_number_format() {
        String orderNumber = OrderPDFService.generateOrderNumber("CC1", "25", (short) 7);
        assertEquals("ITCC1_25_7", orderNumber);
    }

    @Test
    void should_generate_pdf_with_totals_and_fields() throws IOException {
        Long orderId = 100L;

        Item item1 = itemWithVat(orderId, 1, BigDecimal.valueOf(10), 2L, BigDecimal.valueOf(19));
        Item item2 = itemWithVat(orderId, 2, BigDecimal.valueOf(5), 4L, BigDecimal.valueOf(19));
        List<Item> items = List.of(item1, item2);

        ItemResponseDTO itemDto1 = new ItemResponseDTO(1, "Item A", BigDecimal.valueOf(10), 2L, "pcs", "A-1", null, "",
                new VatResponseDTO(BigDecimal.valueOf(19), "VAT"), PreferredList.RZ, "", VatType.netto);
        ItemResponseDTO itemDto2 = new ItemResponseDTO(2, "Item B", BigDecimal.valueOf(5), 4L, "pcs", "B-1", null, "",
                new VatResponseDTO(BigDecimal.valueOf(19), "VAT"), PreferredList.RZ, "", VatType.netto);

        Invoice invoice = new Invoice();
        invoice.setId("INV-1");

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(supplierRepository.findById(order.getSupplierId())).thenReturn(Optional.of(supplier));
        when(itemRepository.findByOrder_Id(orderId)).thenReturn(items);
        when(invoiceRepository.findByOrderId(orderId)).thenReturn(Optional.of(invoice));
        when(personRepository.findById(order.getDeliveryPersonId())).thenReturn(Optional.of(deliveryPerson));
        when(personRepository.findById(order.getInvoicePersonId())).thenReturn(Optional.of(invoicePerson));
        when(quotationRepository.getQuotationByOrderId(orderId)).thenReturn(List.of());
        when(itemResponseMapper.toDto(items)).thenReturn(List.of(itemDto1, itemDto2));

        ResponseEntity<byte[]> response = orderPDFService.generateOrderPDF(orderId);

        assertNotNull(response.getBody());
        assertTrue(response.getBody().length > 0);

        try (PDDocument document = Loader.loadPDF(response.getBody())) {
            PDAcroForm form = document.getDocumentCatalog().getAcroForm();
            assertNotNull(form);

            String orderNumber = fieldValue(form, "Formular1[0].#subform[0].Header[0].Rechnungsnummer[0]");
            String subTotal = fieldValue(form, "Formular1[0].#subform[0].Body[0].Zwischensumme[0]");
            String netTotal = fieldValue(form, "Formular1[0].#subform[0].Body[0].Nettosumme[1]");
            String total = fieldValue(form, "Formular1[0].#subform[0].Body[0].Gesamtsumme[0]");
            String companyAddress = fieldValue(form, "Formular1[0].#subform[0].Header[0].Textfeld1[0]");
            String deliveryStreet = fieldValue(form, "Formular1[0].#subform[0].Header[0].Telefon[0]");
            String deliveryAddressField = fieldValue(form, "Formular1[0].#subform[0].Header[0].Fax[0]");

            assertEquals(OrderPDFService.generateOrderNumber("CC-1", "25", (short) 7), orderNumber);
            assertTrue(subTotal.contains("40 €"));
            assertTrue(netTotal.contains("36,00 €"));
            assertTrue(total.contains("42,84 €"));
            assertTrue(companyAddress.contains("Supplier GmbH"));
            assertTrue(deliveryStreet.contains("Delivery Street"));
            assertTrue(deliveryAddressField.contains("11111"));
        }

        verify(orderRepository).findById(orderId);
        verify(supplierRepository).findById(order.getSupplierId());
        verify(itemRepository).findByOrder_Id(orderId);
        verify(invoiceRepository).findByOrderId(orderId);
        verify(personRepository).findById(order.getDeliveryPersonId());
        verify(personRepository).findById(order.getInvoicePersonId());
        verify(quotationRepository).getQuotationByOrderId(orderId);
        verify(itemResponseMapper).toDto(items);
    }

    @Test
    void should_generate_pdf_when_optional_entities_missing() throws IOException {
        Long orderId = 100L;

        Item item1 = itemWithVat(orderId, 1, BigDecimal.valueOf(0), 1L, BigDecimal.valueOf(7));
        List<Item> items = List.of(item1);

        ItemResponseDTO itemDto1 = new ItemResponseDTO(1, "Item A", BigDecimal.valueOf(0), 1L, "pcs", "A-1", null, "",
                new VatResponseDTO(BigDecimal.valueOf(7), "VAT"), PreferredList.RZ, "", VatType.netto);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(supplierRepository.findById(order.getSupplierId())).thenReturn(Optional.empty());
        when(itemRepository.findByOrder_Id(orderId)).thenReturn(items);
        when(invoiceRepository.findByOrderId(orderId)).thenReturn(Optional.empty());
        when(personRepository.findById(order.getDeliveryPersonId())).thenReturn(Optional.empty());
        when(personRepository.findById(order.getInvoicePersonId())).thenReturn(Optional.empty());
        when(quotationRepository.getQuotationByOrderId(orderId)).thenReturn(List.of());
        when(itemResponseMapper.toDto(items)).thenReturn(List.of(itemDto1));

        ResponseEntity<byte[]> response = orderPDFService.generateOrderPDF(orderId);

        assertNotNull(response.getBody());
        assertTrue(response.getBody().length > 0);

        try (PDDocument document = Loader.loadPDF(response.getBody())) {
            PDAcroForm form = document.getDocumentCatalog().getAcroForm();
            assertNotNull(form);
            String orderNumber = fieldValue(form, "Formular1[0].#subform[0].Header[0].Rechnungsnummer[0]");
            assertEquals(OrderPDFService.generateOrderNumber("CC-1", "25", (short) 7), orderNumber);
        }

        verify(orderRepository).findById(orderId);
        verify(supplierRepository).findById(order.getSupplierId());
        verify(itemRepository).findByOrder_Id(orderId);
        verify(invoiceRepository).findByOrderId(orderId);
        verify(personRepository).findById(order.getDeliveryPersonId());
        verify(personRepository).findById(order.getInvoicePersonId());
        verify(quotationRepository).getQuotationByOrderId(orderId);
        verify(itemResponseMapper).toDto(items);
    }

    @Test
    void should_handle_boundary_values_for_totals() throws IOException {
        Long orderId = 100L;

        order.setPercentageDiscount(BigDecimal.valueOf(50));

        Item item1 = itemWithVat(orderId, 1, BigDecimal.valueOf(0), 0L, BigDecimal.valueOf(100));
        List<Item> items = List.of(item1);

        ItemResponseDTO itemDto1 = new ItemResponseDTO(1, "Item Zero", BigDecimal.valueOf(0), 0L, "pcs", "Z-1", null, "",
                new VatResponseDTO(BigDecimal.valueOf(100), "VAT"), PreferredList.RZ, "", VatType.netto);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(supplierRepository.findById(order.getSupplierId())).thenReturn(Optional.of(supplier));
        when(itemRepository.findByOrder_Id(orderId)).thenReturn(items);
        when(invoiceRepository.findByOrderId(orderId)).thenReturn(Optional.empty());
        when(personRepository.findById(order.getDeliveryPersonId())).thenReturn(Optional.of(deliveryPerson));
        when(personRepository.findById(order.getInvoicePersonId())).thenReturn(Optional.of(invoicePerson));
        when(quotationRepository.getQuotationByOrderId(orderId)).thenReturn(List.of());
        when(itemResponseMapper.toDto(items)).thenReturn(List.of(itemDto1));

        ResponseEntity<byte[]> response = orderPDFService.generateOrderPDF(orderId);

        assertNotNull(response.getBody());
        assertTrue(response.getBody().length > 0);

        try (PDDocument document = Loader.loadPDF(response.getBody())) {
            PDAcroForm form = document.getDocumentCatalog().getAcroForm();
            assertNotNull(form);

            String subTotal = fieldValue(form, "Formular1[0].#subform[0].Body[0].Zwischensumme[0]");
            String netTotal = fieldValue(form, "Formular1[0].#subform[0].Body[0].Nettosumme[1]");
            String total = fieldValue(form, "Formular1[0].#subform[0].Body[0].Gesamtsumme[0]");

            assertTrue(subTotal.contains("0"));
            assertTrue(netTotal.contains("0"));
            assertTrue(total.contains("0"));
        }

        verify(orderRepository).findById(orderId);
        verify(supplierRepository).findById(order.getSupplierId());
        verify(itemRepository).findByOrder_Id(orderId);
        verify(invoiceRepository).findByOrderId(orderId);
        verify(personRepository).findById(order.getDeliveryPersonId());
        verify(personRepository).findById(order.getInvoicePersonId());
        verify(quotationRepository).getQuotationByOrderId(orderId);
        verify(itemResponseMapper).toDto(items);
    }

    private static Address address(String street, String buildingNumber, String postalCode, String town) {
        Address address = new Address();
        address.setStreet(street);
        address.setBuildingNumber(buildingNumber);
        address.setPostalCode(postalCode);
        address.setTown(town);
        return address;
    }

    private static Item itemWithVat(Long orderId, int itemId, BigDecimal price, long quantity, BigDecimal vatValue) {
        Item item = new Item();
        item.setOrderId(orderId);
        item.setItemId(itemId);
        item.setPricePerUnit(price);
        item.setQuantity(quantity);
        item.setVatValue(vatValue);
        item.setName("Item " + itemId);
        return item;
    }

    private static String fieldValue(PDAcroForm form, String name) throws IOException {
        return form.getField(name).getValueAsString();
    }
}
