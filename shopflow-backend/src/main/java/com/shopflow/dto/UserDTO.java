package com.shopflow.dto;

import com.shopflow.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private String email;
    private String prenom;
    private String nom;
    private Role role;
    private Boolean actif;
    private LocalDateTime dateCreation;
}
