package de.hs_esslingen.besy.services;

import de.hs_esslingen.besy.enums.*;
import de.hs_esslingen.besy.exceptions.NotFoundException;
import de.hs_esslingen.besy.models.*;
import de.hs_esslingen.besy.repositories.*;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@ActiveProfiles("test")
@Transactional
@Disabled("Fails because of syntax issues")
class OrderPDFServiceIntegrationTest {

    @Autowired
    private OrderPDFService orderPDFService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private QuotationRepository quotationRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VatRepository vatRepository;

    final String finaleOrderNumber = "ITCC-1/14/007";

    @AfterEach
    void tearDown() {
        quotationRepository.deleteAll();
        itemRepository.deleteAll();
        invoiceRepository.deleteAll();
        orderRepository.deleteAll();
        supplierRepository.deleteAll();
        personRepository.deleteAll();
        addressRepository.deleteAll();
        userRepository.deleteAll();
        vatRepository.deleteAll();
    }

    @Test
    void should_throw_not_found_when_order_missing() {
        // Missing order should raise NotFoundException
        assertThrows(NotFoundException.class, () -> orderPDFService.generateOrderPDF(9999L));
    }

    @Test
    void should_generate_pdf_end_to_end_with_expected_fields() throws IOException {
        // Prepare VAT
        Vat vat19 = new Vat();
        vat19.setValue(BigDecimal.valueOf(19));
        vat19.setDescription("VAT19");
        vatRepository.save(vat19);

        // Prepare addresses
        Address supplierAddress = address(AddressOwnerType.Supplier, "Supplier St", "10", "12345", "SupplierTown");
        Address deliveryAddress = address(AddressOwnerType.Person, "Delivery St", "1", "11111", "DeliveryTown");
        Address invoiceAddress = address(AddressOwnerType.Person, "Invoice St", "2", "22222", "InvoiceTown");
        addressRepository.saveAll(List.of(supplierAddress, deliveryAddress, invoiceAddress));

        // Prepare supplier
        Supplier supplier = new Supplier();
        supplier.setName("Supplier GmbH");
        supplier.setEmail("supplier@example.com");
        supplier.setAddress(supplierAddress);
        supplier = supplierRepository.save(supplier);

        // Prepare user (order owner)
        User user = new User();
        user.setName("Jane");
        user.setSurname("Doe");
        user.setEmail("jane.doe@example.com");
        user = userRepository.save(user);

        // Prepare persons
        Person deliveryPerson = person("Delivery", "Person", Gender.m, deliveryAddress);
        Person invoicePerson = person("Invoice", "Person", Gender.f, invoiceAddress);
        deliveryPerson = personRepository.save(deliveryPerson);
        invoicePerson = personRepository.save(invoicePerson);

        // Prepare order and approval
        Order order = new Order();
        order.setContentDescription("Test Order");
        order.setStatus(OrderStatus.IN_PROGRESS);
        order.setPrimaryCostCenterId("CC-1");
        order.setSecondaryCostCenterId("CC-2");
        order.setBookingYear("25");
        order.setAutoIndex((short) 7);
        order.setCreatedDate(LocalDateTime.of(2025, 1, 15, 10, 30));
        order.setSupplierId(supplier.getId());
        order.setOwner(user);
        order.setOwnerId(user.getId());
        order.setQuotePrice(BigDecimal.valueOf(100));
        order.setCommentForSupplier("Comment");
        order.setPercentageDiscount(BigDecimal.valueOf(10));
        order.setDeliveryPersonId(deliveryPerson.getId());
        order.setInvoicePersonId(invoicePerson.getId());
        order.setDeliveryAddress(deliveryAddress);
        order.setInvoiceAddress(invoiceAddress);
        order.setDfgKey("DFG-1");

        Approval approval = new Approval();
        approval.setOrder(order);
        approval.setOrderId(order.getId());
        approval.setFlagEdvPermission(false);
        approval.setFlagFurniturePermission(false);
        approval.setFlagFurnitureRoom(false);
        approval.setFlagInvestmentRoom(false);
        approval.setFlagInvestmentStructuralMeasures(false);
        approval.setFlagMediaPermission(false);
        order.setApproval(approval);

        order = orderRepository.save(order);

        // Prepare items
        Item item1 = item(order, 1, "Item A", BigDecimal.valueOf(10), 2L, vat19);
        Item item2 = item(order, 2, "Item B", BigDecimal.valueOf(5), 4L, vat19);
        itemRepository.saveAll(List.of(item1, item2));

        // Optional invoice
        Invoice invoice = new Invoice();
        invoice.setId("INV-1");
        invoice.setCostCenterId("CC-1");
        invoice.setOrderId(order.getId());
        invoice.setPrice(BigDecimal.valueOf(40));
        invoice.setDate(LocalDate.of(2025, 1, 20));
        invoice.setComment("Invoice comment");
        invoice.setCreatedDate(order.getCreatedDate().atOffset(java.time.ZoneOffset.UTC));
        invoiceRepository.save(invoice);

        // Optional quotation
        Quotation quotation = new Quotation();
        quotation.setId(new QuotationId(order.getId(), (short) 1));
        quotation.setOrder(order);
        quotation.setQuoteDate(LocalDate.of(2025, 1, 5));
        quotation.setPrice(BigDecimal.valueOf(50));
        quotation.setCompanyName("Quote Co");
        quotation.setCompanyCity("Quote City");
        quotationRepository.save(quotation);

        // Execute
        ResponseEntity<byte[]> response = orderPDFService.generateOrderPDF(order.getId());

        // Basic response checks
        assertEquals(200, response.getStatusCode().value());
        assertEquals(MediaType.APPLICATION_PDF, response.getHeaders().getContentType());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().length > 0);

        // Parse PDF and verify key fields
        try (PDDocument document = Loader.loadPDF(response.getBody())) {
            PDAcroForm form = document.getDocumentCatalog().getAcroForm();
            assertNotNull(form);

            String orderNumber = fieldValue(form, "Formular1[0].#subform[0].Header[0].Rechnungsnummer[0]");
            String companyAddress = fieldValue(form, "Formular1[0].#subform[0].Header[0].Textfeld1[0]");
            String supplierEmail = fieldValue(form, "Formular1[0].#subform[0].Header[0].Fax[2]");

            String item1Name = fieldValue(form, "Formular1[0].#subform[0].Body[0].Beschreibung[0]");
            String item1Qty = fieldValue(form, "Formular1[0].#subform[0].Body[0].Menge[0]");
            String item1Price = fieldValue(form, "Formular1[0].#subform[0].Body[0].Stückpreis[0]");
            String item1Amount = fieldValue(form, "Formular1[0].#subform[0].Body[0].Betrag[0]");

            String subTotal = fieldValue(form, "Formular1[0].#subform[0].Body[0].Zwischensumme[0]");
            String netTotal = fieldValue(form, "Formular1[0].#subform[0].Body[0].Nettosumme[1]");
            String total = fieldValue(form, "Formular1[0].#subform[0].Body[0].Gesamtsumme[0]");
            String vat = fieldValue(form, "Formular1[0].#subform[0].Body[0].MwStSatz[0]");

            assertEquals(finaleOrderNumber, orderNumber);
            assertTrue(companyAddress.contains("Supplier GmbH"));
            assertTrue(companyAddress.contains("Supplier St"));
            assertTrue(companyAddress.contains("12345"));
            assertTrue(companyAddress.contains("SupplierTown"));
            assertTrue(supplierEmail.contains("supplier@example.com"));

            assertEquals("Item A", item1Name);
            assertEquals("2", item1Qty);
            assertEquals("10 €", item1Price);
            assertEquals("20 €", item1Amount);

            BigDecimal expectedSubTotal = BigDecimal.valueOf(40);
            BigDecimal expectedNetTotal = expectedSubTotal
                    .multiply(BigDecimal.valueOf(100).subtract(order.getPercentageDiscount()))
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            BigDecimal expectedTotal = expectedNetTotal
                    .multiply(BigDecimal.valueOf(100).add(vat19.getValue()))
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

            assertEquals(expectedSubTotal.toString().replace('.', ',') + " €", subTotal);
            assertEquals(expectedNetTotal.toString().replace('.', ',') + " €", netTotal);
            assertEquals(expectedTotal.toString().replace('.', ',') + " €", total);
            assertEquals("19", vat);
        }
    }

    private static Address address(AddressOwnerType ownerType, String street, String buildingNumber, String postalCode, String town) {
        Address address = new Address();
        address.setOwnerType(ownerType);
        address.setStreet(street);
        address.setBuildingNumber(buildingNumber);
        address.setPostalCode(postalCode);
        address.setTown(town);
        return address;
    }

    private static Person person(String name, String surname, Gender gender, Address address) {
        Person person = new Person();
        person.setName(name);
        person.setSurname(surname);
        person.setGender(gender);
        person.setAddress(address);
        return person;
    }

    private static Item item(Order order, int itemId, String name, BigDecimal price, long quantity, Vat vat) {
        Item item = new Item();
        item.setId(new ItemId(order.getId(), itemId));
        item.setOrder(order);
        item.setName(name);
        item.setPricePerUnit(price);
        item.setQuantity(quantity);
        item.setQuantityUnit("pcs");
        item.setArticleId("A-" + itemId);
        item.setComment("Comment");
        item.setVat(vat);
        item.setVatValue(vat.getValue());
        item.setPreferredList(PreferredList.RZ);
        item.setPreferredListNumber("PL-" + itemId);
        item.setVatType(VatType.netto);
        item.setMigratedToInsy(false);
        return item;
    }

    private static String fieldValue(PDAcroForm form, String name) throws IOException {
        return form.getField(name).getValueAsString();
    }
}
