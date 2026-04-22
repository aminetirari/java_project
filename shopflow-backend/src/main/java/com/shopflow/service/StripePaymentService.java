package com.shopflow.service;

import com.shopflow.dto.PaymentIntentResponseDTO;
import com.shopflow.entity.Order;
import com.shopflow.entity.OrderStatus;
import com.shopflow.exception.ResourceNotFoundException;
import com.shopflow.repository.OrderRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class StripePaymentService {

    private final OrderRepository orderRepository;

    @Value("${stripe.api.secret-key}")
    private String secretKey;

    @PostConstruct
    public void init() {
        Stripe.apiKey = secretKey;
    }

    @Transactional
    public PaymentIntentResponseDTO createPaymentIntent(Long orderId, String email) throws StripeException {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Commande introuvable"));

        if (!order.getCustomer().getEmail().equals(email)) {
            throw new IllegalArgumentException("Action non autorisée sur cette commande.");
        }

        if (order.getStatut() != OrderStatus.PENDING) {
            throw new IllegalStateException("Le paiement ne peut être initialisé que pour une commande PENDING.");
        }

        // Stripe utilise des centimes pour les devises comme EUR/USD
        long amount = order.getTotalTTC().multiply(BigDecimal.valueOf(100)).longValue();

        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amount)
                .setCurrency("eur")
                .putMetadata("orderId", String.valueOf(orderId))
                .putMetadata("userEmail", email)
                .build();

        PaymentIntent paymentIntent = PaymentIntent.create(params);

        // Sauvegarder l'ID d'intention sur la commande pour validation future
        order.setPaymentIntentId(paymentIntent.getId());
        orderRepository.save(order);

        return PaymentIntentResponseDTO.builder()
                .clientSecret(paymentIntent.getClientSecret())
                .paymentIntentId(paymentIntent.getId())
                .build();
    }

    @Transactional
    public void validatePayment(Long orderId, String email) throws StripeException {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Commande introuvable"));

        if (!order.getCustomer().getEmail().equals(email)) {
            throw new IllegalArgumentException("Action non autorisée.");
        }

        if (order.getPaymentIntentId() == null) {
            throw new IllegalStateException("Aucun paiement Stripe associé à cette commande.");
        }

        PaymentIntent paymentIntent = PaymentIntent.retrieve(order.getPaymentIntentId());

        if ("succeeded".equals(paymentIntent.getStatus())) {
            order.setStatut(OrderStatus.PAYE);
            orderRepository.save(order);
        } else {
            throw new IllegalStateException("Le paiement n'est pas encore validé. Statut : " + paymentIntent.getStatus());
        }
    }
}
