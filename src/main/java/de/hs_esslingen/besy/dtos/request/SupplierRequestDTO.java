package de.hs_esslingen.besy.dtos.request;

import java.io.Serializable;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Value;

/**
 * DTO for {@link de.hs_esslingen.besy.models.Supplier}
 */
@Value
public class SupplierRequestDTO implements Serializable {
    @NotNull
    String name;
    @Size(max = 45)
    String phone;
    @Size(max = 45)
    String fax;
    @Size(max = 45)
    String email;
    String comment;
    String website;
    @Size(max = 20)
    String vatId;

    @NotNull
    Boolean flagPreferred;
    @NotNull
    AddressRequestDTO address;
}
