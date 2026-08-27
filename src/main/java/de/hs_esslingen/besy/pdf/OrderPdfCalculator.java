package de.hs_esslingen.besy.pdf;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import de.hs_esslingen.besy.enums.VatType;
import de.hs_esslingen.besy.models.Item;
import de.hs_esslingen.besy.models.Vat;
import de.hs_esslingen.besy.services.PriceConversionService;

/**
 * Performs order-PDF calculations only. Formatting remains in the caller.
 */
public final class OrderPdfCalculator {

        private OrderPdfCalculator() {
        }

        public static OrderPdfTotals calculate(
                        List<Item> items,
                        BigDecimal percentageDiscount,
                        BigDecimal defaultVatValue) {

                BigDecimal discount = percentageDiscount != null ? percentageDiscount : BigDecimal.ZERO;

                BigDecimal subTotal = items
                                .stream()
                                .map(item -> netPriceOf(item).multiply(BigDecimal.valueOf(item.getQuantity())))
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal netTotal = subTotal.multiply(
                                (BigDecimal.valueOf(100).subtract(discount))
                                                .divide(BigDecimal.valueOf(100)))
                                .setScale(2, RoundingMode.HALF_UP);

                // Vat now has value-based equality (see Vat.equals), so this Set correctly
                // collapses separate Vat instances that represent the same rate.
                Set<Vat> vats = items.stream()
                                .map(Item::getVat)
                                .filter(Objects::nonNull)
                                .collect(Collectors.toSet());

                if (vats.size() <= 1) {
                        BigDecimal vatValue = vats.stream()
                                        .map(Vat::getValue)
                                        .findFirst()
                                        .orElse(defaultVatValue);

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

                // Mixed VAT: the single "one rate for everything" formula above does not
                // apply, so the total is computed per VAT rate and summed. The VAT rate field
                // itself stays blank — there is no single rate to show.
                BigDecimal total = totalPerVatRateSummed(items, discount);

                return new OrderPdfTotals(
                                subTotal,
                                netTotal,
                                Optional.of(total),
                                Optional.empty(),
                                vats);
        }

        /**
         * Groups items by their (now value-equal) {@link Vat}, applies the discount and
         * VAT rate within each group, rounds each group's gross total to 2 decimals,
         * and sums the already-rounded group totals.
         */
        private static BigDecimal totalPerVatRateSummed(List<Item> items, BigDecimal discount) {
                Map<Vat, BigDecimal> netSubTotalPerVat = items.stream()
                                .filter(item -> item.getVat() != null)
                                .collect(Collectors.groupingBy(
                                                Item::getVat,
                                                Collectors.reducing(BigDecimal.ZERO,
                                                                item -> netPriceOf(item).multiply(
                                                                                BigDecimal.valueOf(item.getQuantity())),
                                                                BigDecimal::add)));

                return netSubTotalPerVat.entrySet().stream()
                                .map(entry -> {
                                        BigDecimal groupNetTotal = entry.getValue()
                                                        .multiply(BigDecimal.valueOf(100).subtract(discount))
                                                        .divide(BigDecimal.valueOf(100))
                                                        .setScale(2, RoundingMode.HALF_UP);

                                        return groupNetTotal
                                                        .multiply(BigDecimal.valueOf(100)
                                                                        .add(entry.getKey().getValue()))
                                                        .divide(BigDecimal.valueOf(100))
                                                        .setScale(2, RoundingMode.HALF_UP);
                                })
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        private static BigDecimal netPriceOf(Item item) {
                return item.getVatType() == VatType.netto
                                ? item.getPricePerUnit()
                                : PriceConversionService.convertGrossPriceToNetPrice(item.getPricePerUnit(),
                                                item.getVat());
        }
}
