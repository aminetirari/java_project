package com.shopflow.controller;

import com.shopflow.dto.PaymentIntentResponseDTO;
import com.shopflow.service.StripePaymentService;
import com.stripe.exception.StripeException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final StripePaymentService paymentService;

    @PostMapping("/intent/{orderId}")
    public ResponseEntity<PaymentIntentResponseDTO> createPaymentIntent(@AuthenticationPrincipal UserDetails userDetails,
                                                                        @PathVariable Long orderId) {
        try {
            return ResponseEntity.ok(paymentService.createPaymentIntent(orderId, userDetails.getUsername()));
        } catch (StripeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PostMapping("/validate/{orderId}")
    public ResponseEntity<?> validatePayment(@AuthenticationPrincipal UserDetails userDetails,
                                             @PathVariable Long orderId) {
        try {
            paymentService.validatePayment(orderId, userDetails.getUsername());
            return ResponseEntity.ok().body(Map.of("message", "Paiement validé avec succès"));
        } catch (StripeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
