package de.hs_esslingen.besy.services;

import java.math.BigDecimal;
import java.math.RoundingMode;

import de.hs_esslingen.besy.models.Vat;

public class PriceConversionService {
    private PriceConversionService() {
    }

    public static BigDecimal convertNetPriceToGrossPrice(BigDecimal netPrice, Vat vatValue) {
        if (netPrice == null || vatValue == null) {
            throw new IllegalArgumentException("Net price and tax rate must not be null");
        }
        return netPrice.multiply(BigDecimal.ONE.add(vatToDecimal(vatValue))).setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal convertGrossPriceToNetPrice(BigDecimal grossPrice, Vat vatValue) {
        if (grossPrice == null || vatValue == null) {
            throw new IllegalArgumentException("Gross price and tax rate must not be null");
        }
        return grossPrice.divide(BigDecimal.ONE.add(vatToDecimal(vatValue)), 2, RoundingMode.HALF_UP);
    }

    private static BigDecimal vatToDecimal(Vat vat) {
        return vat.getValue().divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }
}
