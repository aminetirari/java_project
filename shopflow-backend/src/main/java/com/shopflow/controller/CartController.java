package com.shopflow.controller;

import com.shopflow.dto.CartDTO;
import com.shopflow.dto.CartItemCreateDTO;
import com.shopflow.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('CUSTOMER','ADMIN')")
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<CartDTO> getCart(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(cartService.getOrCreateCart(userDetails.getUsername()));
    }

    @PostMapping("/items")
    public ResponseEntity<CartDTO> addItemToCart(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CartItemCreateDTO itemDto) {
        return ResponseEntity.ok(cartService.addItemToCart(userDetails.getUsername(), itemDto));
    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<CartDTO> updateItemQuantity(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long itemId,
            @RequestParam Integer quantite) {
        return ResponseEntity.ok(cartService.updateItemQuantity(userDetails.getUsername(), itemId, quantite));
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<CartDTO> removeItemFromCart(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long itemId) {
        return ResponseEntity.ok(cartService.removeItemFromCart(userDetails.getUsername(), itemId));
    }

    @PostMapping("/coupon")
    public ResponseEntity<CartDTO> applyCoupon(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody ApplyCouponRequest body) {
        return ResponseEntity.ok(cartService.applyCoupon(userDetails.getUsername(), body.getCode()));
    }

    @DeleteMapping("/coupon")
    public ResponseEntity<CartDTO> removeCoupon(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(cartService.removeCoupon(userDetails.getUsername()));
    }

    @lombok.Data
    public static class ApplyCouponRequest {
        private String code;
    }
}
