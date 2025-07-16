package de.hs_esslingen.besy.services;

import de.hs_esslingen.besy.dtos.response.PreferredListResponseDTO;
import de.hs_esslingen.besy.mappers.response.PreferredListResponseMapper;
import de.hs_esslingen.besy.models.PreferredList;
import de.hs_esslingen.besy.repositories.PreferredListRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class PreferredListService {

    private final PreferredListRepository preferredListRepository;
    private final PreferredListResponseMapper preferredListResponseMapper;

    public ResponseEntity<List<PreferredListResponseDTO>> getPreferredLists() {
        List<PreferredList> preferredLists = preferredListRepository.findAll();
        List<PreferredListResponseDTO> preferredListResponseDTOS = preferredListResponseMapper.toDto(preferredLists);
        return ResponseEntity.ok(preferredListResponseDTOS);
    }
}
