package de.hs_esslingen.besy.controllers;

import de.hs_esslingen.besy.dtos.response.VatResponseDTO;
import de.hs_esslingen.besy.services.VatService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("${api.prefix}/vats")
public class VatController {

    private final VatService vatService;

    @GetMapping
    public ResponseEntity<List<VatResponseDTO>> getAllVats() {
        return vatService.getAllVats();
    }
}
