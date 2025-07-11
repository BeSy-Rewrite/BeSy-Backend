package de.hs_esslingen.besy.service;

import de.hs_esslingen.besy.dto.response.PreferredListResponseDTO;
import de.hs_esslingen.besy.mapper.response.PreferredListResponseMapper;
import de.hs_esslingen.besy.model.PreferredList;
import de.hs_esslingen.besy.repository.PreferredListRepository;
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
