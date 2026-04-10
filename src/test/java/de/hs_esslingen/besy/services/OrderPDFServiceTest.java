package de.hs_esslingen.besy.services;

import de.hs_esslingen.besy.dtos.response.ItemResponseDTO;
import de.hs_esslingen.besy.dtos.response.VatResponseDTO;
import de.hs_esslingen.besy.enums.PreferredList;
import de.hs_esslingen.besy.enums.VatType;
import de.hs_esslingen.besy.exceptions.NotFoundException;
import de.hs_esslingen.besy.helper.OrderNumberHelper;
import de.hs_esslingen.besy.models.*;
import de.hs_esslingen.besy.repositories.*;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderPDFServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private PersonRepository personRepository;

    @Mock
    private QuotationRepository quotationRepository;

    @Mock
    private OrderNumberHelper orderNumberHelper;

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

    final String finaleOrderNumber = "ITCC-1/14/007";

    @BeforeEach
    void setUp() {
        orderPDFService = new OrderPDFService(orderRepository, supplierRepository, itemRepository,
                personRepository, quotationRepository, orderNumberHelper, Locale.GERMANY);
        ReflectionTestUtils.setField(orderPDFService, "orderNumberPrefix", "IT");
        ReflectionTestUtils.setField(orderPDFService, "orderNumberSeparator", "_");

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
        verifyNoInteractions(supplierRepository, itemRepository, personRepository, quotationRepository);
    }

    @Test
    void should_format_date_in_german_locale() throws IOException {
        Long orderId = 100L;

        // Order created on 2025-01-15 — should render as "15.01.2025" in German format
        order.setCreatedDate(LocalDateTime.of(2025, 1, 15, 10, 30));

        Item item1 = itemWithVat(orderId, 1, BigDecimal.valueOf(10), 2L, BigDecimal.valueOf(19));
        List<Item> items = List.of(item1);

        ItemResponseDTO itemDto1 = new ItemResponseDTO(1, "Item A", BigDecimal.valueOf(10), 2L, "pcs", "A-1", "",
                new VatResponseDTO(BigDecimal.valueOf(19), "VAT"), PreferredList.RZ, "", VatType.netto);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(supplierRepository.findById(order.getSupplierId())).thenReturn(Optional.of(supplier));
        when(itemRepository.findByOrder_Id(orderId)).thenReturn(items);
        when(personRepository.findById(order.getDeliveryPersonId())).thenReturn(Optional.of(deliveryPerson));
        when(personRepository.findById(order.getInvoicePersonId())).thenReturn(Optional.of(invoicePerson));
        when(quotationRepository.getQuotationByOrderId(orderId)).thenReturn(List.of());

        ResponseEntity<byte[]> response = orderPDFService.generateOrderPDF(orderId);

        assertNotNull(response.getBody());
        try (PDDocument document = Loader.loadPDF(response.getBody())) {
            PDAcroForm form = document.getDocumentCatalog().getAcroForm();
            String date = fieldValue(form, "Formular1[0].#subform[0].Header[0].Rechnungsdatum[0]");
            assertEquals("15.01.2025", date);
        }
    }

    @Test
    void should_generate_pdf_with_totals_and_fields() throws IOException {
        Long orderId = 100L;

        Item item1 = itemWithVat(orderId, 1, BigDecimal.valueOf(10), 2L, BigDecimal.valueOf(19));
        Item item2 = itemWithVat(orderId, 2, BigDecimal.valueOf(5), 4L, BigDecimal.valueOf(19));
        item2.setVat(item1.getVat());
        List<Item> items = List.of(item1, item2);

        ItemResponseDTO itemDto1 = new ItemResponseDTO(1, "Item A", BigDecimal.valueOf(10), 2L, "pcs", "A-1", "",
                new VatResponseDTO(BigDecimal.valueOf(19), "VAT"), PreferredList.RZ, "", VatType.netto);
        ItemResponseDTO itemDto2 = new ItemResponseDTO(2, "Item B", BigDecimal.valueOf(5), 4L, "pcs", "B-1", "",
                new VatResponseDTO(BigDecimal.valueOf(19), "VAT"), PreferredList.RZ, "", VatType.netto);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(supplierRepository.findById(order.getSupplierId())).thenReturn(Optional.of(supplier));
        when(itemRepository.findByOrder_Id(orderId)).thenReturn(items);
        when(personRepository.findById(order.getDeliveryPersonId())).thenReturn(Optional.of(deliveryPerson));
        when(personRepository.findById(order.getInvoicePersonId())).thenReturn(Optional.of(invoicePerson));
        when(quotationRepository.getQuotationByOrderId(orderId)).thenReturn(List.of());

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

            assertEquals(finaleOrderNumber, orderNumber);
            assertAmountEquals(subTotal, BigDecimal.valueOf(40));
            assertAmountEquals(netTotal, BigDecimal.valueOf(36));
            assertAmountEquals(total, BigDecimal.valueOf(42.84));
            assertTrue(companyAddress.contains("Supplier GmbH"));
            assertTrue(deliveryStreet.contains("Delivery Street"));
            assertTrue(deliveryAddressField.contains("11111"));
        }

        verify(orderRepository).findById(orderId);
        verify(supplierRepository).findById(order.getSupplierId());
        verify(itemRepository).findByOrder_Id(orderId);
        verify(personRepository).findById(order.getDeliveryPersonId());
        verify(personRepository).findById(order.getInvoicePersonId());
        verify(quotationRepository).getQuotationByOrderId(orderId);
    }

    @Test
    void should_generate_pdf_when_optional_entities_missing() throws IOException {
        Long orderId = 100L;

        Item item1 = itemWithVat(orderId, 1, BigDecimal.valueOf(0), 1L, BigDecimal.valueOf(7));
        List<Item> items = List.of(item1);

        ItemResponseDTO itemDto1 = new ItemResponseDTO(1, "Item A", BigDecimal.valueOf(0), 1L, "pcs", "A-1", "",
                new VatResponseDTO(BigDecimal.valueOf(7), "VAT"), PreferredList.RZ, "", VatType.netto);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(supplierRepository.findById(order.getSupplierId())).thenReturn(Optional.empty());
        when(itemRepository.findByOrder_Id(orderId)).thenReturn(items);
        when(personRepository.findById(order.getDeliveryPersonId())).thenReturn(Optional.empty());
        when(personRepository.findById(order.getInvoicePersonId())).thenReturn(Optional.empty());
        when(quotationRepository.getQuotationByOrderId(orderId)).thenReturn(List.of());

        ResponseEntity<byte[]> response = orderPDFService.generateOrderPDF(orderId);

        assertNotNull(response.getBody());
        assertTrue(response.getBody().length > 0);

        try (PDDocument document = Loader.loadPDF(response.getBody())) {
            PDAcroForm form = document.getDocumentCatalog().getAcroForm();
            assertNotNull(form);
            String orderNumber = fieldValue(form, "Formular1[0].#subform[0].Header[0].Rechnungsnummer[0]");
            assertEquals(finaleOrderNumber, orderNumber);
        }

        verify(orderRepository).findById(orderId);
        verify(supplierRepository).findById(order.getSupplierId());
        verify(itemRepository).findByOrder_Id(orderId);
        verify(personRepository).findById(order.getDeliveryPersonId());
        verify(personRepository).findById(order.getInvoicePersonId());
        verify(quotationRepository).getQuotationByOrderId(orderId);
    }

    @Test
    void should_handle_boundary_values_for_totals() throws IOException {
        Long orderId = 100L;

        order.setPercentageDiscount(BigDecimal.valueOf(50));

        Item item1 = itemWithVat(orderId, 1, BigDecimal.valueOf(0), 0L, BigDecimal.valueOf(100));
        List<Item> items = List.of(item1);

        ItemResponseDTO itemDto1 = new ItemResponseDTO(1, "Item Zero", BigDecimal.valueOf(0), 0L, "pcs", "Z-1", "",
                new VatResponseDTO(BigDecimal.valueOf(100), "VAT"), PreferredList.RZ, "", VatType.netto);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(supplierRepository.findById(order.getSupplierId())).thenReturn(Optional.of(supplier));
        when(itemRepository.findByOrder_Id(orderId)).thenReturn(items);
        when(personRepository.findById(order.getDeliveryPersonId())).thenReturn(Optional.of(deliveryPerson));
        when(personRepository.findById(order.getInvoicePersonId())).thenReturn(Optional.of(invoicePerson));
        when(quotationRepository.getQuotationByOrderId(orderId)).thenReturn(List.of());

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
        verify(personRepository).findById(order.getDeliveryPersonId());
        verify(personRepository).findById(order.getInvoicePersonId());
        verify(quotationRepository).getQuotationByOrderId(orderId);
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
        item.setId(new ItemId(orderId, itemId));
        item.setPricePerUnit(price);
        item.setQuantity(quantity);
        Vat vat = new Vat();
        vat.setValue(vatValue);
        vat.setDescription("VAT");
        item.setVat(vat);
        item.setVatValue(vatValue);
        item.setVatType(VatType.netto);
        item.setName("Item " + itemId);
        return item;
    }

    private static String fieldValue(PDAcroForm form, String name) throws IOException {
        return form.getField(name).getValueAsString();
    }

    private static void assertAmountEquals(String actual, BigDecimal expected) {
        String normalized = actual.replace('\u00A0', ' ').replace(',', '.');
        Matcher matcher = Pattern.compile("-?\\d+(?:\\.\\d+)?").matcher(normalized);
        assertTrue(matcher.find(), "No numeric amount found in: " + actual);
        BigDecimal parsed = new BigDecimal(matcher.group());
        assertEquals(0, parsed.compareTo(expected), "Expected amount " + expected + " but got " + parsed);
    }
}
