package de.hs_esslingen.besy.repositories;

import de.hs_esslingen.besy.enums.AddressOwnerType;
import de.hs_esslingen.besy.models.Address;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@ActiveProfiles("test")
class AddressRepositoryTest {

    @Autowired
    private AddressRepository addressRepository;

    @Test
    void should_return_only_supplier_addresses_when_owner_type_is_supplier() {
        Address supplierAddress1 = createAddress("TownA", AddressOwnerType.Supplier);
        Address supplierAddress2 = createAddress("TownB", AddressOwnerType.Supplier);
        Address personAddress = createAddress("TownC", AddressOwnerType.Person);

        addressRepository.save(supplierAddress1);
        addressRepository.save(supplierAddress2);
        addressRepository.save(personAddress);

        List<Address> result = addressRepository.getAddressByOwnerType(AddressOwnerType.Supplier);

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(a -> a.getOwnerType() == AddressOwnerType.Supplier));
    }

    @Test
    void should_return_only_person_addresses_when_owner_type_is_person() {
        Address supplierAddress = createAddress("TownA", AddressOwnerType.Supplier);
        Address personAddress1 = createAddress("TownB", AddressOwnerType.Person);
        Address personAddress2 = createAddress("TownC", AddressOwnerType.Person);

        addressRepository.save(supplierAddress);
        addressRepository.save(personAddress1);
        addressRepository.save(personAddress2);

        List<Address> result = addressRepository.getAddressByOwnerType(AddressOwnerType.Person);

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(a -> a.getOwnerType() == AddressOwnerType.Person));
    }

    @Test
    void should_return_empty_list_when_no_addresses_of_given_owner_type_exist() {
        Address supplierAddress = createAddress("TownA", AddressOwnerType.Supplier);
        addressRepository.save(supplierAddress);

        List<Address> result = addressRepository.getAddressByOwnerType(AddressOwnerType.Person);

        assertTrue(result.isEmpty());
    }

    private Address createAddress(String town, AddressOwnerType ownerType) {
        Address address = new Address();
        address.setTown(town);
        address.setOwnerType(ownerType);
        return address;
    }
}
