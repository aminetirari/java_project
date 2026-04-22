package com.shopflow.service;

import com.shopflow.dto.ReviewCreateDTO;
import com.shopflow.dto.ReviewDTO;
import com.shopflow.entity.Product;
import com.shopflow.entity.Review;
import com.shopflow.entity.User;
import com.shopflow.exception.ResourceNotFoundException;
import com.shopflow.mapper.ReviewMapper;
import com.shopflow.repository.ProductRepository;
import com.shopflow.repository.ReviewRepository;
import com.shopflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ReviewMapper reviewMapper;

    @Transactional(readOnly = true)
    public List<ReviewDTO> getApprovedReviewsForProduct(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Produit introuvable");
        }
        return reviewMapper.toDtoList(reviewRepository.findByProductIdAndApprouveTrue(productId));
    }

    @Transactional
    public ReviewDTO addReview(String email, Long productId, ReviewCreateDTO dto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Produit introuvable"));

        // Règles métiers : Un seul avis par client et par produit (simplifié ici avec Repository)
        if (reviewRepository.existsByCustomerIdAndProductId(user.getId(), productId)) {
            throw new IllegalStateException("Vous avez déjà laissé un avis pour ce produit.");
        }

        // TODO : Vérifier également que l'utilisateur a bien acheté le produit (statut PAYE ou DELIVERED).
        
        Review review = Review.builder()
                .customer(user)
                .product(product)
                .note(dto.getNote())
                .commentaire(dto.getCommentaire())
                .approuve(true) // Auto-approuvé pour le MVP
                .build();

        return reviewMapper.toDto(reviewRepository.save(review));
    }

    @Transactional
    public void deleteReview(Long reviewId, String email) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Avis introuvable"));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        // Autoriser la suppression seulement si c'est le sien, ou on est ADMIN (pas montré ici)
        if (!review.getCustomer().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Vous n'êtes pas autorisé à supprimer cet avis.");
        }

        reviewRepository.delete(review);
    }
}
