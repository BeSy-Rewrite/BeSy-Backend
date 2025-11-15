package de.hs_esslingen.besy.controllers;

import de.hs_esslingen.besy.dtos.request.UserPreferencesRequestDTO;
import de.hs_esslingen.besy.dtos.response.UserPreferencesResponseDTO;
import de.hs_esslingen.besy.dtos.response.UserResponseDTO;
import de.hs_esslingen.besy.services.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("${api.prefix}/users")
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping
    @RequestMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUser(@PathVariable("id") Integer id) {
        return userService.getUserById(id);
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getCurrentUser(@AuthenticationPrincipal Jwt jwt) {
        return userService.getUserByKeycloakUUID(jwt);
    }

    @GetMapping("/{id}/preferences")
    public ResponseEntity<UserPreferencesResponseDTO> getUserPreferences(@PathVariable("id") Integer id) {
        return userService.getUserPreferences(id);
    }

    @PostMapping("/{id}/preferences")
    public ResponseEntity<UserPreferencesResponseDTO> addUserPreferences(
            @PathVariable("id") Integer id,
            @RequestBody UserPreferencesRequestDTO requestDTO) {
        return userService.addUserPreferences(id, requestDTO);
    }

    @DeleteMapping("/{id}/preferences")
    public ResponseEntity<UserPreferencesResponseDTO> deleteUserPreferences(
            @PathVariable("id") Integer id,
            @RequestBody UserPreferencesRequestDTO requestDTO
    ){
        return userService.deleteUserPreferences(id, requestDTO);
    }

}
