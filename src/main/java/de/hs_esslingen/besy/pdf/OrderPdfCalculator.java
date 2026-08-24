package de.hs_esslingen.besy.pdf;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import de.hs_esslingen.besy.enums.VatType;
import de.hs_esslingen.besy.models.Item;
import de.hs_esslingen.besy.models.Vat;
import de.hs_esslingen.besy.services.PriceConversionService;

/**
 * Performs order-PDF calculations only. Formatting remains in the caller until
 * PdfValueFormatter is introduced.
 */
public final class OrderPdfCalculator {

    private OrderPdfCalculator() {
    }

    public static OrderPdfTotals calculate(
            List<Item> items,
            BigDecimal percentageDiscount,
            String defaultVatValue) {

        BigDecimal subTotal = items
                .stream()
                .map(item -> {
                    BigDecimal netPrice = item.getVatType() == VatType.netto
                            ? item.getPricePerUnit()
                            : PriceConversionService.convertGrossPriceToNetPrice(
                                    item.getPricePerUnit(),
                                    item.getVat());

                    return netPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal netTotal = subTotal.multiply(
                (BigDecimal.valueOf(100).subtract(
                        percentageDiscount != null ? percentageDiscount : BigDecimal.ZERO))
                        .divide(BigDecimal.valueOf(100)))
                .setScale(2, RoundingMode.HALF_UP);

        // Intentionally retains Vat identity-based Set semantics.
        Set<Vat> vats = items.stream()
                .map(Item::getVat)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (vats.size() <= 1) {
            BigDecimal vatValue = vats.stream()
                    .map(Vat::getValue)
                    .findFirst()
                    .orElse(BigDecimal.valueOf(Double.parseDouble(defaultVatValue)));

            BigDecimal total = netTotal.multiply(
                    (BigDecimal.valueOf(100).add(vatValue)).divide(BigDecimal.valueOf(100)))
                    .setScale(2, RoundingMode.HALF_UP);

            return new OrderPdfTotals(
                    subTotal,
                    netTotal,
                    Optional.of(total),
                    Optional.of(vatValue),
                    vats);
        }

        return new OrderPdfTotals(
                subTotal,
                netTotal,
                Optional.empty(),
                Optional.empty(),
                vats);
    }
}
