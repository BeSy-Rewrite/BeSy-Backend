package de.hs_esslingen.besy.dtos.request;

import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link de.hs_esslingen.besy.models.Supplier}
 */
@Value
public class SupplierRequestDTO implements Serializable {
    String name;
    String phone;
    String fax;
    String email;
    String comment;
    String website;
    String vatId;
    Boolean flagPreferred;
    String customerNumber;
    AddressRequestDTO address;
}