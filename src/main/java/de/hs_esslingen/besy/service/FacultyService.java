package de.hs_esslingen.besy.service;

import de.hs_esslingen.besy.dto.FacultyResponseDTO;
import de.hs_esslingen.besy.mapper.FacultyResponseMapper;
import de.hs_esslingen.besy.model.Faculty;
import de.hs_esslingen.besy.repository.FacultyRepository;
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
