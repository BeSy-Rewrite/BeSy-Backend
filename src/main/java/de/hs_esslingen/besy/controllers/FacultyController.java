package de.hs_esslingen.besy.controllers;

import de.hs_esslingen.besy.dtos.response.FacultyResponseDTO;
import de.hs_esslingen.besy.services.FacultyService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("${api.prefix}/faculties")
public class FacultyController {

    private final FacultyService facultyService;

    @GetMapping
    public ResponseEntity<List<FacultyResponseDTO>> getFaculties() {
        return facultyService.getAllFaculties();
    }
}
