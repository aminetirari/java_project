package com.shopflow.controller;

import com.shopflow.dto.UserCreateDTO;
import com.shopflow.dto.UserDTO;
import com.shopflow.security.UserDetailsImpl;
import com.shopflow.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> createUser(@Valid @RequestBody UserCreateDTO userCreateDTO) {
        return new ResponseEntity<>(userService.createUser(userCreateDTO), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> updateUser(@PathVariable Long id, @Valid @RequestBody UserCreateDTO userCreateDTO) {
        return ResponseEntity.ok(userService.updateUser(id, userCreateDTO));
    }

    @PutMapping("/{id}/active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> setActive(@PathVariable Long id,
                                             @RequestBody Map<String, Boolean> body,
                                             @AuthenticationPrincipal UserDetailsImpl current) {
        boolean actif = Boolean.TRUE.equals(body.get("actif"));
        if (!actif && current != null && id.equals(current.getId())) {
            throw new IllegalStateException("Vous ne pouvez pas désactiver votre propre compte.");
        }
        return ResponseEntity.ok(userService.setActive(id, actif));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id,
                                           @AuthenticationPrincipal UserDetailsImpl current) {
        if (current != null && id.equals(current.getId())) {
            throw new IllegalStateException("Vous ne pouvez pas supprimer votre propre compte.");
        }
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
