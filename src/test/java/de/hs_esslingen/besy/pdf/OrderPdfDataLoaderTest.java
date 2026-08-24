package de.hs_esslingen.besy.pdf;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import de.hs_esslingen.besy.exceptions.NotFoundException;
import de.hs_esslingen.besy.models.Order;
import de.hs_esslingen.besy.models.Person;
import de.hs_esslingen.besy.repositories.ItemRepository;
import de.hs_esslingen.besy.repositories.OrderRepository;
import de.hs_esslingen.besy.repositories.PersonRepository;
import de.hs_esslingen.besy.repositories.QuotationRepository;
import de.hs_esslingen.besy.repositories.SupplierRepository;

@ExtendWith(MockitoExtension.class)
class OrderPdfDataLoaderTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private SupplierRepository supplierRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private PersonRepository personRepository;
    @Mock
    private QuotationRepository quotationRepository;

    @InjectMocks
    private OrderPdfDataLoader loader;

    @Test
    void throws_not_found_and_touches_nothing_else_when_order_missing() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> loader.load(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Order with id 999 does not exist.");

        verifyNoInteractions(supplierRepository, itemRepository, personRepository, quotationRepository);
    }

    @Test
    void treats_null_id_as_not_found_without_repository_access() {
        assertThatThrownBy(() -> loader.load(null))
                .isInstanceOf(NotFoundException.class);

        verifyNoInteractions(orderRepository, supplierRepository, itemRepository,
                personRepository, quotationRepository);
    }

    @Test
    void prefers_already_loaded_queries_person_over_repository_lookup() {
        Order order = new Order();
        order.setId(1L);
        Person attached = new Person();
        attached.setId(42L);
        order.setQueriesPerson(attached);
        order.setQueriesPersonId(42L);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(itemRepository.findByOrder_Id(1L)).thenReturn(List.of());
        when(quotationRepository.getQuotationByOrderId(1L)).thenReturn(List.of());

        OrderPdfData data = loader.load(1L);

        assertThat(data.queriesPerson()).containsSame(attached);
        assertThat(data.supplier()).isEmpty();
        verifyNoInteractions(personRepository, supplierRepository);
    }
}
