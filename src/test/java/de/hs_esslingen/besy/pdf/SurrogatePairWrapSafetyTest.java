package de.hs_esslingen.besy.pdf;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.hs_esslingen.besy.enums.VatType;
import de.hs_esslingen.besy.models.Item;
import de.hs_esslingen.besy.models.ItemId;
import de.hs_esslingen.besy.models.Vat;

class SurrogatePairWrapSafetyTest {

    private PDDocument document;
    private PDFOrder order;

    private Method avoidSurrogateSplit;
    private Method firstCodePointLength;
    private Method findMaxFittingPrefixLength;
    private Method wrapItem;
    private Method getStringWidth;

    @BeforeEach
    void setUp() throws Exception {
        OrderPdfProperties properties = OrderPdfProperties.defaults();
        PdfTemplateLoader loader = new PdfTemplateLoader(properties);
        document = loader.loadOrderTemplate();
        PDAcroForm acroForm = document.getDocumentCatalog().getAcroForm();
        order = new PDFOrder().parseOrder(acroForm);

        avoidSurrogateSplit = PDFOrder.class.getDeclaredMethod("avoidSurrogateSplit", String.class, int.class);
        avoidSurrogateSplit.setAccessible(true);

        firstCodePointLength = PDFOrder.class.getDeclaredMethod("firstCodePointLength", String.class);
        firstCodePointLength.setAccessible(true);

        findMaxFittingPrefixLength = PDFOrder.class.getDeclaredMethod("findMaxFittingPrefixLength", String.class,
                float.class);
        findMaxFittingPrefixLength.setAccessible(true);

        wrapItem = PDFOrder.class.getDeclaredMethod("wrapItem", Item.class);
        wrapItem.setAccessible(true);

        getStringWidth = PDFOrder.class.getDeclaredMethod("getStringWidth", String.class);
        getStringWidth.setAccessible(true);
    }

    @AfterEach
    void tearDown() throws Exception {
        document.close();
    }

    // ----------------------------------------------------------
    // avoidSurrogateSplit

    @Test
    void avoidSurrogateSplit_movesBackWhenIndexSplitsAPair() throws Exception {
        String text = "AB😀CD"; // 😀 = high surrogate at index 2, low surrogate at index 3
        int result = (int) avoidSurrogateSplit.invoke(order, text, 3);
        assertThat(result).isEqualTo(2);
    }

    @Test
    void avoidSurrogateSplit_leavesSafeIndicesUnchanged() throws Exception {
        String text = "AB😀CD";
        assertThat((int) avoidSurrogateSplit.invoke(order, text, 2)).isEqualTo(2); // before the pair
        assertThat((int) avoidSurrogateSplit.invoke(order, text, 4)).isEqualTo(4); // after the pair
        assertThat((int) avoidSurrogateSplit.invoke(order, text, 0)).isZero(); // string start
        assertThat((int) avoidSurrogateSplit.invoke(order, text, text.length())).isEqualTo(text.length()); // end
    }

    // ----------------------------------------------------------
    // firstCodePointLength

    @Test
    void firstCodePointLength_returnsTwoForLeadingSurrogatePair() throws Exception {
        assertThat((int) firstCodePointLength.invoke(order, "😀 rest")).isEqualTo(2);
    }

    @Test
    void firstCodePointLength_returnsOneForOrdinaryLeadingChar() throws Exception {
        assertThat((int) firstCodePointLength.invoke(order, "A rest")).isEqualTo(1);
        assertThat((int) firstCodePointLength.invoke(order, "A")).isEqualTo(1);
    }

    // ----------------------------------------------------------
    // findMaxFittingPrefixLength (exhaustive)

    @Test
    void findMaxFittingPrefixLength_neverSplitsASurrogatePair_forAnyBudget() throws Exception {
        String text = "AAAA😀BBBB😀CCCC";
        float fullWidth = (float) getStringWidth.invoke(order, text);

        for (float maxWidth = 0f; maxWidth <= fullWidth + 1f; maxWidth += 0.5f) {
            int index = (int) findMaxFittingPrefixLength.invoke(order, text, maxWidth);
            assertThat(splitsAPair(text, index))
                    .as("maxWidth=%s must not produce a cut index (%s) that splits a surrogate pair", maxWidth,
                            index)
                    .isFalse();
        }
    }

    // ---------------------------------------------------------- wrapItem
    // end-to-end

    @Test
    void wrapItem_neverProducesALineWithALoneSurrogate() throws Exception {
        // Sweep the emoji's position across a long description via variable
        // padding, so the natural (unprotected) cut point would, for at
        // least some padding length, land inside the pair if the guard
        // were missing (or became load-bearing after a font change).
        for (int padding = 0; padding < 80; padding++) {
            String description = "X".repeat(padding) + "😀" + "Y".repeat(80 - padding)
                    + " weiterer Text der garantiert zu einem Zeilenumbruch fuehrt und diesen Satz verlaengert";

            Item item = new Item();
            item.setId(new ItemId(999L, 1));
            item.setName(description);
            item.setQuantity(1L);
            item.setPricePerUnit(new BigDecimal("1.00"));
            item.setVat(vat());
            item.setVatType(VatType.netto);

            @SuppressWarnings("unchecked")
            List<Item> wrapped = (List<Item>) wrapItem.invoke(order, item);

            for (Item line : wrapped) {
                assertThat(containsLoneSurrogate(line.getName()))
                        .as("padding=%d produced a line with a lone surrogate: %s", padding, line.getName())
                        .isFalse();
            }
        }
    }

    private Vat vat() {
        Vat v = new Vat();
        v.setValue(new BigDecimal("19.00"));
        v.setDescription("VAT 19.00");
        return v;
    }

    private boolean splitsAPair(String text, int index) {
        return index > 0 && index < text.length()
                && Character.isHighSurrogate(text.charAt(index - 1))
                && Character.isLowSurrogate(text.charAt(index));
    }

    private boolean containsLoneSurrogate(String s) {
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (Character.isHighSurrogate(c)) {
                if (i + 1 >= s.length() || !Character.isLowSurrogate(s.charAt(i + 1))) {
                    return true;
                }
                i++;
            } else if (Character.isLowSurrogate(c)) {
                return true;
            }
        }
        return false;
    }
}
