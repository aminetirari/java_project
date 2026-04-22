package com.shopflow.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CartDTO {
    private Long id;
    private Long customerId;
    private List<CartItemDTO> lignes;
    private Double sousTotal;
    private String codePromo;
    private Double remise;
    private Double totalCart;
}
