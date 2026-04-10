package de.hs_esslingen.besy.extern.bic.dtos;

public enum BicReqDataAuthFlag {
    SEND_WITH_OAUTH("OAuth"),
    SEND_WITH_APIKEY("APIkey");

    public final String label;

    private BicReqDataAuthFlag(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return this.label;
    }
}
