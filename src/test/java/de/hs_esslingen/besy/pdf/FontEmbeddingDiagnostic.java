package de.hs_esslingen.besy.pdf;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.fontbox.ttf.TTFParser;
import org.apache.fontbox.ttf.TrueTypeFont;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.io.RandomAccessReadBufferedFile;
import org.apache.pdfbox.pdmodel.PDAppearanceContentStream;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDFontDescriptor;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceDictionary;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceStream;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDTextField;
import org.junit.jupiter.api.Test;

/**
 * THROWAWAY / READ-ONLY DIAGNOSTIC for Commit 4 (embedding a Unicode-capable
 * fallback font). Not part of the permanent suite; does not modify
 * production code. Validates, with real font files, the assumptions behind
 * the "always embed Latin/Greek/Cyrillic, embed CJK/emoji only on demand,
 * rely on PDFBox subsetting to keep output small" plan.
 *
 * <p>
 * <strong>Setup required before running:</strong> place the following font
 * files under {@code src/test/resources/fonts/} (not checked in -- these are
 * only needed to run this diagnostic; see licensing note below):
 * <ul>
 * <li>{@code NotoSans-Regular.ttf}, {@code NotoSans-Bold.ttf} -- from
 * https://fonts.google.com/noto/specimen/Noto+Sans (Latin/Greek/Cyrillic
 * coverage; "Download family" gives static TTFs)</li>
 * <li>{@code NotoSansSC-Regular.ttf} -- from
 * https://fonts.google.com/noto/specimen/Noto+Sans+SC (Simplified Chinese)</li>
 * <li>{@code NotoEmoji-Regular.ttf} -- from
 * https://fonts.google.com/noto/specimen/Noto+Emoji (monochrome/outline
 * emoji, NOT Noto Color Emoji)</li>
 * </ul>
 * All under SIL Open Font License 1.1. Tests that reference a missing file
 * are skipped (not failed) via {@link org.junit.jupiter.api.Assumptions},
 * with a message telling you what to add.
 *
 * <p>
 * Run in isolation:
 * {@code mvn -q test-compile && mvn test -Dtest=FontEmbeddingDiagnosticTest}
 */
class FontEmbeddingDiagnostic {

    private static final Path FONT_DIR = Path.of("src", "test", "resources", "fonts");
    private static final Path NOTO_SANS_REGULAR = FONT_DIR.resolve("NotoSans-Regular.ttf");
    private static final Path NOTO_SANS_BOLD = FONT_DIR.resolve("NotoSans-Bold.ttf");
    private static final Path NOTO_SANS_SC = FONT_DIR.resolve("NotoSansSC-Regular.ttf");
    private static final Path NOTO_EMOJI = FONT_DIR.resolve("NotoEmoji-Regular.ttf");

    private static final String TARGET_FIELD = "Formular1[0].#subform[0].Header[0].Textfeld1[0]"; // companyAddress

    // ------------------------------------------------------------- 0) Raw file
    // sizes

    @Test
    void printRawFontFileSizes() throws IOException {
        System.out.println("=== Raw font file sizes on disk (input, NOT what ends up in the PDF) ===");
        printSizeIfPresent("Noto Sans Regular", NOTO_SANS_REGULAR);
        printSizeIfPresent("Noto Sans Bold", NOTO_SANS_BOLD);
        printSizeIfPresent("Noto Sans SC Regular (CJK)", NOTO_SANS_SC);
        printSizeIfPresent("Noto Emoji Regular", NOTO_EMOJI);
    }

    private void printSizeIfPresent(String label, Path path) throws IOException {
        if (Files.exists(path)) {
            System.out.printf("  %-28s %8.1f KB  (%s)%n", label, Files.size(path) / 1024.0, path);
        } else {
            System.out.printf("  %-28s MISSING -- see class Javadoc for download instructions (%s)%n", label, path);
        }
    }

    // ------------------------------------------------------------- 1) Subsetting:
    // Latin Extended

    @Test
    void embeddingNotoSansWithSubsetting_keepsOutputSmall_forLatinExtendedText() throws IOException {
        assumeTrue(Files.exists(NOTO_SANS_REGULAR),
                "Skipped: " + NOTO_SANS_REGULAR + " not present -- see class Javadoc.");

        String probe = "Müller-Lüdenscheid Straße Ñoño Café à la carte";
        byte[] baseline = renderBaseline();
        byte[] withFont = renderWithEmbeddedFont(NOTO_SANS_REGULAR, "NotoSansDiag", probe);

        System.out.println("=== Subsetting: Latin Extended probe (\"" + probe + "\") ===");
        System.out.printf("  Raw NotoSans-Regular.ttf size:      %8.1f KB%n", Files.size(NOTO_SANS_REGULAR) / 1024.0);
        System.out.printf("  Baseline PDF (no embedded font):    %8.1f KB%n", baseline.length / 1024.0);
        System.out.printf("  PDF with subset-embedded NotoSans:  %8.1f KB%n", withFont.length / 1024.0);
        System.out.printf("  Delta (subset font's real cost):    %8.1f KB%n",
                (withFont.length - baseline.length) / 1024.0);

        // Sanity: the field value itself must still round-trip correctly.
        assertFieldValueRoundTrips(withFont, probe);
    }

    // ------------------------------------------------------------- 2) Subsetting:
    // CJK

    @Test
    void embeddingNotoSansSC_withSubsetting_keepsOutputSmall_forThreeCjkCharacters() throws IOException {
        assumeTrue(Files.exists(NOTO_SANS_SC),
                "Skipped: " + NOTO_SANS_SC + " not present -- see class Javadoc.");

        String probe = "中文测";
        byte[] baseline = renderBaseline();
        byte[] withFont = renderWithEmbeddedFont(NOTO_SANS_SC, "NotoSansSCDiag", probe);

        System.out.println("=== Subsetting: CJK probe (\"" + probe + "\", 3 characters) ===");
        System.out.printf("  Raw NotoSansSC-Regular.ttf size:    %8.1f KB (%.1f MB)%n",
                Files.size(NOTO_SANS_SC) / 1024.0, Files.size(NOTO_SANS_SC) / 1024.0 / 1024.0);
        System.out.printf("  Baseline PDF (no embedded font):    %8.1f KB%n", baseline.length / 1024.0);
        System.out.printf("  PDF with subset-embedded NotoSansSC (3 glyphs): %8.1f KB%n", withFont.length / 1024.0);
        System.out.printf("  Delta (subset font's real cost):    %8.1f KB%n",
                (withFont.length - baseline.length) / 1024.0);
        System.out.println("  (Expect delta to be tiny relative to the raw font size above -- "
                + "that is the whole point of subsetting: only the 3 used glyphs are kept.)");

        assertFieldValueRoundTrips(withFont, probe);
    }

    // ------------------------------------------------------------- 3) Emoji
    // coverage + subsetting

    @Test
    void embeddingNotoEmoji_withSubsetting_rendersMonochromeOutlineInsteadOfPlaceholder() throws IOException {
        assumeTrue(Files.exists(NOTO_EMOJI),
                "Skipped: " + NOTO_EMOJI + " not present -- see class Javadoc.");

        String probe = "😀";
        byte[] baseline = renderBaseline();
        byte[] withFont = renderWithEmbeddedFont(NOTO_EMOJI, "NotoEmojiDiag", probe);

        System.out.println("=== Subsetting: single emoji probe (\"" + probe + "\") ===");
        System.out.printf("  Raw NotoEmoji-Regular.ttf size:     %8.1f KB%n", Files.size(NOTO_EMOJI) / 1024.0);
        System.out.printf("  Baseline PDF (no embedded font):    %8.1f KB%n", baseline.length / 1024.0);
        System.out.printf("  PDF with subset-embedded NotoEmoji (1 glyph): %8.1f KB%n", withFont.length / 1024.0);

        assertFieldValueRoundTrips(withFont, probe);
    }

    // ------------------------------------------------------------- 4) Glyph
    // coverage probes

    @Test
    void glyphCoverageProbesAgainstCandidateFonts() throws IOException {
        System.out.println("=== Glyph coverage probes against candidate embedded fonts ===");
        probeIfPresent("Noto Sans (Latin/Greek/Cyrillic)", NOTO_SANS_REGULAR,
                "ä", "ß", "é", "ñ", "α", "β", "д", "ж", "中", "😀");
        probeIfPresent("Noto Sans SC (CJK)", NOTO_SANS_SC,
                "中", "文", "測", "ä", "😀");
        probeIfPresent("Noto Emoji", NOTO_EMOJI,
                "😀", "😂", "🎉", "ä", "中");
    }

    private void probeIfPresent(String label, Path fontPath, String... probes) throws IOException {
        if (!Files.exists(fontPath)) {
            System.out.println("  [" + label + "] SKIPPED -- font file missing (" + fontPath + ")");
            return;
        }
        try (PDDocument doc = new PDDocument()) {
            PDFont font = PDType0Font.load(doc, new FileInputStream(fontPath.toFile()), true);
            System.out.println("  --- " + label + " ---");
            for (String probe : probes) {
                try {
                    font.encode(probe);
                    System.out.println("    [OK]   U+" + Integer.toHexString(probe.codePointAt(0)).toUpperCase()
                            + " (\"" + probe + "\")");
                } catch (IllegalArgumentException e) {
                    System.out.println("    [FAIL] U+" + Integer.toHexString(probe.codePointAt(0)).toUpperCase()
                            + " (\"" + probe + "\") -> " + e.getMessage());
                }
            }
        }
    }

    // ------------------------------------------------------------- 5)
    // Parse-once-reuse experiment
    //
    // Scoped deliberately to the plain-TrueType Latin font only
    // (NotoSans-Regular.ttf),
    // to isolate the "can a parsed font program be reused across documents"
    // question from OTF/CFF parsing details, which are orthogonal to it.

    @Test
    void reusingParsedTrueTypeFontProgramAcrossDocuments_timingAndCorrectness() throws IOException {
        assumeTrue(Files.exists(NOTO_SANS_REGULAR),
                "Skipped: " + NOTO_SANS_REGULAR + " not present -- see class Javadoc.");

        String probeA = "Müller GmbH";
        String probeB = "Café Schön AG";

        // --- Approach A: re-parse the font file from bytes for each document ---
        long startA = System.nanoTime();
        byte[] outA1 = renderWithEmbeddedFont(NOTO_SANS_REGULAR, "NotoSansDiag", probeA);
        byte[] outA2 = renderWithEmbeddedFont(NOTO_SANS_REGULAR, "NotoSansDiag", probeB);
        long durationA = System.nanoTime() - startA;

        // --- Approach B: parse the TrueType program once, reuse it for two documents
        // ---
        long startB = System.nanoTime();
        byte[] outB1;
        byte[] outB2;
        try (var raf1 = new RandomAccessReadBufferedFile(NOTO_SANS_REGULAR.toFile())) {
            TrueTypeFont ttf = new TTFParser().parse(raf1);
            try (PDDocument doc1 = openTemplate();
                    PDDocument doc2 = openTemplate()) {

                PDFont font1 = PDType0Font.load(doc1, ttf, true);
                PDFont font2 = PDType0Font.load(doc2, ttf, true);

                writeProbeIntoField(doc1, font1, "NotoSansDiag", probeA);
                writeProbeIntoField(doc2, font2, "NotoSansDiag", probeB);

                outB1 = toBytes(doc1);
                outB2 = toBytes(doc2);
            }
            // ttf must stay open for both saves above (subsetting reads glyph data
            // at save() time), only close it once both documents are saved.
            ttf.close();
        }
        long durationB = System.nanoTime() - startB;

        System.out.println("=== Parse-once-and-reuse vs. re-parse-per-document ===");
        System.out.printf("  Approach A (re-parse per document): %6.1f ms%n", durationA / 1_000_000.0);
        System.out.printf("  Approach B (parse once, reuse):     %6.1f ms%n", durationB / 1_000_000.0);
        System.out.printf("  Speedup factor:                     %6.2fx%n", (double) durationA / durationB);

        // Correctness: each output must independently round-trip its own probe,
        // with no cross-contamination between the two documents that shared the
        // parsed TrueTypeFont instance.
        assertFieldValueRoundTrips(outA1, probeA);
        assertFieldValueRoundTrips(outA2, probeB);
        assertFieldValueRoundTrips(outB1, probeA);
        assertFieldValueRoundTrips(outB2, probeB);

        System.out.println("  Correctness: all four outputs round-tripped their own probe value independently.");
    }

    // ------------------------------------------------------------- 6) Isolate:
    // subsetting vs. general CID-keyed issue

    @Test
    void embeddingNotoSansSC_withoutSubsetting() throws IOException {
        assumeTrue(Files.exists(NOTO_SANS_SC),
                "Skipped: " + NOTO_SANS_SC + " not present -- see class Javadoc.");
        runIsolationProbe("NotoSansSC (no subsetting)", NOTO_SANS_SC, "NotoSansSCDiagFull", "中文测", false);
    }

    @Test
    void embeddingNotoEmoji_withoutSubsetting() throws IOException {
        assumeTrue(Files.exists(NOTO_EMOJI),
                "Skipped: " + NOTO_EMOJI + " not present -- see class Javadoc.");
        runIsolationProbe("NotoEmoji (no subsetting)", NOTO_EMOJI, "NotoEmojiDiagFull", "😀", false);
    }

    @Test
    void embeddingNotoSansSC_inPlainDocument_withSubsetting() throws IOException {
        assumeTrue(Files.exists(NOTO_SANS_SC),
                "Skipped: " + NOTO_SANS_SC + " not present -- see class Javadoc.");
        // Same subsetting flag as the failing test, but using a bare PDDocument
        // (no AcroForm/template context) -- isolates whether the template
        // context matters at all.
        try (PDDocument doc = new PDDocument()) {
            PDFont font = PDType0Font.load(doc, new java.io.FileInputStream(NOTO_SANS_SC.toFile()), true);
            System.out.println("=== NotoSansSC in plain PDDocument, subsetting=true ===");
            try {
                float width = font.getStringWidth("中");
                System.out.println("  getStringWidth(\"中\") succeeded: " + width
                        + " (no fallback-font warning above => hypothesis 2 is wrong, template context mattered)");
            } catch (Exception e) {
                System.out.println("  getStringWidth(\"中\") FAILED: " + e
                        + " (fails even in a bare PDDocument => hypothesis 1, template context is irrelevant)");
            }
        }
    }

    private void runIsolationProbe(String label, Path fontFile, String resourceName, String probe,
            boolean embedSubset) throws IOException {
        System.out.println("=== " + label + " ===");
        try (PDDocument doc = openTemplate()) {
            PDFont font = PDType0Font.load(doc, new java.io.FileInputStream(fontFile.toFile()), embedSubset);
            try {
                writeProbeIntoField(doc, font, resourceName, probe);
                byte[] out = toBytes(doc);
                System.out.println("  SUCCESS, output size: " + out.length / 1024.0 + " KB");
                assertFieldValueRoundTrips(out, probe);
            } catch (Exception e) {
                System.out.println("  FAILED: " + e);
            }
        }
    }
    // ------------------------------------------------------------- 7) Does
    // AcroForm
    // appearance generation resolve a DIFFERENT (broken) font instance than the one
    // we hold, even before save()?

    @Test
    void freshlyResolvedFontFromResources_comparedToOriginalInstance() throws IOException {
        assumeTrue(Files.exists(NOTO_SANS_REGULAR) && Files.exists(NOTO_SANS_SC),
                "Skipped: NotoSans-Regular.ttf or NotoSansSC-Regular.ttf missing.");

        compareOriginalVsResolvedFont("NotoSans-Regular (Latin)", NOTO_SANS_REGULAR, "NotoSansFreshDiag", "é");
        compareOriginalVsResolvedFont("NotoSansSC-Regular (CJK)", NOTO_SANS_SC, "NotoSansSCFreshDiag", "中");
    }

    private void compareOriginalVsResolvedFont(String label, Path fontFile, String resourceName, String probeChar)
            throws IOException {
        System.out.println("=== " + label + ": original vs. freshly-resolved-from-DR font instance ===");
        try (PDDocument doc = openTemplate()) {
            PDFont originalFont = PDType0Font.load(doc, new FileInputStream(fontFile.toFile()), true);
            PDAcroForm acroForm = doc.getDocumentCatalog().getAcroForm();
            PDResources dr = acroForm.getDefaultResources();
            dr.put(COSName.getPDFName(resourceName), originalFont);

            // Re-fetch a font object for the SAME resource name/COS dictionary --
            // simulating what AppearanceGeneratorHelper does internally when it
            // resolves the font referenced by a field's /DA string against /DR,
            // rather than reusing our exact Java object.
            PDFont resolvedFont = dr.getFont(COSName.getPDFName(resourceName));

            System.out.println("  Same Java instance as original? " + (originalFont == resolvedFont));

            try {
                float widthOriginal = originalFont.getStringWidth(probeChar);
                System.out.println("  originalFont.getStringWidth(\"" + probeChar + "\") = " + widthOriginal);
            } catch (Exception e) {
                System.out.println("  originalFont.getStringWidth(\"" + probeChar + "\") FAILED: " + e);
            }

            try {
                float widthResolved = resolvedFont.getStringWidth(probeChar);
                System.out.println("  resolvedFont.getStringWidth(\"" + probeChar + "\") = " + widthResolved);
            } catch (Exception e) {
                System.out.println("  resolvedFont.getStringWidth(\"" + probeChar + "\") FAILED: " + e);
            }
        }
    }

    // ------------------------------------------------------------- 8) Which font
    // ACTUALLY ended up referenced in the generated appearance stream for the
    // "successful" Latin case -- our embedded font, or a silent fallback?

    @Test
    void inspectActualFontResourceUsedInGeneratedAppearanceStream() throws IOException {
        assumeTrue(Files.exists(NOTO_SANS_REGULAR),
                "Skipped: " + NOTO_SANS_REGULAR + " not present.");

        String probe = "Müller Café";
        byte[] pdfBytes = renderWithEmbeddedFont(NOTO_SANS_REGULAR, "NotoSansDiag", probe);

        try (PDDocument reloaded = Loader.loadPDF(pdfBytes)) {
            PDAcroForm form = reloaded.getDocumentCatalog().getAcroForm();
            PDTextField field = (PDTextField) form.getField(TARGET_FIELD);

            var widget = field.getWidgets().get(0);
            var appearanceStream = widget.getAppearance().getNormalAppearance().getAppearanceStream();

            System.out.println("=== Appearance stream content (raw operators) for " + TARGET_FIELD + " ===");
            try (var is = appearanceStream.getContentStream().createInputStream()) {
                String content = new String(is.readAllBytes(), java.nio.charset.StandardCharsets.ISO_8859_1);
                System.out.println(content);
            }

            System.out.println("=== Fonts present in reloaded AcroForm /DR ===");
            PDResources dr = form.getDefaultResources();
            for (COSName name : dr.getFontNames()) {
                PDFont f = dr.getFont(name);
                System.out.println("  /" + name.getName() + " -> BaseFont=" + f.getName()
                        + " Subtype=" + f.getCOSObject().getNameAsString(COSName.SUBTYPE));
            }
        }
    }

    // ------------------------------------------------------------- 7) Manual
    // appearance stream: bypass AppearanceGeneratorHelper entirely for CJK

    @Test
    void manuallyBuiltAppearanceStream_bypassesAutomaticGenerator_forCjkText() throws IOException {
        assumeTrue(Files.exists(NOTO_SANS_SC), "Skipped: " + NOTO_SANS_SC + " not present.");

        try (PDDocument doc = openTemplate()) {
            PDAcroForm acroForm = doc.getDocumentCatalog().getAcroForm();
            PDResources dr = acroForm.getDefaultResources();
            PDFont font = PDType0Font.load(doc, new FileInputStream(NOTO_SANS_SC.toFile()), true);
            dr.put(COSName.getPDFName("NotoSansSCDiag"), font);

            PDTextField field = (PDTextField) acroForm.getField(TARGET_FIELD);
            field.setDefaultAppearance("/NotoSansSCDiag 10 Tf 0 g");

            // Set the raw /V directly -- deliberately NOT calling setValue(),
            // so PDTextField.constructAppearances()/AppearanceGeneratorHelper
            // (which re-resolves the font from /DR, hitting the fallback bug)
            // is never invoked.
            field.getCOSObject().setString(org.apache.pdfbox.cos.COSName.V, "中文测");

            var widget = field.getWidgets().get(0);
            org.apache.pdfbox.pdmodel.common.PDRectangle rect = widget.getRectangle();

            org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceStream appearance = new org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceStream(
                    doc);
            PDResources apResources = new PDResources();
            apResources.put(COSName.getPDFName("F1"), font);
            appearance.setResources(apResources);
            appearance.setBBox(new org.apache.pdfbox.pdmodel.common.PDRectangle(rect.getWidth(), rect.getHeight()));

            System.out.println("=== Manual appearance stream, using `font` directly (never re-fetched from /DR) ===");
            try (org.apache.pdfbox.pdmodel.PDPageContentStream cs = new org.apache.pdfbox.pdmodel.PDPageContentStream(
                    doc, appearance)) {
                cs.beginText();
                cs.setFont(font, 10);
                cs.newLineAtOffset(2, 5);
                cs.showText("中文测"); // <-- the critical call: does THIS throw?
                cs.endText();
                System.out.println("  showText(\"中文测\") succeeded -- no fallback-font substitution here.");
            }

            var appearanceDict = widget.getAppearance();
            if (appearanceDict == null) {
                appearanceDict = new org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceDictionary();
                widget.setAppearance(appearanceDict);
            }
            appearanceDict.setNormalAppearance(appearance);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            doc.save(out);
            System.out.println("  SUCCESS: saved without crash, size=" + out.size() / 1024.0 + " KB");

            try (PDDocument reloaded = Loader.loadPDF(out.toByteArray())) {
                String val = reloaded.getDocumentCatalog().getAcroForm().getField(TARGET_FIELD).getValueAsString();
                System.out.println("  Round-tripped /V value: " + val + " (matches: " + "中文测".equals(val) + ")");
            }
        }
    }

    // ------------------------------------------------------------- 8) Concurrency
    // safety of sharing one parsed TrueTypeFont across simultaneous documents

    @Test
    void concurrentReuseOfParsedTrueTypeFontProgram_acrossThreads() throws Exception {
        assumeTrue(Files.exists(NOTO_SANS_REGULAR), "Skipped: NotoSans-Regular.ttf not present.");

        byte[] fontBytes = Files.readAllBytes(NOTO_SANS_REGULAR);
        TrueTypeFont sharedTtf;
        try (var raf = new org.apache.pdfbox.io.RandomAccessReadBuffer(fontBytes)) {
            sharedTtf = new TTFParser().parse(raf);
        }

        int threadCount = 8;
        var pool = java.util.concurrent.Executors.newFixedThreadPool(threadCount);
        java.util.List<String> probes = java.util.List.of(
                "Müller AG", "Café GmbH", "Straße KG", "Ñoño SE",
                "Schön OHG", "Björk Inc", "Zürich Co", "Vienna Ltd");
        java.util.List<java.util.concurrent.Future<byte[]>> futures = new java.util.ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            String probe = probes.get(i);
            futures.add(pool.submit(() -> {
                try (PDDocument doc = openTemplate()) {
                    PDFont font = PDType0Font.load(doc, sharedTtf, true);
                    writeProbeIntoField(doc, font, "NotoSansConcurrent", probe);
                    return toBytes(doc);
                }
            }));
        }

        boolean allCorrect = true;
        for (int i = 0; i < threadCount; i++) {
            byte[] pdfBytes = futures.get(i).get();
            try {
                assertFieldValueRoundTrips(pdfBytes, probes.get(i));
            } catch (AssertionError e) {
                allCorrect = false;
                System.out.println("  Thread " + i + " MISMATCH: " + e.getMessage());
            }
        }
        pool.shutdown();
        sharedTtf.close();

        System.out.println(
                "=== Concurrent reuse of ONE shared parsed TrueTypeFont across " + threadCount + " threads ===");
        System.out.println("  All threads produced their own correct, uncorrupted probe value: " + allCorrect);
        System.out.println("  (false => sharing a parsed font object across concurrent requests is NOT safe as-is;"
                + " would need per-request parsing or synchronization instead.)");
    }
    // ------------------------------------------------------------- 9) Workaround
    // A:
    // does forcing an indirect /DR font entry + an explicit ResourceCache make
    // dr.getFont() return OUR instance instead of rebuilding a broken one?

    @Test
    void indirectResourceCacheWorkaround_forCjkFont() throws IOException {
        assumeTrue(Files.exists(NOTO_SANS_SC), "Skipped: " + NOTO_SANS_SC + " not present.");

        try (PDDocument doc = openTemplate()) {
            System.out.println("=== Workaround A: indirect /DR font entry + explicit ResourceCache ===");

            PDAcroForm acroForm = doc.getDocumentCatalog().getAcroForm();
            PDResources dr = acroForm.getDefaultResources();
            if (dr == null) {
                dr = new PDResources();
                acroForm.setDefaultResources(dr);
            }

            PDFont font = PDType0Font.load(doc, new FileInputStream(NOTO_SANS_SC.toFile()), true);
            org.apache.pdfbox.cos.COSName fontName = COSName.getPDFName("NotoSansSCIndirect");

            // Force an INDIRECT COS reference for the font entry, mirroring what
            // PDResources.getFont() checks before deciding to rebuild the font
            // from scratch instead of returning a cached instance.
            org.apache.pdfbox.cos.COSDictionary fontsDict = (org.apache.pdfbox.cos.COSDictionary) dr.getCOSObject()
                    .getDictionaryObject(COSName.FONT);
            if (fontsDict == null) {
                fontsDict = new org.apache.pdfbox.cos.COSDictionary();
                dr.getCOSObject().setItem(COSName.FONT, fontsDict);
            }
            org.apache.pdfbox.cos.COSObject indirectFontRef = new org.apache.pdfbox.cos.COSObject(font.getCOSObject());
            fontsDict.setItem(fontName, indirectFontRef);

            // NOTE: uncertain API -- PDDocument may not expose setResourceCache()
            // directly in this PDFBox version. If this line fails to compile,
            // report the exact error; that alone answers whether this workaround
            // is even reachable here.
            org.apache.pdfbox.pdmodel.ResourceCache cache = new org.apache.pdfbox.pdmodel.DefaultResourceCache();
            doc.setResourceCache(cache);
            cache.put(indirectFontRef, font);

            boolean sameInstance = dr.getFont(fontName) == font;
            System.out.println("  dr.getFont(name) == original instance? " + sameInstance);

            PDTextField field = (PDTextField) acroForm.getField(TARGET_FIELD);
            field.setDefaultAppearance("/" + fontName.getName() + " 10 Tf 0 g");

            try {
                field.setValue("中文测");
                System.out.println("  setValue(\"中文测\") SUCCEEDED via Workaround A.");
                byte[] out = toBytes(doc);
                System.out.println("  Output size: " + out.length / 1024.0 + " KB");
                assertFieldValueRoundTrips(out, "中文测");
            } catch (Exception e) {
                System.out.println("  setValue(\"中文测\") FAILED even with Workaround A: " + e);
            }
        }
    }

    @Test
    void indirectResourceCacheWorkaround_inspectActualAppearanceStreamContent() throws IOException {
        assumeTrue(Files.exists(NOTO_SANS_SC), "Skipped: " + NOTO_SANS_SC + " not present.");

        try (PDDocument doc = openTemplate()) {
            PDAcroForm acroForm = doc.getDocumentCatalog().getAcroForm();
            PDResources dr = acroForm.getDefaultResources();
            if (dr == null) {
                dr = new PDResources();
                acroForm.setDefaultResources(dr);
            }

            PDFont font = PDType0Font.load(doc, new FileInputStream(NOTO_SANS_SC.toFile()), true);
            COSName fontName = COSName.getPDFName("NotoSansSCIndirect");

            org.apache.pdfbox.cos.COSDictionary fontsDict = (org.apache.pdfbox.cos.COSDictionary) dr.getCOSObject()
                    .getDictionaryObject(COSName.FONT);
            if (fontsDict == null) {
                fontsDict = new org.apache.pdfbox.cos.COSDictionary();
                dr.getCOSObject().setItem(COSName.FONT, fontsDict);
            }
            org.apache.pdfbox.cos.COSObject indirectFontRef = new org.apache.pdfbox.cos.COSObject(font.getCOSObject());
            fontsDict.setItem(fontName, indirectFontRef);

            org.apache.pdfbox.pdmodel.ResourceCache cache = new org.apache.pdfbox.pdmodel.DefaultResourceCache();
            doc.setResourceCache(cache);
            cache.put(indirectFontRef, font);

            PDTextField field = (PDTextField) acroForm.getField(TARGET_FIELD);
            field.setDefaultAppearance("/" + fontName.getName() + " 10 Tf 0 g");
            field.setValue("中文测");
            doc.save("target/FontEmbeddingDiagnosticTest-WorkaroundA.pdf");

            byte[] out = toBytes(doc);

            try (PDDocument reloaded = Loader.loadPDF(out)) {
                PDAcroForm reloadedForm = reloaded.getDocumentCatalog().getAcroForm();
                PDTextField reloadedField = (PDTextField) reloadedForm.getField(TARGET_FIELD);

                var widget = reloadedField.getWidgets().get(0);
                var appearanceStream = widget.getAppearance().getNormalAppearance().getAppearanceStream();

                System.out.println("=== Workaround A: actual appearance stream content ===");
                try (var is = appearanceStream.getContentStream().createInputStream()) {
                    String content = new String(is.readAllBytes(), java.nio.charset.StandardCharsets.ISO_8859_1);
                    System.out.println(content);
                }

                System.out.println("=== Fonts actually embedded in reloaded /DR ===");
                PDResources reloadedDr = reloadedForm.getDefaultResources();
                for (COSName name : reloadedDr.getFontNames()) {
                    PDFont f = reloadedDr.getFont(name);
                    PDFontDescriptor fd = f.getFontDescriptor();
                    boolean embedded = fd != null
                            && (fd.getFontFile() != null || fd.getFontFile2() != null || fd.getFontFile3() != null);
                    System.out.println("  /" + name.getName() + " -> BaseFont=" + f.getName()
                            + " Embedded=" + embedded);
                }

                // Does the reloaded font (whatever it is) actually cover CJK?
                PDFont usedFont = reloadedDr.getFont(fontName);
                try {
                    usedFont.encode("中");
                    System.out.println("  Font referenced by /" + fontName.getName()
                            + " CAN encode \"中\" after reload.");
                } catch (Exception e) {
                    System.out.println("  Font referenced by /" + fontName.getName()
                            + " CANNOT encode \"中\" after reload: " + e);
                }
            }
        }
    }

    @Test
    void manualAppearanceStream_confirmsRealEmbeddingAfterReload() throws IOException {
        assumeTrue(Files.exists(NOTO_SANS_SC), "Skipped: " + NOTO_SANS_SC + " not present.");

        try (PDDocument doc = openTemplate()) {
            PDAcroForm acroForm = doc.getDocumentCatalog().getAcroForm();
            PDResources dr = acroForm.getDefaultResources();
            PDFont font = PDType0Font.load(doc, new FileInputStream(NOTO_SANS_SC.toFile()), true);
            dr.put(COSName.getPDFName("NotoSansSCManualDiag"), font);

            PDTextField field = (PDTextField) acroForm.getField(TARGET_FIELD);
            field.setDefaultAppearance("/NotoSansSCManualDiag 10 Tf 0 g");
            field.getCOSObject().setString(COSName.V, "中文测");

            var widget = field.getWidgets().get(0);
            var rect = widget.getRectangle();

            var appearance = new org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceStream(doc);
            PDResources apResources = new PDResources();
            apResources.put(COSName.getPDFName("F1"), font);
            appearance.setResources(apResources);
            appearance.setBBox(new org.apache.pdfbox.pdmodel.common.PDRectangle(rect.getWidth(), rect.getHeight()));

            try (var cs = new org.apache.pdfbox.pdmodel.PDPageContentStream(doc, appearance)) {
                cs.beginText();
                cs.setFont(font, 10);
                cs.newLineAtOffset(2, 5);
                cs.showText("中文测");
                cs.endText();
            }

            var appearanceDict = widget.getAppearance();
            if (appearanceDict == null) {
                appearanceDict = new org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceDictionary();
                widget.setAppearance(appearanceDict);
            }
            appearanceDict.setNormalAppearance(appearance);

            byte[] out = toBytes(doc);

            try (PDDocument reloaded = Loader.loadPDF(out)) {
                PDAcroForm reloadedForm = reloaded.getDocumentCatalog().getAcroForm();
                PDResources reloadedDr = reloadedForm.getDefaultResources();
                PDFont reloadedFont = reloadedDr.getFont(COSName.getPDFName("NotoSansSCManualDiag"));

                var fd = reloadedFont.getFontDescriptor();
                boolean embedded = fd != null
                        && (fd.getFontFile() != null || fd.getFontFile2() != null || fd.getFontFile3() != null);
                System.out.println("=== Manual appearance stream: embedding check after reload ===");
                System.out.println("  Embedded=" + embedded);

                try {
                    reloadedFont.encode("中");
                    System.out.println("  reloadedFont.encode(\"中\") SUCCEEDED -- real glyph data present.");
                } catch (Exception e) {
                    System.out.println("  reloadedFont.encode(\"中\") FAILED: " + e);
                }

                // Also inspect the actual reloaded appearance stream content,
                // same technique as before.
                var reloadedField = (PDTextField) reloadedForm.getField(TARGET_FIELD);
                var reloadedWidget = reloadedField.getWidgets().get(0);
                var reloadedAppearance = reloadedWidget.getAppearance().getNormalAppearance().getAppearanceStream();
                try (var is = reloadedAppearance.getContentStream().createInputStream()) {
                    System.out.println("  Appearance stream content:");
                    System.out.println(new String(is.readAllBytes(), java.nio.charset.StandardCharsets.ISO_8859_1));
                }
            }
        }
    }

    @Test
    void isolate_pdAppearanceContentStream_withoutFormatter_manualShowText() throws IOException {
        assumeTrue(Files.exists(NOTO_SANS_SC), "Skipped: NotoSansSC-Regular.ttf not present.");

        try (PDDocument doc = openTemplate()) {
            PDAcroForm acroForm = doc.getDocumentCatalog().getAcroForm();
            PDResources dr = acroForm.getDefaultResources();
            PDFont font = PDType0Font.load(doc, new FileInputStream(NOTO_SANS_SC.toFile()), true);
            dr.put(COSName.getPDFName("NotoSansSCIsolateDiag"), font);

            PDTextField field = (PDTextField) acroForm.getField(TARGET_FIELD);
            field.setDefaultAppearance("/NotoSansSCIsolateDiag 10 Tf 0 g");
            field.getCOSObject().setString(COSName.V, "中文测");

            PDAnnotationWidget widget = field.getWidgets().get(0);
            PDRectangle rect = widget.getRectangle();

            PDAppearanceStream appearance = new PDAppearanceStream(doc);
            PDResources apResources = new PDResources();
            apResources.put(COSName.getPDFName("F1"), font);
            appearance.setResources(apResources);
            appearance.setBBox(new PDRectangle(rect.getWidth(), rect.getHeight()));

            // Deliberately: PDAppearanceContentStream (no doc), but PLAIN
            // showText() -- no PlainTextFormatter/trampoline involved at all.
            // If this ALSO shows Embedded=false, the content-stream class
            // itself is the cause, independent of the formatter.
            try (PDAppearanceContentStream cs = new PDAppearanceContentStream(appearance)) {
                cs.beginText();
                cs.setFont(font, 10);
                cs.newLineAtOffset(2, 5);
                cs.showText("中文测");
                cs.endText();
            }

            PDAppearanceDictionary appearanceDict = widget.getAppearance();
            if (appearanceDict == null) {
                appearanceDict = new PDAppearanceDictionary();
                widget.setAppearance(appearanceDict);
            }
            appearanceDict.setNormalAppearance(appearance);

            // PDAppearanceContentStream never registers the font with the PDDocument
            // (it passes `null` as the document to its superclass internally), so the
            // normal "subset automatically at save() time" mechanism never fires for
            // fonts used only through this stream type. Do it ourselves.
            if (font instanceof PDType0Font type0Font) {
                type0Font.subset();
            }

            byte[] out = toBytes(doc);

            try (PDDocument reloaded = Loader.loadPDF(out)) {
                PDResources reloadedDr = reloaded.getDocumentCatalog().getAcroForm().getDefaultResources();
                PDFont reloadedFont = reloadedDr.getFont(COSName.getPDFName("NotoSansSCIsolateDiag"));
                var fd = reloadedFont.getFontDescriptor();
                boolean embedded = fd != null
                        && (fd.getFontFile() != null || fd.getFontFile2() != null || fd.getFontFile3() != null);
                System.out.println("=== Isolation: PDAppearanceContentStream + manual showText (NO formatter) ===");
                System.out.println("  Embedded=" + embedded);
                try {
                    reloadedFont.encode("中");
                    System.out.println("  encode(\"中\") SUCCEEDED");
                } catch (Exception e) {
                    System.out.println("  encode(\"中\") FAILED: " + e);
                }
            }
        }
    }

    // ------------------------------------------------------------- Plumbing

    private PDDocument openTemplate() throws IOException {
        OrderPdfProperties properties = OrderPdfProperties.defaults();
        return new PdfTemplateLoader(properties).loadOrderTemplate();
    }

    private byte[] renderBaseline() throws IOException {
        try (PDDocument doc = openTemplate()) {
            return toBytes(doc);
        }
    }

    private byte[] renderWithEmbeddedFont(Path fontFile, String resourceName, String probeValue) throws IOException {
        try (PDDocument doc = openTemplate()) {
            PDFont font = PDType0Font.load(doc, new FileInputStream(fontFile.toFile()), true);
            writeProbeIntoField(doc, font, resourceName, probeValue);
            return toBytes(doc);
        }
    }

    private void writeProbeIntoField(PDDocument doc, PDFont font, String resourceName, String probeValue)
            throws IOException {
        PDAcroForm acroForm = doc.getDocumentCatalog().getAcroForm();
        PDResources dr = acroForm.getDefaultResources();
        dr.put(COSName.getPDFName(resourceName), font);

        PDTextField field = (PDTextField) acroForm.getField(TARGET_FIELD);
        field.setDefaultAppearance("/" + resourceName + " 10 Tf 0 g");
        field.setValue(probeValue);
    }

    private byte[] toBytes(PDDocument doc) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        doc.save(out);
        return out.toByteArray();
    }

    private void assertFieldValueRoundTrips(byte[] pdfBytes, String expected) throws IOException {
        try (PDDocument reloaded = Loader.loadPDF(pdfBytes)) {
            PDAcroForm form = reloaded.getDocumentCatalog().getAcroForm();
            String actual = form.getField(TARGET_FIELD).getValueAsString();
            if (!expected.equals(actual)) {
                throw new AssertionError("Field value did not round-trip. Expected: " + expected
                        + " / Actual: " + actual);
            }
        }
    }
}
