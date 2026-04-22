package com.shopflow.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AddressDTO {
    private Long id;
    private Long userId; // For reference
    private String rue;
    private String ville;
    private String codePostal;
    private String pays;
    private Boolean principal;
}
