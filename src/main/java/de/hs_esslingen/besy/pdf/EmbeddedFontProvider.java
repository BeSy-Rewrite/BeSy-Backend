package de.hs_esslingen.besy.pdf;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

/**
 * Provides Unicode-capable fonts embedded into a document at generation
 * time, in three tiers:
 *
 * <ul>
 * <li><strong>Always loaded</strong>: Noto Sans regular + bold -- Latin
 * Extended, Greek, Cyrillic. Small (~614 KB each), loaded eagerly at
 * startup.</li>
 * <li><strong>Loaded on first use</strong>: Noto Sans SC -- CJK Unified
 * Ideographs, plus full Latin coverage (so it can carry an entire mixed
 * German+Chinese field on its own). Large (~10 MB); parsed only once,
 * the first time any request actually needs it, then cached.</li>
 * <li><strong>Loaded on first use</strong>: Noto Emoji (monochrome
 * outline) -- emoji only, no Latin coverage at all.</li>
 * </ul>
 *
 * <p>
 * Each {@code embedXxx} method returns a fresh, request-scoped
 * {@link PDFont} wrapper for the given {@link PDDocument} (subset-enabled),
 * built from a cached, already-parsed raw byte array. Concurrent reuse of
 * the same raw bytes across simultaneous requests is safe (see
 * {@code FontEmbeddingDiagnostic#concurrentReuseOfParsedTrueTypeFontProgram_acrossThreads});
 * each call still re-parses into a document-local {@code PDFont}, since a
 * {@code PDFont}/{@code PDType0Font} instance itself is tied to one
 * document's subsetting state and must not be shared across documents.
 */
@Component
public class EmbeddedFontProvider {

    private byte[] regularFontBytes;
    private byte[] boldFontBytes;

    // Loaded lazily, on first actual use -- see
    // loadCjkFontBytes()/loadEmojiFontBytes().
    private volatile byte[] cjkFontBytes;
    private volatile byte[] emojiFontBytes;

    @PostConstruct
    void init() throws IOException {
        regularFontBytes = readClasspathResource("/fonts/NotoSans-Regular.ttf");
        boldFontBytes = readClasspathResource("/fonts/NotoSans-Bold.ttf");
    }

    private byte[] readClasspathResource(String path) throws IOException {
        try (InputStream is = getClass().getResourceAsStream(path)) {
            if (is == null) {
                throw new IOException("Required embedded font resource not found on classpath: " + path);
            }
            return is.readAllBytes();
        }
    }

    /**
     * Always-available regular-weight font covering Latin Extended, Greek,
     * Cyrillic.
     */
    public PDFont embedRegular(PDDocument document) throws IOException {
        return loadFont(document, regularFontBytes);
    }

    /**
     * Always-available bold-weight font, same coverage as {@link #embedRegular}.
     */
    public PDFont embedBold(PDDocument document) throws IOException {
        return loadFont(document, boldFontBytes);
    }

    /**
     * CJK-capable font (Noto Sans SC), also covering full Latin -- safe to
     * use for an entire field's text even if that text mixes German and
     * Chinese/Japanese/Korean characters. Parses the ~10 MB source font on
     * first call only; cached afterward.
     */
    public PDFont embedCjk(PDDocument document) throws IOException {
        return loadFont(document, cjkFontBytesCached());
    }

    /**
     * Monochrome emoji-only font (Noto Emoji). Has NO Latin/Basic coverage
     * at all -- only usable for text that is entirely emoji.
     */
    public PDFont embedEmoji(PDDocument document) throws IOException {
        return loadFont(document, emojiFontBytesCached());
    }

    private byte[] cjkFontBytesCached() throws IOException {
        byte[] local = cjkFontBytes;
        if (local == null) {
            synchronized (this) {
                local = cjkFontBytes;
                if (local == null) {
                    local = readClasspathResource("/fonts/NotoSansSC-Regular.ttf");
                    cjkFontBytes = local;
                }
            }
        }
        return local;
    }

    private byte[] emojiFontBytesCached() throws IOException {
        byte[] local = emojiFontBytes;
        if (local == null) {
            synchronized (this) {
                local = emojiFontBytes;
                if (local == null) {
                    local = readClasspathResource("/fonts/NotoEmoji-Regular.ttf");
                    emojiFontBytes = local;
                }
            }
        }
        return local;
    }

    private PDFont loadFont(PDDocument document, byte[] fontBytes) throws IOException {
        return PDType0Font.load(document, new ByteArrayInputStream(fontBytes), true);
    }
}
