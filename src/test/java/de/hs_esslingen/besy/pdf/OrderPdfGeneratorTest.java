package de.hs_esslingen.besy.pdf;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import de.hs_esslingen.besy.enums.VatType;
import de.hs_esslingen.besy.exceptions.NotFoundException;
import de.hs_esslingen.besy.exceptions.PdfTemplateNotFoundException;
import de.hs_esslingen.besy.models.Approval;
import de.hs_esslingen.besy.models.Item;
import de.hs_esslingen.besy.models.ItemId;
import de.hs_esslingen.besy.models.Order;
import de.hs_esslingen.besy.models.Vat;
import de.hs_esslingen.besy.services.OrderService;

/**
 * ORCHESTRATION TEST for {@link OrderPdfGenerator}.
 *
 * Deliberately NO field value assertions: the field values are fully covered by
 * {@link OrderPdfGoldenTest} (snapshots). This test only checks
 * what the Orchestrator itself is responsible for:
 *
 * <ul>
 * <li>the order: Template -> Data -> Number -> Writer (fixed, because the
 * error sequence and the side effects of the PDFOrder setter depend on it
 * ),</li>
 * <li>exactly what is passed to the {@link OrderPdfFormWriter},</li>
 * <li>that the {@link PDDocument} is closed in EVERY case,</li>
 * <li>that XFA is removed and valid bytes are returned.</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class OrderPdfGeneratorTest {

    private static final Long ORDER_ID = 100L;

    @Mock
    private OrderPdfDataLoader dataLoader;
    @Mock
    private OrderService orderService;
    @Mock
    private PdfTemplateLoader templateLoader;
    @Mock
    private OrderPdfFormWriter formWriter;

    private OrderPdfProperties properties;
    private OrderPdfGenerator generator;

    private Order order;
    private OrderPdfData data;

    @BeforeEach
    void setUp() {
        properties = OrderPdfProperties.defaults();
        generator = new OrderPdfGenerator(dataLoader, orderService, properties, templateLoader, formWriter);

        order = new Order();
        order.setId(ORDER_ID);
        order.setPercentageDiscount(BigDecimal.valueOf(10));

        // a shared VAT instance -> single-VAT branch (see the "VAT equals" quirk)
        Vat vat19 = vat(BigDecimal.valueOf(19));
        List<Item> items = new ArrayList<>(); // MUTABLE: setItems() sorts in place
        items.add(item(1, BigDecimal.valueOf(10), 2L, vat19));
        items.add(item(2, BigDecimal.valueOf(5), 4L, vat19));

        data = new OrderPdfData(
                order,
                Optional.empty(),
                new Approval(),
                items,
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                List.of());
    }

    // ------------------------------------------------------------ Orchestration

    @Test
    @DisplayName("Ordering: Template -> Data -> Order Number -> Writer")
    void keeps_the_frozen_call_order() throws IOException {
        PDDocument document = stubTemplate();
        stubData("IT_25_CC-1_7");

        generator.generate(ORDER_ID);

        InOrder inOrder = inOrder(templateLoader, dataLoader, orderService, formWriter);
        inOrder.verify(templateLoader).loadOrderTemplate();
        inOrder.verify(dataLoader).load(ORDER_ID);
        inOrder.verify(orderService).getOrderNumber(order);
        inOrder.verify(formWriter).write(any(PDFOrder.class), eq(data), any(OrderPdfTotals.class),
                eq("IT_25_CC-1_7"));
        verify(document).close();
    }

    @Test
    @DisplayName("passes loaded data, calculated totals and order number to the writer")
    void passes_data_totals_and_order_number_to_the_writer() throws IOException {
        stubTemplate();
        stubData("IT_25_CC-1_7");

        generator.generate(ORDER_ID);

        ArgumentCaptor<OrderPdfTotals> totals = ArgumentCaptor.forClass(OrderPdfTotals.class);
        verify(formWriter).write(any(PDFOrder.class), eq(data), totals.capture(), eq("IT_25_CC-1_7"));

        OrderPdfTotals captured = totals.getValue();
        // 10*2 + 5*4 = 40; minus 10% = 36; + 19% MwSt = 42,84
        assertThat(captured.subTotal()).isEqualByComparingTo("40");
        assertThat(captured.netTotal()).isEqualByComparingTo("36");
        assertThat(captured.total()).isPresent().get(org.assertj.core.api.InstanceOfAssertFactories.BIG_DECIMAL)
                .isEqualByComparingTo("42.84");
        assertThat(captured.vats()).hasSize(1);
    }

    @Test
    @DisplayName("no order number available -> empty string")
    void uses_empty_string_when_no_order_number_exists() throws IOException {
        stubTemplate();
        stubData(null);

        generator.generate(ORDER_ID);

        verify(formWriter).write(any(PDFOrder.class), eq(data), any(OrderPdfTotals.class), eq(""));
    }

    // ------------------------------------------------------------------- Results

    @Test
    @DisplayName("returns a loadable PDF with AcroForm and without XFA")
    void returns_a_loadable_pdf_without_xfa() throws IOException {
        stubTemplate();
        stubData("IT_25_CC-1_7");

        byte[] pdf = generator.generate(ORDER_ID);

        assertThat(pdf).isNotNull().isNotEmpty();
        try (PDDocument reloaded = Loader.loadPDF(pdf)) {
            PDAcroForm form = reloaded.getDocumentCatalog().getAcroForm();
            assertThat(form).isNotNull();
            assertThat(form.getXFA()).isNull();
        }
    }

    // --------------------------------------------------------- Error Handling

    @Test
    @DisplayName("Error in DataLoader: Exception propagated, but the document is closed anyway")
    void closes_the_document_when_loading_the_data_fails() throws IOException {
        PDDocument document = stubTemplate();
        when(dataLoader.load(ORDER_ID)).thenThrow(new NotFoundException("Order with id 100 does not exist."));

        assertThatThrownBy(() -> generator.generate(ORDER_ID))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("does not exist");

        verify(document).close();
        verifyNoInteractions(orderService, formWriter);
    }

    @Test
    @DisplayName("A missing template causes an error BEFORE data is loaded (frozen sequence)")
    void fails_before_touching_the_data_loader_when_the_template_is_missing() throws IOException {
        when(templateLoader.loadOrderTemplate())
                .thenThrow(new PdfTemplateNotFoundException("static/does-not-exist.pdf"));

        assertThatThrownBy(() -> generator.generate(ORDER_ID))
                .isInstanceOf(PdfTemplateNotFoundException.class)
                .hasMessageContaining("static/does-not-exist.pdf");

        verifyNoInteractions(dataLoader, orderService, formWriter);
    }

    // -------------------------------------------------------------------- Helpers

    /**
     * Actual template, as a "spy" — this allows to verify {@code close()} without
     * having to recreate PDFBox's internal workings.
     */
    private PDDocument stubTemplate() throws IOException {
        PDDocument document = spy(new PdfTemplateLoader(properties).loadOrderTemplate());
        when(templateLoader.loadOrderTemplate()).thenReturn(document);
        return document;
    }

    private void stubData(String orderNumber) {
        when(dataLoader.load(ORDER_ID)).thenReturn(data);
        when(orderService.getOrderNumber(order)).thenReturn(Optional.ofNullable(orderNumber));
    }

    private static Vat vat(BigDecimal value) {
        Vat v = new Vat();
        v.setValue(value);
        v.setDescription("VAT " + value);
        return v;
    }

    private static Item item(int itemId, BigDecimal pricePerUnit, long quantity, Vat vat) {
        Item i = new Item();
        i.setId(new ItemId(ORDER_ID, itemId));
        i.setName("Item " + itemId);
        i.setPricePerUnit(pricePerUnit);
        i.setQuantity(quantity);
        i.setQuantityUnit("Stk");
        i.setVat(vat);
        i.setVatValue(vat.getValue());
        i.setVatType(VatType.netto);
        return i;
    }

    private static Locale previousDefaultLocale;

    @org.junit.jupiter.api.BeforeAll
    static void forceGermanLocale() {
        previousDefaultLocale = Locale.getDefault();
        Locale.setDefault(Locale.GERMANY);
    }

    @org.junit.jupiter.api.AfterAll
    static void restoreDefaultLocale() {
        Locale.setDefault(previousDefaultLocale);
    }
}
