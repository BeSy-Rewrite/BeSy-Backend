package de.hs_esslingen.besy.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.hs_esslingen.besy.dtos.request.UserPreferencesRequestDTO;
import de.hs_esslingen.besy.dtos.response.UserPreferencesResponseDTO;
import de.hs_esslingen.besy.dtos.response.UserResponseDTO;
import de.hs_esslingen.besy.exceptions.NotFoundException;
import de.hs_esslingen.besy.mappers.request.UserPreferencesRequestMapper;
import de.hs_esslingen.besy.mappers.response.UserPreferencesResponseMapper;
import de.hs_esslingen.besy.mappers.response.UserResponseMapper;
import de.hs_esslingen.besy.models.User;
import de.hs_esslingen.besy.models.UserPreferences;
import de.hs_esslingen.besy.repositories.UserPreferencesRepository;
import de.hs_esslingen.besy.repositories.UserRepository;
import de.hs_esslingen.besy.security.KeycloakAuthenticationConverter;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    @Value("${user-role-name}")
    private String userRoleName;

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

    public ResponseEntity<UserResponseDTO> getUserByKeycloakUUID(Jwt jwt) {
        User user = this.resolveUserFromJwt(jwt);
        return ResponseEntity.ok(userResponseMapper.toDto(user));
    }

    public ResponseEntity<List<UserPreferencesResponseDTO>> getUserPreferencesByPreferenceType(Jwt jwt,
            String preferenceType) {

        User user = this.resolveUserFromJwt(jwt);
        List<UserPreferences> userPreferences = userPreferencesRepository
                .getUserPreferencesByUser_IdAndPreferenceType(user.getId(), preferenceType);
        List<UserPreferencesResponseDTO> response = userPreferencesResponseMapper.toDto(userPreferences);
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<UserPreferencesResponseDTO> addUserPreference(Jwt jwt, UserPreferencesRequestDTO requestDTO) {
        User user = this.resolveUserFromJwt(jwt);

        UserPreferences preferences = userPreferencesRequestMapper.toEntity(requestDTO);
        preferences.setUser(user);
        UserPreferences savedPreferences = userPreferencesRepository.save(preferences);
        return ResponseEntity.ok(userPreferencesResponseMapper.toDto(savedPreferences));
    }

    public ResponseEntity<UserPreferencesResponseDTO> updateUserPreferences(Jwt jwt,
            UserPreferencesRequestDTO requestDTO, Integer id) {
        User user = this.resolveUserFromJwt(jwt);

        UserPreferences preferences = userPreferencesRepository.findByIdAndUser(id, user);
        if (preferences == null)
            throw new NotFoundException("Präferenz existiert nicht.");

        userPreferencesRequestMapper.partialUpdate(preferences, requestDTO);
        return ResponseEntity.ok(userPreferencesResponseMapper.toDto(preferences));
    }

    @Transactional
    public ResponseEntity<Void> deleteUserPreferences(Jwt jwt, Integer preferenceId) {
        User user = this.resolveUserFromJwt(jwt);

        userPreferencesRepository.deleteByIdAndUser(preferenceId, user);
        return ResponseEntity.noContent().build();
    }

    @Transactional
    public User resolveUserFromJwt(Jwt jwt) {
        return userRepository.findOptionalByKeycloakUUID(jwt.getSubject())
                .or(() -> {
                    Optional<User> user = userRepository
                            .findOptionalByEmail(jwt.getClaimAsString("email").toLowerCase());
                    user.ifPresent(u -> {
                        u.setKeycloakUUID(jwt.getSubject());
                        u = userRepository.save(u);
                    });
                    return user;
                })
                .orElseGet(() -> {
                    if (KeycloakAuthenticationConverter.hasRole(jwt, userRoleName)) {
                        try {
                            User newUser = new User();
                            newUser.setKeycloakUUID(jwt.getSubject());
                            newUser.setEmail(jwt.getClaimAsString("email").toLowerCase());
                            newUser.setName(jwt.getClaimAsString("given_name"));
                            newUser.setSurname(jwt.getClaimAsString("family_name"));
                            return userRepository.save(newUser);
                        } catch (DataIntegrityViolationException e) {
                            // Race condition: Another thread created the user simultaneously
                            // Try to retrieve the user by keycloak_uuid
                            return userRepository.findOptionalByKeycloakUUID(jwt.getSubject())
                                    .orElseThrow(
                                            () -> new NotFoundException("Benutzer mit Keycloak UUID " + jwt.getSubject()
                                                    + " konnte nicht gefunden oder erstellt werden."));
                        }
                    } else {
                        throw new NotFoundException("Benutzer mit Keycloak UUID " + jwt.getSubject()
                                + " konnte nicht gefunden oder erstellt werden.");
                    }
                });
    }
}
