package de.hs_esslingen.besy.services;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserResponseMapper userResponseMapper;

    @Mock
    private UserPreferencesRepository userPreferencesRepository;

    @Mock
    private UserPreferencesResponseMapper userPreferencesResponseMapper;

    @Mock
    private UserPreferencesRequestMapper userPreferencesRequestMapper;

    @InjectMocks
    private UserService userService;

    private User user;
    private UserResponseDTO userResponseDTO;
    private UserPreferences preferences;
    private UserPreferencesRequestDTO preferencesRequestDTO;
    private UserPreferencesResponseDTO preferencesResponseDTO;
    private Jwt jwt;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1);
        user.setName("Jane");
        user.setSurname("Doe");
        user.setEmail("jane.doe@example.com");
        user.setKeycloakUUID("kc-123");

        userResponseDTO = new UserResponseDTO("Jane", "Doe", "jane.doe@example.com", "kc-123");

        preferences = new UserPreferences();
        preferences.setId(10);
        preferences.setPreferenceType("table");
        preferences.setPreferences(Map.of("pageSize", 20));
        preferences.setUser(user);

        preferencesRequestDTO = new UserPreferencesRequestDTO("table", Map.of("pageSize", 20));
        preferencesResponseDTO = new UserPreferencesResponseDTO(10, "table", Map.of("pageSize", 20));

        jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject("kc-123")
                .build();
    }

    @Test
    void should_get_all_users() {
        when(userRepository.findAll()).thenReturn(List.of(user));
        when(userResponseMapper.toDto(List.of(user))).thenReturn(List.of(userResponseDTO));

        ResponseEntity<List<UserResponseDTO>> response = userService.getAllUsers();

        assertEquals(200, response.getStatusCode().value());
        assertEquals(List.of(userResponseDTO), response.getBody());
        verify(userRepository).findAll();
        verify(userResponseMapper).toDto(List.of(user));
    }

    @Test
    void should_get_user_by_id_when_exists() {
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(userResponseMapper.toDto(user)).thenReturn(userResponseDTO);

        ResponseEntity<UserResponseDTO> response = userService.getUserById(1);

        assertEquals(200, response.getStatusCode().value());
        assertSame(userResponseDTO, response.getBody());
        verify(userRepository).findById(1);
        verify(userResponseMapper).toDto(user);
    }

    @Test
    void should_throw_not_found_when_user_missing() {
        when(userRepository.findById(1)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class, () -> userService.getUserById(1));

        assertEquals(true, ex.getMessage().contains("1"));
        verify(userRepository).findById(1);
    }

    @Test
    void should_get_user_by_keycloak_uuid() {
        when(userRepository.findByKeycloakUUID("kc-123")).thenReturn(user);
        when(userResponseMapper.toDto(user)).thenReturn(userResponseDTO);

        ResponseEntity<UserResponseDTO> response = userService.getUserByKeycloakUUID(jwt);

        assertEquals(200, response.getStatusCode().value());
        assertSame(userResponseDTO, response.getBody());
        verify(userRepository).findByKeycloakUUID("kc-123");
        verify(userResponseMapper).toDto(user);
    }

    @Test
    void should_get_user_preferences_by_type() {
        when(userRepository.findOptionalByKeycloakUUID("kc-123")).thenReturn(Optional.of(user));
        when(userPreferencesRepository.getUserPreferencesByUser_IdAndPreferenceType(1, "table"))
                .thenReturn(List.of(preferences));
        when(userPreferencesResponseMapper.toDto(List.of(preferences))).thenReturn(List.of(preferencesResponseDTO));

        ResponseEntity<List<UserPreferencesResponseDTO>> response = userService.getUserPreferencesByPreferenceType(jwt, "table");

        assertEquals(200, response.getStatusCode().value());
        assertEquals(List.of(preferencesResponseDTO), response.getBody());
        verify(userRepository).findOptionalByKeycloakUUID("kc-123");
        verify(userPreferencesRepository).getUserPreferencesByUser_IdAndPreferenceType(1, "table");
        verify(userPreferencesResponseMapper).toDto(List.of(preferences));
    }

    @Test
    void should_throw_not_found_when_user_missing_for_preferences() {
        when(userRepository.findOptionalByKeycloakUUID("kc-123")).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> userService.getUserPreferencesByPreferenceType(jwt, "table"));

        assertEquals(true, ex.getMessage().contains("Benutzer"));
        verify(userRepository).findOptionalByKeycloakUUID("kc-123");
        verify(userPreferencesRepository, never()).getUserPreferencesByUser_IdAndPreferenceType(any(), any());
    }

    @Test
    void should_add_user_preference() {
        when(userRepository.findOptionalByKeycloakUUID("kc-123")).thenReturn(Optional.of(user));
        when(userPreferencesRequestMapper.toEntity(preferencesRequestDTO)).thenReturn(preferences);
        when(userPreferencesRepository.save(any(UserPreferences.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userPreferencesResponseMapper.toDto(any(UserPreferences.class))).thenReturn(preferencesResponseDTO);

        ResponseEntity<UserPreferencesResponseDTO> response = userService.addUserPreference(jwt, preferencesRequestDTO);

        assertEquals(200, response.getStatusCode().value());
        assertSame(preferencesResponseDTO, response.getBody());

        ArgumentCaptor<UserPreferences> captor = ArgumentCaptor.forClass(UserPreferences.class);
        verify(userPreferencesRepository).save(captor.capture());
        assertSame(user, captor.getValue().getUser());

        verify(userRepository).findOptionalByKeycloakUUID("kc-123");
        verify(userPreferencesRequestMapper).toEntity(preferencesRequestDTO);
        verify(userPreferencesResponseMapper).toDto(preferences);
    }

    @Test
    void should_throw_not_found_when_user_missing_for_add_preference() {
        when(userRepository.findOptionalByKeycloakUUID("kc-123")).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> userService.addUserPreference(jwt, preferencesRequestDTO));

        assertEquals(true, ex.getMessage().contains("Benutzer"));
        verify(userRepository).findOptionalByKeycloakUUID("kc-123");
        verify(userPreferencesRepository, never()).save(any(UserPreferences.class));
    }

    @Test
    void should_update_user_preferences_when_exists() {
        when(userRepository.findOptionalByKeycloakUUID("kc-123")).thenReturn(Optional.of(user));
        when(userPreferencesRepository.findByIdAndUser(10, user)).thenReturn(preferences);
        when(userPreferencesResponseMapper.toDto(preferences)).thenReturn(preferencesResponseDTO);

        ResponseEntity<UserPreferencesResponseDTO> response = userService.updateUserPreferences(jwt, preferencesRequestDTO, 10);

        assertEquals(200, response.getStatusCode().value());
        assertSame(preferencesResponseDTO, response.getBody());
        verify(userPreferencesRequestMapper).partialUpdate(preferences, preferencesRequestDTO);
        verify(userPreferencesResponseMapper).toDto(preferences);
    }

    @Test
    void should_throw_not_found_when_preference_missing() {
        when(userRepository.findOptionalByKeycloakUUID("kc-123")).thenReturn(Optional.of(user));
        when(userPreferencesRepository.findByIdAndUser(10, user)).thenReturn(null);

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> userService.updateUserPreferences(jwt, preferencesRequestDTO, 10));

        assertEquals(true, ex.getMessage().contains("Präferenz"));
        verify(userPreferencesRepository).findByIdAndUser(10, user);
        verify(userPreferencesRequestMapper, never()).partialUpdate(any(UserPreferences.class), any(UserPreferencesRequestDTO.class));
    }

    @Test
    void should_throw_not_found_when_user_missing_for_update_preference() {
        when(userRepository.findOptionalByKeycloakUUID("kc-123")).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> userService.updateUserPreferences(jwt, preferencesRequestDTO, 10));

        assertEquals(true, ex.getMessage().contains("Benutzer"));
        verify(userRepository).findOptionalByKeycloakUUID("kc-123");
        verify(userPreferencesRepository, never()).findByIdAndUser(any(), any(User.class));
    }

    @Test
    void should_delete_user_preferences() {
        when(userRepository.findOptionalByKeycloakUUID("kc-123")).thenReturn(Optional.of(user));

        ResponseEntity<Void> response = userService.deleteUserPreferences(jwt, 10);

        assertEquals(204, response.getStatusCode().value());
        verify(userPreferencesRepository).deleteByIdAndUser(10, user);
    }

    @Test
    void should_throw_not_found_when_user_missing_for_delete_preference() {
        when(userRepository.findOptionalByKeycloakUUID("kc-123")).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> userService.deleteUserPreferences(jwt, 10));

        assertEquals(true, ex.getMessage().contains("Benutzer"));
        verify(userRepository).findOptionalByKeycloakUUID("kc-123");
        verify(userPreferencesRepository, never()).deleteByIdAndUser(any(), any(User.class));
    }
}
