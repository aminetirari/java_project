package com.shopflow.dto;

import com.shopflow.entity.CouponType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CouponCreateDTO {
    @NotBlank(message = "Le code est obligatoire")
    private String code;

    @NotNull(message = "Le type de coupon est obligatoire")
    private CouponType type;

    @NotNull(message = "La valeur du coupon est obligatoire")
    @DecimalMin(value = "0.0", inclusive = false, message = "La valeur doit être strictement positive")
    private BigDecimal valeur;

    private LocalDateTime dateExpiration;

    private Integer usagesMax;
}
