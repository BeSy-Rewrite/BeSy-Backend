package de.hs_esslingen.besy.services;

import de.hs_esslingen.besy.dtos.request.UserPreferencesRequestDTO;
import de.hs_esslingen.besy.dtos.response.UserPreferencesResponseDTO;
import de.hs_esslingen.besy.dtos.response.UserResponseDTO;
import de.hs_esslingen.besy.exceptions.NotFoundException;
import de.hs_esslingen.besy.mappers.response.UserPreferencesResponseMapper;
import de.hs_esslingen.besy.mappers.response.UserResponseMapper;
import de.hs_esslingen.besy.models.User;
import de.hs_esslingen.besy.repositories.UserRepository;
import lombok.AllArgsConstructor;
import org.apache.coyote.Response;
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
    private final UserPreferencesResponseMapper userPreferencesResponseMapper;

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


    public ResponseEntity<UserPreferencesResponseDTO> getUserPreferences(Integer id) {
        User user = userRepository.findById(id).orElseThrow(() -> new NotFoundException("Benutzer mit id " + id + " nicht gefunden."));

        UserPreferencesResponseDTO responseDTO = userPreferencesResponseMapper.toDto(user);
        return ResponseEntity.ok(responseDTO);
    }

    public ResponseEntity<UserPreferencesResponseDTO> addUserPreferences(Integer id, UserPreferencesRequestDTO requestDTO) {
        User user = userRepository.findById(id).orElseThrow(() -> new NotFoundException("Benutzer mit id " + id + " nicht gefunden."));

        Set<String> currentUserOrderFilterPreferences = user.getOrderFilterPreferences();
        currentUserOrderFilterPreferences.addAll(requestDTO.getOrderFilterPreferences());
        user.setOrderFilterPreferences(currentUserOrderFilterPreferences);
        User savedUser = userRepository.save(user);

        UserPreferencesResponseDTO responseDTO = userPreferencesResponseMapper.toDto(savedUser);
        return ResponseEntity.ok(responseDTO);
    }

    public ResponseEntity<UserPreferencesResponseDTO> deleteUserPreferences(Integer id, UserPreferencesRequestDTO requestDTO) {
        User user = userRepository.findById(id).orElseThrow(() -> new NotFoundException("Benutzer mit id " + id + " nicht gefunden."));

        Set<String> currentUserOrderFilterPreferences = user.getOrderFilterPreferences();
        currentUserOrderFilterPreferences.removeAll(requestDTO.getOrderFilterPreferences());
        user.setOrderFilterPreferences(currentUserOrderFilterPreferences);

        User savedUser = userRepository.save(user);
        return ResponseEntity.ok(userPreferencesResponseMapper.toDto(savedUser));
    }

}
