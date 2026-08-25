package de.hs_esslingen.besy.pdf;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * {@link OrderPDFService} is now just a facade for
 * {@link OrderPdfGenerator} (caller: {@code OrderController}). The test focuses
 * exclusively on delegation—the business logic is contained in
 * {@link OrderPdfGeneratorTest} and {@link OrderPdfGoldenTest}.
 */
@ExtendWith(MockitoExtension.class)
class OrderPDFServiceTest {

    @Mock
    private OrderPdfGenerator generator;

    @InjectMocks
    private OrderPDFService service;

    @Test
    @DisplayName("delegates to the generator and returns its bytes unchanged")
    void delegates_to_the_generator() throws IOException {
        byte[] expected = { 1, 2, 3 };
        when(generator.generate(42L)).thenReturn(expected);

        assertThat(service.generateOrderPDF(42L)).isSameAs(expected);
        verify(generator).generate(42L);
    }

    @Test
    @DisplayName("propagates IOException unchanged")
    void propagates_io_exception() throws IOException {
        when(generator.generate(42L)).thenThrow(new IOException("boom"));

        assertThatThrownBy(() -> service.generateOrderPDF(42L))
                .isInstanceOf(IOException.class)
                .hasMessage("boom");
    }
}
