package com.shopflow.dto;

import com.shopflow.entity.CouponType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class CouponDTO {
    private Long id;
    private String code;
    private CouponType type;
    private BigDecimal valeur;
    private LocalDateTime dateExpiration;
    private Integer usagesMax;
    private Integer usagesActuels;
    private Boolean actif;
}
