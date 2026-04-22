package com.shopflow.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class JwtResponseDTO {
    private String token;
    private Long id;
    private String email;
    private List<String> roles;
}
