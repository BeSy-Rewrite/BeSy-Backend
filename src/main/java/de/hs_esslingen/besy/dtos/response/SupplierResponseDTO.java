package de.hs_esslingen.besy.dtos.response;

import de.hs_esslingen.besy.dtos.request.AddressRequestDTO;
import lombok.Value;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for {@link de.hs_esslingen.besy.models.Supplier}
 */
@Value
public class SupplierResponseDTO implements Serializable {
    Integer id;
    String name;
    String phone;
    String fax;
    String email;
    String comment;
    String website;
    String vatId;
    Boolean flagPreferred;
    String buildingName;
    String street;
    String buildingNumber;
    String town;
    String postalCode;
    String county;
    String country;
    LocalDate deactivatedDate;
    AddressRequestDTO address;
}