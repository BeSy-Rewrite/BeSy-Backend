package de.hs_esslingen.besy.controller;

import de.hs_esslingen.besy.dto.response.PreferredListResponseDTO;
import de.hs_esslingen.besy.service.PreferredListService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("${api.prefix}/preferred_lists")
public class PreferredListController {

    private final PreferredListService preferredListService;

    @GetMapping
    public ResponseEntity<List<PreferredListResponseDTO>> getPreferredLists() {
        return preferredListService.getPreferredLists();
    }

}
