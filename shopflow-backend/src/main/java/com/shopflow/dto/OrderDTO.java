package com.shopflow.dto;

import com.shopflow.entity.OrderStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class OrderDTO {
    private Long id;
    private String numeroCommande;
    private Long customerId;
    private LocalDateTime dateCommande;
    private String paymentIntentId;

    private OrderStatus status;
    private BigDecimal sousTotal;
    private String codePromo;
    private BigDecimal montantRemise;
    private BigDecimal fraisLivraison;
    private BigDecimal total;
    @Builder.Default
    private List<OrderItemDTO> lignes = new ArrayList<>();
}
