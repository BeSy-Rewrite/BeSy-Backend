package de.hs_esslingen.besy.controller;

import de.hs_esslingen.besy.model.Faculty;
import de.hs_esslingen.besy.repository.FacultyRepository;
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

    private final FacultyRepository facultyRepository;

    @GetMapping
    public ResponseEntity<List<Faculty>> getAddressTypes() {
        return ResponseEntity.ok(facultyRepository.findAll());
    }
}
