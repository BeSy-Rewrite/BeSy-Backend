package de.hs_esslingen.besy.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.hs_esslingen.besy.dtos.request.CustomerIdRequestDTO;
import de.hs_esslingen.besy.dtos.request.SupplierRequestDTO;
import de.hs_esslingen.besy.dtos.response.AddressResponseDTO;
import de.hs_esslingen.besy.dtos.response.CustomerIdResponseDTO;
import de.hs_esslingen.besy.dtos.response.SupplierResponseDTO;
import de.hs_esslingen.besy.dtos.response.SupplierWithAddressResponseDTO;
import de.hs_esslingen.besy.exceptions.NotFoundException;
import de.hs_esslingen.besy.services.AddressService;
import de.hs_esslingen.besy.services.CustomerIdService;
import de.hs_esslingen.besy.services.SupplierService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping(value = "${api.prefix}/suppliers", produces = "application/json")
public class SupplierController {

    private final SupplierService supplierService;
    private final CustomerIdService customerIdService;
    private final AddressService addressService;

    @GetMapping
    @Operation(description = "Gibt eine Liste von Lieferanten zurück.")
    @ApiResponse(responseCode = "200", description = "Liste von Lieferanten")
    public ResponseEntity<List<SupplierResponseDTO>> getAllSuppliers() {
        return supplierService.getAllSuppliers();
    }

    @GetMapping("/{supplier-id}")
    @Operation(description = "Gibt einen Lieferanten anhand seiner ID zurück.")
    @ApiResponse(responseCode = "200", description = "Lieferant gefunden.")
    @ApiResponse(responseCode = "404", description = "Lieferant nicht gefunden.")
    public ResponseEntity<SupplierResponseDTO> getSupplierById(
            @Parameter(description = "Die eindeutige ID des Lieferanten, welcher abgerufen werden soll.") @PathVariable("supplier-id") Integer id) {
        return supplierService.getSupplierById(id);
    }

    @PostMapping
    @Operation(description = "Erstellt einen neuen Lieferanten.")
    @ApiResponse(responseCode = "201", description = "Lieferant erfolgreich erstellt.")
    @ApiResponse(responseCode = "400", description = "Ungültige Eingabe(n).")
    @ApiResponse(responseCode = "500", description = "Interner Serverfehler.")
    public ResponseEntity<SupplierWithAddressResponseDTO> createSupplier(
            @RequestBody SupplierRequestDTO supplierRequestDTO) {
        return supplierService.createSupplier(supplierRequestDTO);
    }

    @PutMapping("/{supplier-id}")
    @Operation(description = "Aktualisiert einen bestehenden Lieferanten anhand seiner ID.")
    @ApiResponse(responseCode = "200", description = "Lieferant gefunden.")
    @ApiResponse(responseCode = "404", description = "Lieferant nicht gefunden.")
    @ApiResponse(responseCode = "500", description = "Interner Serverfehler.")
    public ResponseEntity<SupplierWithAddressResponseDTO> updateSupplier(
            @Parameter(description = "Die eindeutige ID des Lieferanten, welcher aktualisiert werden soll.") @PathVariable("supplier-id") Integer id,
            @RequestBody SupplierRequestDTO supplierRequestDTO) {
        if (!supplierService.existsSupplierById(id))
            throw new NotFoundException("Lieferant nicht gefunden.");
        return supplierService.updateSupplier(id, supplierRequestDTO);
    }

    @GetMapping("/{supplier-id}/customer_ids")
    @Operation(description = "Gibt die Kundennummern eines Lieferanten zurück.")
    @ApiResponse(responseCode = "200", description = "Liste der Kundennummern eines Lieferanten.")
    @ApiResponse(responseCode = "404", description = "Lieferant nicht gefunden.")
    public ResponseEntity<List<CustomerIdResponseDTO>> getAllCustomerIds(
            @Parameter(description = "Die eindeutige ID des Lieferanten, für welchen die Kundennummern abgerufen werden sollen.") @PathVariable("supplier-id") Integer id) {
        if (!supplierService.existsSupplierById(id))
            throw new NotFoundException("Lieferant nicht gefunden.");
        return customerIdService.getAllCustomerIds(id);
    }

    @PostMapping("/{supplier-id}/customer_ids")
    @Operation(description = "Erstellt eine Kundennummer für einen Lieferanten.")
    @ApiResponse(responseCode = "201", description = "Kundennummer erfolgreich erstellt.")
    @ApiResponse(responseCode = "404", description = "Lieferant nicht gefunden.")
    @ApiResponse(responseCode = "409", description = "Lieferantennummer bereits vorhanden für diesen Lieferanten.\nVerweis auf eine nicht existierende Entität.")
    @ApiResponse(responseCode = "500", description = "Interner Serverfehler.")
    public ResponseEntity<CustomerIdResponseDTO> createCustomerId(
            @Parameter(description = "Die eindeutige ID des Lieferanten, für welchen die Kundennummer erstellt werden soll.") @PathVariable("supplier-id") Integer id,
            @RequestBody CustomerIdRequestDTO dto) {
        return customerIdService.createCustomerId(id, dto);
    }

    /**
     * @deprecated Remove this endpoint in a future release.
     */
    @Deprecated(since = "api-1.8.x")
    @GetMapping("/addresses")
    @Operation(description = "Gibt eine Liste aller Lieferantenadressen zurück.")
    @ApiResponse(responseCode = "200", description = "Liste aller Adressen.")
    public ResponseEntity<List<AddressResponseDTO>> getAddresses() {
        return addressService.getSupplierAddresses();
    }

    @GetMapping("/{supplier-id}/address")
    @Operation(description = "Gibt die Adresse des angegebenen Lieferanten zurück.")
    @ApiResponse(responseCode = "200", description = "Adresse des Lieferanten.")
    @ApiResponse(responseCode = "404", description = "Lieferant nicht gefunden. Dieser Lieferant besitzt keine Adresse.")
    public ResponseEntity<AddressResponseDTO> getAddressOfSupplier(
            @Parameter(description = "Die ID des Lieferanten, dessen Adresse abgerufen werden soll.") @PathVariable("supplier-id") Integer id) {
        if (!supplierService.existsSupplierById(id))
            throw new NotFoundException("Person nicht gefunden.");
        return addressService.getAddressOfSupplier(id);
    }

}
