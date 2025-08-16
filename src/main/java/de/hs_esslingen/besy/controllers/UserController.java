package de.hs_esslingen.besy.controllers;

import de.hs_esslingen.besy.dtos.response.UserResponseDTO;
import de.hs_esslingen.besy.services.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<UserResponseDTO> getUser(@PathVariable("id") String id) {
        return userService.getUserById(id);
    }
}
