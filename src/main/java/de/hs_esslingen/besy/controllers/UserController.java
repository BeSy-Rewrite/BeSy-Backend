package de.hs_esslingen.besy.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import de.hs_esslingen.besy.dtos.request.UserPreferencesRequestDTO;
import de.hs_esslingen.besy.dtos.response.UserPreferencesResponseDTO;
import de.hs_esslingen.besy.dtos.response.UserResponseDTO;
import de.hs_esslingen.besy.exceptions.NotFoundException;
import de.hs_esslingen.besy.mappers.response.UserResponseMapper;
import de.hs_esslingen.besy.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping(value = "${api.prefix}/users", produces = "application/json")
public class UserController {

        private final UserService userService;
        private final UserResponseMapper userResponseMapper;

        @GetMapping
        @Operation(description = "Gibt alle Benutzer zurück.")
        @ApiResponse(responseCode = "200", description = "Liste der Benutzer.")
        public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
                return userService.getAllUsers();
        }

        @GetMapping("/{id}")
        @Operation(description = "Gibt einen Benutzer anhand seiner ID zurück.")
        @ApiResponse(responseCode = "200", description = "Benutzer erfolgreich abgerufen.")
        @ApiResponse(responseCode = "404", description = "Benutzer nicht gefunden.")
        public ResponseEntity<UserResponseDTO> getUser(
                        @Parameter(description = "Die ID des abzurufenden Benutzers.") @PathVariable("id") long id) {
                return ResponseEntity.ok(userResponseMapper.toDto(userService.getUserById(id)
                                .orElseThrow(() -> new NotFoundException(
                                                "Benutzer mit id " + id + " nicht gefunden."))));
        }

        @GetMapping("/me")
        @Operation(description = "Gibt den aktuell authentifizierten Benutzer zurück.")
        @ApiResponse(responseCode = "200", description = "Erfolgreiche Abfrage des Benutzers")
        @ApiResponse(responseCode = "500", description = "Interner Serverfehler.")
        public ResponseEntity<UserResponseDTO> getCurrentUser(@AuthenticationPrincipal Jwt jwt) {
                return userService.getUserByKeycloakUUID(jwt);
        }

        @GetMapping("/me/preferences")
        @Operation(description = "Gibt die Präferenzen des aktuell authentifizierten Benutzers zurück.")
        @ApiResponse(responseCode = "200", description = "Erfolgreiche Abfrage der Benutzerpräferenzen")
        @ApiResponse(responseCode = "404", description = "Benutzer nicht gefunden")
        @ApiResponse(responseCode = "500", description = "Interner Serverfehler")
        public ResponseEntity<List<UserPreferencesResponseDTO>> getUserPreferences(
                        @AuthenticationPrincipal Jwt jwt,
                        @Parameter(description = "Filtert Benutzerpräferenzen nach Typ") @RequestParam(value = "type", required = false) String type) {
                return userService.getUserPreferencesByPreferenceType(jwt, type);
        }

        @PostMapping("/me/preferences")
        @Operation(description = "Fügt eine Präferenz für den aktuell authentifizierten Benutzer hinzu.")
        @ApiResponse(responseCode = "200", description = "Benutzerpräferenzen erfolgreich hinzugefügt")
        @ApiResponse(responseCode = "400", description = "Ungültige Anfrage")
        @ApiResponse(responseCode = "401", description = "Nicht authentifiziert")
        @ApiResponse(responseCode = "500", description = "Interner Serverfehler")
        public ResponseEntity<UserPreferencesResponseDTO> addUserPreferences(
                        @AuthenticationPrincipal Jwt jwt,
                        @RequestBody UserPreferencesRequestDTO requestDTO) {
                return userService.addUserPreference(jwt, requestDTO);
        }

        @PutMapping("/me/preferences/{id}")
        @Operation(description = "Aktualisiert eine Präferenz des aktuell authentifizierten Benutzers.")
        @ApiResponse(responseCode = "200", description = "Benutzerpräferenzen erfolgreich aktualisiert")
        @ApiResponse(responseCode = "400", description = "Ungültige Anfrage")
        @ApiResponse(responseCode = "401", description = "Nicht authentifiziert")
        @ApiResponse(responseCode = "404", description = "Benutzerpräferenz nicht gefunden")
        @ApiResponse(responseCode = "500", description = "Interner Serverfehler")
        public ResponseEntity<UserPreferencesResponseDTO> updateUserPreferences(
                        @AuthenticationPrincipal Jwt jwt,
                        @Parameter(description = "ID der Benutzerpräferenz.") @PathVariable("id") Integer id,
                        @RequestBody UserPreferencesRequestDTO requestDTO) {
                return userService.updateUserPreferences(jwt, requestDTO, id);
        }

        @DeleteMapping("/me/preferences/{id}")
        @Operation(description = "Löscht eine Präferenz des aktuell authentifizierten Benutzers.")
        @ApiResponse(responseCode = "204", description = "Benutzerpräferenz erfolgreich gelöscht")
        @ApiResponse(responseCode = "400", description = "Ungültige Anfrage")
        @ApiResponse(responseCode = "401", description = "Nicht authentifiziert")
        @ApiResponse(responseCode = "404", description = "Benutzerpräferenz nicht gefunden")
        @ApiResponse(responseCode = "500", description = "Interner Serverfehler")
        public ResponseEntity<Void> deleteUserPreferences(
                        @AuthenticationPrincipal Jwt jwt,
                        @Parameter(description = "ID der Benutzerpräferenz.") @PathVariable("id") Integer id) {
                return userService.deleteUserPreferences(jwt, id);
        }

}
