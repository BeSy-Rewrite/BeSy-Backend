package de.hs_esslingen.besy.pdf;

import java.util.List;
import java.util.Optional;

import de.hs_esslingen.besy.models.Address;
import de.hs_esslingen.besy.models.Approval;
import de.hs_esslingen.besy.models.Item;
import de.hs_esslingen.besy.models.Order;
import de.hs_esslingen.besy.models.Person;
import de.hs_esslingen.besy.models.Quotation;
import de.hs_esslingen.besy.models.Supplier;

/**
 * Everything the PDF generation needs from the persistence layer, loaded once
 * by {@link OrderPdfDataLoader}. Pure data holder — no repository access, no
 * formatting, no calculation.
 */
public record OrderPdfData(
        Order order,
        Optional<Supplier> supplier,
        Approval approval,
        List<Item> items,
        Optional<Person> deliveryPerson,
        Optional<Person> invoicePerson,
        Optional<Person> queriesPerson,
        List<Quotation> quotations) {

    /** Convenience accessor — the delivery address is reached via the order. */
    public Address deliveryAddress() {
        return order.getDeliveryAddress();
    }

    /** Convenience accessor — the invoice address is reached via the order. */
    public Address invoiceAddress() {
        return order.getInvoiceAddress();
    }
}
