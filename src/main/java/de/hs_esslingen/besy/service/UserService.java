package de.hs_esslingen.besy.service;

import de.hs_esslingen.besy.dto.UserResponseDTO;
import de.hs_esslingen.besy.exception.NotFoundException;
import de.hs_esslingen.besy.mapper.UserResponseMapper;
import de.hs_esslingen.besy.model.User;
import de.hs_esslingen.besy.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserResponseMapper userResponseMapper;

    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        List<User> users = userRepository.findAll();
        List<UserResponseDTO> userResponseDTOS = userResponseMapper.toDto(users);
        return ResponseEntity.ok(userResponseDTOS);
    }

    public ResponseEntity<UserResponseDTO> getUserById(String id) {
        return userRepository.findById(id)
                .map(user -> ResponseEntity.ok(userResponseMapper.toDto(user)))
                .orElseThrow(() -> new NotFoundException("User with id " + id + " not found"));
    }
}
