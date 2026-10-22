package de.hs_esslingen.besy.dtos.request;

import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Value;

/**
 * DTO for {@link de.hs_esslingen.besy.models.CustomerId}
 */
@Value
public class CustomerIdRequestDTO implements Serializable {
    @NotNull
    @Schema(description = "Kundenkennung (extern oder intern)")
    String customerId;
    @Schema(description = "Optionaler Kommentar zur Kundenkennung")
    String comment;
}
