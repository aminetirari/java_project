package com.shopflow.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentIntentResponseDTO {
    private String clientSecret;
    private String paymentIntentId;
}
