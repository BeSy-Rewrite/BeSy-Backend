package de.hs_esslingen.besy.pdf;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.pdmodel.interactive.form.PDNonTerminalField;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import de.hs_esslingen.besy.enums.Gender;
import de.hs_esslingen.besy.enums.OrderStatus;
import de.hs_esslingen.besy.enums.PreferredList;
import de.hs_esslingen.besy.enums.VatType;
import de.hs_esslingen.besy.models.Address;
import de.hs_esslingen.besy.models.Approval;
import de.hs_esslingen.besy.models.CustomerId;
import de.hs_esslingen.besy.models.Item;
import de.hs_esslingen.besy.models.ItemId;
import de.hs_esslingen.besy.models.Order;
import de.hs_esslingen.besy.models.Person;
import de.hs_esslingen.besy.models.Quotation;
import de.hs_esslingen.besy.models.QuotationId;
import de.hs_esslingen.besy.models.Supplier;
import de.hs_esslingen.besy.models.Vat;
import de.hs_esslingen.besy.repositories.ItemRepository;
import de.hs_esslingen.besy.repositories.OrderRepository;
import de.hs_esslingen.besy.repositories.PersonRepository;
import de.hs_esslingen.besy.repositories.QuotationRepository;
import de.hs_esslingen.besy.repositories.SupplierRepository;
import de.hs_esslingen.besy.services.OrderService;

/**
 * GOLDEN / SNAPSHOT TEST — Safety net for PDF refactoring.
 *
 * Renders fixed fixture orders and compares ALL AcroForm field values in the
 * generated PDF against a checked-in snapshot. The test must remain green
 * through every
 * refactoring step (pure refactoring => identical
 * field values).
 *
 * It also intentionally preserves current BUGGY BEHAVIOR (locale formatting,
 * “ ” for missing addresses, identity-based VAT settings). Only when a
 * behavior is intentionally corrected should you regenerate the snapshot:
 *
 * mvn test -Dtest=OrderPdfGoldenTest -Dpdf.golden.update=true
 *
 * and review the diff BEFORE committing.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OrderPdfGoldenTest {

    private static final String GOLDEN_DIR = "golden/pdf/";
    private static final Path ACTUAL_OUT = Path.of("target", "golden-actual", "pdf");

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
    private OrderService orderService;

    private OrderPDFService service;

    @BeforeEach
    void setUp() {
        OrderPdfDataLoader dataLoader = new OrderPdfDataLoader(
                orderRepository,
                supplierRepository,
                itemRepository,
                personRepository,
                quotationRepository);

        OrderPdfProperties properties = OrderPdfProperties.defaults();

        service = new OrderPDFService(new OrderPdfGenerator(
                dataLoader,
                orderService,
                properties,
                new PdfTemplateLoader(properties),
                new OrderPdfFormWriter(Locale.GERMANY, properties)));
    }

    // ------------------------------------------------------------------- Tests

    @Test
    @DisplayName("golden: complete order, one VAT rate (split VAT), 10% discount, 2 offers")
    void goldenSingleVat() throws IOException {
        Fixture f = Fixture.completeSingleVat();
        stub(f);

        assertMatchesGolden("order-single-vat.snapshot", renderAndExtractFields(f.orderId()));
    }

    @Test
    @DisplayName("golden: mixed VAT rates -> comment branch")
    void goldenMixedVat() throws IOException {
        Fixture f = Fixture.mixedVat();
        stub(f);

        assertMatchesGolden("order-mixed-vat.snapshot", renderAndExtractFields(f.orderId()));
    }

    @Test
    @DisplayName("golden: gross price + customer number + long text (line break logic)")
    void goldenGrossAndWrapping() throws IOException {
        Fixture f = Fixture.grossPriceAndWrapping();
        stub(f);

        assertMatchesGolden("order-gross-wrapping.snapshot", renderAndExtractFields(f.orderId()));
    }

    @Test
    @DisplayName("golden: minimum order (no suppliers, no people, no addresses, no items)")
    void goldenMinimal() throws IOException {
        Fixture f = Fixture.minimal();
        stub(f);

        assertMatchesGolden("order-minimal.snapshot", renderAndExtractFields(f.orderId()));
    }

    @Test
    @DisplayName("Vat.equals fixed: two separate 19% instances now count as one rate")
    void goldenDuplicateVatInstances() throws IOException {
        Fixture f = Fixture.duplicateVatInstances();
        stub(f);

        Map<String, String> fields = renderAndExtractFields(f.orderId());

        // Q1 fixed: two Vat instances with the same numeric value are now equal,
        // so Set<Vat> has size 1 and the single-VAT branch is taken.
        assertThat(fields.get("Formular1[0].#subform[0].Body[0].Textfeld1[1]"))
                .as("Vat.equals fixed => Set size 1 => single-VAT branch, no mixed-VAT hint")
                .doesNotContain("Unterschiedlichen Mehrwertsteuersätze");

        assertMatchesGolden("order-duplicate-vat-instances.snapshot", fields);
    }

    // -------------------------------------------------------------------- Plumbing

    private void stub(Fixture f) {
        when(orderRepository.findById(f.orderId())).thenReturn(Optional.of(f.order));
        when(itemRepository.findByOrder_Id(f.orderId())).thenReturn(f.items);
        when(quotationRepository.getQuotationByOrderId(f.orderId())).thenReturn(f.quotations);
        when(orderService.getOrderNumber(f.order)).thenReturn(Optional.ofNullable(f.orderNumber));

        if (f.supplier != null) {
            when(supplierRepository.findById(f.supplier.getId())).thenReturn(Optional.of(f.supplier));
        }
        if (f.deliveryPerson != null) {
            when(personRepository.findById(f.deliveryPerson.getId())).thenReturn(Optional.of(f.deliveryPerson));
        }
        if (f.invoicePerson != null) {
            when(personRepository.findById(f.invoicePerson.getId())).thenReturn(Optional.of(f.invoicePerson));
        }
    }

    /**
     * Generates the PDF and provides a deterministic, sorted view of all
     * form fields.
     */
    private Map<String, String> renderAndExtractFields(Long orderId) throws IOException {
        byte[] pdf = service.generateOrderPDF(orderId);
        assertThat(pdf).isNotNull().isNotEmpty();

        try (PDDocument doc = Loader.loadPDF(pdf)) {
            PDAcroForm form = doc.getDocumentCatalog().getAcroForm();
            assertThat(form).as("generated PDF must still contain an AcroForm").isNotNull();
            assertThat(form.getXFA()).as("XFA must be removed").isNull();

            Map<String, String> fields = new TreeMap<>();
            for (PDField field : form.getFieldTree()) {
                if (field instanceof PDNonTerminalField) {
                    continue; // nur Blattfelder tragen Werte
                }
                fields.put(field.getFullyQualifiedName(), nullSafe(field.getValueAsString()));
            }
            assertThat(fields).as("The template should provide form fields").isNotEmpty();
            return fields;
        }
    }

    private void assertMatchesGolden(String name, Map<String, String> actual) throws IOException {
        String serialized = serialize(actual);

        if (Boolean.getBoolean("pdf.golden.update")) {
            Path target = Path.of("src", "test", "resources", GOLDEN_DIR, name);
            Files.createDirectories(target.getParent());
            Files.writeString(target, serialized, StandardCharsets.UTF_8);
            throw new AssertionError("Golden file regenerated: " + target
                    + " — Review the diff, then run it again without -Dpdf.golden.update.");
        }

        String expected = readGolden(name, serialized);
        if (!expected.equals(serialized)) {
            Path dump = writeActual(name, serialized);
            assertThat(serialized)
                    .as("PDF field values have changed (current status as of %s)", dump)
                    .isEqualTo(expected);
        }
    }

    private String readGolden(String name, String actualForBootstrap) throws IOException {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(GOLDEN_DIR + name)) {
            if (in == null) {
                Path dump = writeActual(name, actualForBootstrap);
                throw new AssertionError("Golden file missing: 'src/test/resources/" + GOLDEN_DIR + name
                        + "'. The current output is located in " + dump
                        + " — check it manually and apply it (or use -Dpdf.golden.update=true).");
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private Path writeActual(String name, String content) throws IOException {
        Files.createDirectories(ACTUAL_OUT);
        Path out = ACTUAL_OUT.resolve(name);
        Files.writeString(out, content, StandardCharsets.UTF_8);
        return out;
    }

    /**
     * One line per field; line breaks are escaped so that the snapshot
     * remains diff-friendly.
     */
    private static String serialize(Map<String, String> fields) {
        StringBuilder sb = new StringBuilder();
        fields.forEach((k, v) -> sb.append(k)
                .append(" = ")
                .append(v.replace("\\", "\\\\").replace("\r", "\\r").replace("\n", "\\n"))
                .append('\n'));
        return sb.toString();
    }

    private static String nullSafe(String s) {
        return s == null ? "" : s;
    }

    // -------------------------------------------------------------------- Fixtures

    private static final class Fixture {
        Order order;
        Supplier supplier;
        Person deliveryPerson;
        Person invoicePerson;
        List<Item> items = new ArrayList<>(); // MUTABLE: setItems() sorts in place
        List<Quotation> quotations = new ArrayList<>();
        String orderNumber = "IT_26_CC-1_7";

        Long orderId() {
            return order.getId();
        }

        static Fixture completeSingleVat() {
            Fixture f = new Fixture();
            f.order = baseOrder(100L);
            f.supplier = supplier();
            f.deliveryPerson = person(201L, "Erika", "Mustermann");
            f.invoicePerson = person(202L, "Max", "Mustermann");

            f.order.setSupplierId(f.supplier.getId());
            f.order.setDeliveryPersonId(f.deliveryPerson.getId());
            f.order.setInvoicePersonId(f.invoicePerson.getId());
            f.order.setQueriesPerson(person(203L, "Anna", "Beispiel"));
            f.order.setPercentageDiscount(new BigDecimal("10.00"));
            f.order.setCommentForSupplier("Bitte Lieferschein beilegen.");

            // IMPORTANT: a single VAT instance => Set<Vat>.size() == 1 =>
            // Single-VAT branch
            Vat vat19 = vat("19.00");
            f.items.add(item(100L, 1, "Laptop 14 Zoll", "1200.00", 2L, vat19, VatType.netto));
            f.items.add(item(100L, 2, "Dockingstation", "200.00", 1L, vat19, VatType.netto));

            f.quotations.add(quotation(100L, (short) 2, "Mitbewerber A GmbH", "2400.00", LocalDate.of(2026, 1, 5)));
            f.quotations.add(quotation(100L, (short) 3, "Mitbewerber B AG", "2550.50", LocalDate.of(2026, 1, 7)));
            return f;
        }

        static Fixture mixedVat() {
            Fixture f = completeSingleVat();
            f.items.add(item(100L, 3, "Fachbuch", "39.90", 3L, vat("7.00"), VatType.netto));
            return f;
        }

        static Fixture duplicateVatInstances() {
            Fixture f = new Fixture();
            f.order = baseOrder(101L);
            f.order.setPercentageDiscount(BigDecimal.ZERO);
            f.order.setCommentForSupplier("Nur ein Satz erwartet.");
            // two SEPARATE instances with the same value -> identity-based set
            f.items.add(item(101L, 1, "Maus", "20.00", 1L, vat("19.00"), VatType.netto));
            f.items.add(item(101L, 2, "Tastatur", "30.00", 1L, vat("19.00"), VatType.netto));
            return f;
        }

        static Fixture grossPriceAndWrapping() {
            Fixture f = new Fixture();
            f.order = baseOrder(102L);
            f.supplier = supplier();
            f.order.setSupplierId(f.supplier.getId());
            f.order.setPercentageDiscount(new BigDecimal("3.50"));
            f.order.setCommentForSupplier("Bruttopreise.");

            CustomerId customer = mock(CustomerId.class);
            when(customer.getCustomerId()).thenReturn("K-4711");
            f.order.setCustomer(customer);

            Vat vat19 = vat("19.00");
            f.items.add(item(102L, 1, "Bürostuhl mit Rückenlehne, höhenverstellbar, "
                    + "inkl. Armlehnen und fünf Jahren Garantie auf alle Verschleißteile",
                    "476.00", 2L, vat19, VatType.brutto));
            f.items.add(item(102L, 2, "Möbelstück", "119.00", 1L, vat19, VatType.brutto));
            return f;
        }

        static Fixture minimal() {
            Fixture f = new Fixture();
            f.order = baseOrder(103L);
            f.orderNumber = null; // getOrderNumber -> Optional.empty()
            f.order.setSupplierId(null);
            f.order.setDeliveryPersonId(null);
            f.order.setInvoicePersonId(null);
            f.order.setQueriesPerson(null);
            f.order.setQueriesPersonId(null);
            f.order.setDeliveryAddress(null);
            f.order.setInvoiceAddress(null);
            f.order.setPercentageDiscount(null);
            f.order.setCommentForSupplier(null);
            f.order.setQuoteNumber(null);
            f.order.setDecisionOtherReasonsDescription(null);
            return f;
        }

        // --- Building Blocks -------------------------------------------------------

        private static Order baseOrder(long id) {
            Order o = new Order();
            o.setId(id);
            o.setContentDescription("Golden Test Order");
            o.setStatus(OrderStatus.IN_PROGRESS);
            // Fixed timestamp -> deterministic “Date” field
            o.setCreatedDate(LocalDateTime.of(2026, 1, 15, 10, 30, 0));
            o.setBookingYear("26");
            o.setAutoIndex((short) 7);
            o.setQuoteNumber("ANG-2026-001");
            o.setPrimaryCostCenterId("CC-1");
            o.setSecondaryCostCenterId("CC-2");
            o.setDfgKey("DFG-1");
            o.setFlagDecisionCheapestOffer(true);
            o.setFlagDecisionMostEconomicalOffer(false);
            o.setFlagDecisionSoleSupplier(false);
            o.setFlagDecisionContractPartner(false);
            o.setFlagDecisionPreferredSupplierList(false);
            o.setFlagDecisionOtherReasons(true);
            o.setDecisionOtherReasonsDescription("Rahmenvertrag Hochschule");
            o.setDeliveryAddress(address("Flandernstraße", "101", "73732", "Esslingen"));
            o.setInvoiceAddress(address("Kanalstraße", "33", "73728", "Esslingen"));
            o.setApproval(approval()); // Required: setApprovalFlags does not check for null
            return o;
        }

        private static Approval approval() {
            Approval a = new Approval();
            a.setFlagEdvPermission(true);
            a.setFlagFurniturePermission(false);
            a.setFlagFurnitureRoom(false);
            a.setFlagInvestmentRoom(true);
            a.setFlagInvestmentStructuralMeasures(false);
            a.setFlagMediaPermission(false);
            return a;
        }

        private static Person person(Long id, String name, String surname) {
            Person p = new Person();
            p.setId(id);
            p.setName(name);
            p.setSurname(surname);
            p.setGender(Gender.f);
            p.setPhone("+49 711 397-0");
            p.setEmail(name.toLowerCase(Locale.ROOT) + "." + surname.toLowerCase(Locale.ROOT) + "@hs-esslingen.de");
            return p;
        }

        private static Supplier supplier() {
            Supplier s = new Supplier();
            s.setId(10);
            s.setName("Supplier GmbH");
            s.setEmail("supplier@example.com");
            s.setAddress(address("Industriestraße", "5", "70173", "Stuttgart"));
            return s;
        }

        private static Address address(String street, String no, String zip, String town) {
            Address a = new Address();
            a.setStreet(street);
            a.setBuildingNumber(no);
            a.setPostalCode(zip);
            a.setTown(town);
            return a;
        }

        private static Vat vat(String value) {
            Vat v = new Vat();
            v.setValue(new BigDecimal(value));
            v.setDescription("VAT " + value);
            return v;
        }

        private static Item item(Long orderId, int itemId, String name, String pricePerUnit,
                long quantity, Vat vat, VatType vatType) {
            Item i = new Item();
            i.setId(new ItemId(orderId, itemId));
            i.setName(name);
            i.setPricePerUnit(new BigDecimal(pricePerUnit));
            i.setQuantity(quantity);
            i.setQuantityUnit("Stk");
            i.setArticleId("A-" + itemId);
            i.setComment("");
            i.setVat(vat);
            i.setVatValue(vat.getValue());
            i.setVatType(vatType);
            i.setPreferredList(PreferredList.RZ);
            i.setPreferredListNumber("PL-" + itemId);
            i.setMigratedToInsy(false);
            return i;
        }

        private static Quotation quotation(Long orderId, short index, String company,
                String price, LocalDate date) {
            Quotation q = new Quotation();
            q.setId(new QuotationId(orderId, index)); // Required: getIndex() reads from the ID
            q.setCompanyName(company);
            q.setCompanyCity("Stuttgart");
            q.setPrice(new BigDecimal(price));
            q.setQuoteDate(date);
            return q;
        }
    }
}
