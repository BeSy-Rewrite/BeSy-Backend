package de.hs_esslingen.besy.services;

import de.hs_esslingen.besy.dtos.request.UserPreferencesRequestDTO;
import de.hs_esslingen.besy.dtos.response.UserPreferencesResponseDTO;
import de.hs_esslingen.besy.dtos.response.UserResponseDTO;
import de.hs_esslingen.besy.enums.PreferenceType;
import de.hs_esslingen.besy.exceptions.NotFoundException;
import de.hs_esslingen.besy.mappers.request.UserPreferencesRequestMapper;
import de.hs_esslingen.besy.mappers.response.UserPreferencesResponseMapper;
import de.hs_esslingen.besy.mappers.response.UserResponseMapper;
import de.hs_esslingen.besy.models.User;
import de.hs_esslingen.besy.models.UserPreferences;
import de.hs_esslingen.besy.repositories.UserPreferencesRepository;
import de.hs_esslingen.besy.repositories.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserResponseMapper userResponseMapper;
    private final UserPreferencesRepository userPreferencesRepository;
    private final UserPreferencesResponseMapper userPreferencesResponseMapper;
    private final UserPreferencesRequestMapper userPreferencesRequestMapper;


    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        List<User> users = userRepository.findAll();
        List<UserResponseDTO> userResponseDTOS = userResponseMapper.toDto(users);
        return ResponseEntity.ok(userResponseDTOS);
    }

    public ResponseEntity<UserResponseDTO> getUserById(Integer id) {
        return userRepository.findById(id)
                .map(user -> ResponseEntity.ok(userResponseMapper.toDto(user)))
                .orElseThrow(() -> new NotFoundException("Benutzer mit id " + id + " nicht gefunden."));
    }

    public ResponseEntity<UserResponseDTO> getUserByKeycloakUUID(Jwt jwt){
        User user = userRepository.findByKeycloakUUID(jwt.getSubject());
        return ResponseEntity.ok(userResponseMapper.toDto(user));
    }


    public ResponseEntity<List<UserPreferencesResponseDTO>> getUserPreferencesByPreferenceType(Jwt jwt, PreferenceType preferenceType) {

        User user = userRepository.findOptionalByKeycloakUUID(jwt.getSubject()).orElseThrow(() -> new NotFoundException("Benutzer existiert nicht."));
        List<UserPreferences> userPreferences = userPreferencesRepository.getUserPreferencesByUser_IdAndPreferenceType(user.getId(), preferenceType);
        List<UserPreferencesResponseDTO> response = userPreferencesResponseMapper.toDto(userPreferences);
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<UserPreferencesResponseDTO> addUserPreference(Jwt jwt, UserPreferencesRequestDTO requestDTO) {
        User user = userRepository.findOptionalByKeycloakUUID(jwt.getSubject()).orElseThrow(() -> new NotFoundException("Benutzer existiert nicht."));

        UserPreferences preferences = userPreferencesRequestMapper.toEntity(requestDTO);
        preferences.setUser(user);
        UserPreferences savedPreferences = userPreferencesRepository.save(preferences);
        return ResponseEntity.ok(userPreferencesResponseMapper.toDto(savedPreferences));
    }

    public ResponseEntity<Void> deleteUserPreferences(Jwt jwt, Integer preferenceId) {
        User user = userRepository.findOptionalByKeycloakUUID(jwt.getSubject()).orElseThrow(() -> new NotFoundException("Benutzer existiert nicht."));

        userPreferencesRepository.deleteByIdAndUser(preferenceId, user);
        return ResponseEntity.noContent().build();
    }

}
