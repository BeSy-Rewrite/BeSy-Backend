package de.hs_esslingen.besy.exceptions;

import java.time.Instant;

import io.micrometer.common.lang.NonNull;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(name = "ErrorResponse", description = "Standardisierte Fehlerantwort")
public record ErrorResponseDTO(
        @NonNull @Schema(format = "date-time") Instant timestamp,
        @NotNull int status,
        @NotNull String error,
        @NotNull String message,
        @NotNull String path) {
}
