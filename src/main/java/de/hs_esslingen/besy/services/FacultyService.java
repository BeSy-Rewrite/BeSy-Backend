package de.hs_esslingen.besy.services;

import de.hs_esslingen.besy.dtos.response.FacultyResponseDTO;
import de.hs_esslingen.besy.mappers.response.FacultyResponseMapper;
import de.hs_esslingen.besy.repositories.FacultyRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class FacultyService {

    private final FacultyRepository facultyRepository;
    private final FacultyResponseMapper facultyResponseMapper;

    public ResponseEntity<List<FacultyResponseDTO>> getAllFaculties() {
        List<Faculty> faculties = facultyRepository.findAll();
        List<FacultyResponseDTO> facultyResponseDTOs = facultyResponseMapper.toDto(faculties);
        return ResponseEntity.ok(facultyResponseDTOs);
    }
}
