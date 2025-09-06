package de.hs_esslingen.besy.dtos.response;

import lombok.Value;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * DTO for {@link de.hs_esslingen.besy.models.Supplier}
 */
@Value
public class CreateSupplierResponseDTO implements Serializable {
    Integer id;
    String name;
    String phone;
    String fax;
    String email;
    String comment;
    String website;
    String vatId;
    Boolean flagPreferred;
    LocalDate deactivatedDate;
    AddressResponseDTO address;
}