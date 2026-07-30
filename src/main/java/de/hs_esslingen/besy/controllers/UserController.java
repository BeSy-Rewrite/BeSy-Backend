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
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("${api.prefix}/users")
public class UserController {

    private final UserService userService;
    private final UserResponseMapper userResponseMapper;

    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUser(@PathVariable("id") long id) {
        return ResponseEntity.ok(userResponseMapper.toDto(userService.getUserById(id)
                .orElseThrow(() -> new NotFoundException("Benutzer mit id " + id + " nicht gefunden."))));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getCurrentUser(@AuthenticationPrincipal Jwt jwt) {
        return userService.getUserByKeycloakUUID(jwt);
    }

    @GetMapping("/me/preferences")
    public ResponseEntity<List<UserPreferencesResponseDTO>> getUserPreferences(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(value = "type", required = false) String type) {
        return userService.getUserPreferencesByPreferenceType(jwt, type);
    }

    @PostMapping("/me/preferences")
    public ResponseEntity<UserPreferencesResponseDTO> addUserPreferences(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody UserPreferencesRequestDTO requestDTO) {
        return userService.addUserPreference(jwt, requestDTO);
    }

    @PutMapping("/me/preferences/{id}")
    public ResponseEntity<UserPreferencesResponseDTO> updateUserPreferences(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("id") Integer id,
            @RequestBody UserPreferencesRequestDTO requestDTO) {
        return userService.updateUserPreferences(jwt, requestDTO, id);
    }

    @DeleteMapping("/me/preferences/{id}")
    public ResponseEntity<Void> deleteUserPreferences(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("id") Integer id) {
        return userService.deleteUserPreferences(jwt, id);
    }

}
