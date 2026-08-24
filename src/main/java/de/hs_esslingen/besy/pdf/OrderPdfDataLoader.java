package de.hs_esslingen.besy.pdf;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import de.hs_esslingen.besy.exceptions.NotFoundException;
import de.hs_esslingen.besy.models.Item;
import de.hs_esslingen.besy.models.Order;
import de.hs_esslingen.besy.models.Person;
import de.hs_esslingen.besy.models.Quotation;
import de.hs_esslingen.besy.models.Supplier;
import de.hs_esslingen.besy.repositories.ItemRepository;
import de.hs_esslingen.besy.repositories.OrderRepository;
import de.hs_esslingen.besy.repositories.PersonRepository;
import de.hs_esslingen.besy.repositories.QuotationRepository;
import de.hs_esslingen.besy.repositories.SupplierRepository;
import lombok.RequiredArgsConstructor;

/**
 * Single point of repository access for PDF generation.
 *
 * <p>
 * Extracted from {@code OrderPDFService} without behaviour change: same
 * repository calls, same order, same {@link NotFoundException} message.
 */
@Component
@RequiredArgsConstructor
public class OrderPdfDataLoader {

    private final OrderRepository orderRepository;
    private final SupplierRepository supplierRepository;
    private final ItemRepository itemRepository;
    private final PersonRepository personRepository;
    private final QuotationRepository quotationRepository;

    /**
     * Loads the order aggregate required for the PDF.
     *
     * @param orderId id of the order; {@code null} is treated as "not found"
     * @throws NotFoundException if no order with the given id exists
     */
    public OrderPdfData load(Long orderId) {
        Order order = Optional.ofNullable(orderId)
                .flatMap(orderRepository::findById)
                .orElseThrow(() -> new NotFoundException("Order with id " + orderId + " does not exist."));

        Optional<Supplier> supplier = Optional.ofNullable(order.getSupplierId())
                .flatMap(supplierRepository::findById);

        List<Item> items = itemRepository.findByOrder_Id(order.getId());

        Optional<Person> deliveryPerson = Optional.ofNullable(order.getDeliveryPersonId())
                .flatMap(personRepository::findById);
        Optional<Person> invoicePerson = Optional.ofNullable(order.getInvoicePersonId())
                .flatMap(personRepository::findById);
        Optional<Person> queriesPerson = Optional.ofNullable(order.getQueriesPerson())
                .or(() -> Optional.ofNullable(order.getQueriesPersonId())
                        .flatMap(personRepository::findById));

        List<Quotation> quotations = quotationRepository.getQuotationByOrderId(orderId);

        return new OrderPdfData(
                order,
                supplier,
                order.getApproval(),
                items,
                deliveryPerson,
                invoicePerson,
                queriesPerson,
                quotations);
    }
}
