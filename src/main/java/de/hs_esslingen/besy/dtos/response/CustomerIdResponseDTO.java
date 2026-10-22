package de.hs_esslingen.besy.dtos.response;

import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Value;

/**
 * DTO for {@link de.hs_esslingen.besy.models.CustomerId}
 */
@Value
public class CustomerIdResponseDTO implements Serializable {
    @NotNull
    @Schema(description = "Kundenkennung (extern oder intern)")
    String customerId;
    @Schema(description = "Optionaler Kommentar zur Kundenkennung")
    String comment;
}
