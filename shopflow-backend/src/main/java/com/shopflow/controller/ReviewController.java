package com.shopflow.controller;

import com.shopflow.dto.ReviewCreateDTO;
import com.shopflow.dto.ReviewDTO;
import com.shopflow.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/reviews/product/{productId}")
    public ResponseEntity<List<ReviewDTO>> getReviewsForProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(reviewService.getApprovedReviewsForProduct(productId));
    }

    @GetMapping("/products/{productId}/reviews")
    public ResponseEntity<List<ReviewDTO>> getProductReviews(@PathVariable Long productId) {
        return ResponseEntity.ok(reviewService.getApprovedReviewsForProduct(productId));
    }

    @PostMapping("/products/{productId}/reviews")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ReviewDTO> addReview(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long productId,
            @Valid @RequestBody ReviewCreateDTO reviewDTO) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reviewService.addReview(userDetails.getUsername(), productId, reviewDTO));
    }

    @PostMapping("/reviews")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ReviewDTO> addReviewFlat(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ReviewFlatCreateRequest body) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reviewService.addReview(userDetails.getUsername(), body.getProductId(),
                        ReviewCreateDTO.from(body.getNote(), body.getCommentaire())));
    }

    @GetMapping("/reviews/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ReviewDTO>> pending() {
        return ResponseEntity.ok(reviewService.getPending());
    }

    @GetMapping("/reviews/my")
    public ResponseEntity<List<ReviewDTO>> myReviews(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(reviewService.getMyReviews(userDetails.getUsername()));
    }

    @PutMapping("/reviews/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReviewDTO> approve(@PathVariable Long id) {
        return ResponseEntity.ok(reviewService.approve(id));
    }

    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long reviewId) {
        reviewService.deleteReview(reviewId, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    @lombok.Data
    public static class ReviewFlatCreateRequest {
        private Long productId;
        private Integer note;
        private String commentaire;
    }
}
