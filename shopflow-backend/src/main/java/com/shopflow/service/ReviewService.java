package com.shopflow.service;

import com.shopflow.dto.ReviewCreateDTO;
import com.shopflow.dto.ReviewDTO;
import com.shopflow.dto.ReviewableItemDTO;
import com.shopflow.entity.Order;
import com.shopflow.entity.OrderItem;
import com.shopflow.entity.OrderStatus;
import com.shopflow.entity.Product;
import com.shopflow.entity.Review;
import com.shopflow.entity.Role;
import com.shopflow.entity.User;
import com.shopflow.exception.ResourceNotFoundException;
import com.shopflow.mapper.ReviewMapper;
import com.shopflow.repository.OrderRepository;
import com.shopflow.repository.ProductRepository;
import com.shopflow.repository.ReviewRepository;
import com.shopflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private static final Set<OrderStatus> REVIEWABLE_STATUSES =
            EnumSet.of(OrderStatus.PAID, OrderStatus.PAYE, OrderStatus.PROCESSING, OrderStatus.SHIPPED, OrderStatus.DELIVERED);

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final ReviewMapper reviewMapper;

    @Transactional(readOnly = true)
    public List<ReviewDTO> getApprovedReviewsForProduct(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Produit introuvable");
        }
        return reviewMapper.toDtoList(reviewRepository.findByProductIdAndApprouveTrue(productId));
    }

    @Transactional(readOnly = true)
    public List<ReviewDTO> getAllReviewsForProduct(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Produit introuvable");
        }
        return reviewMapper.toDtoList(reviewRepository.findByProductId(productId));
    }

    @Transactional(readOnly = true)
    public List<ReviewDTO> getPending() {
        return reviewMapper.toDtoList(reviewRepository.findByApprouveFalse());
    }

    @Transactional(readOnly = true)
    public List<ReviewDTO> getMyReviews(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));
        return reviewMapper.toDtoList(reviewRepository.findByCustomerId(user.getId()));
    }

    @Transactional
    public ReviewDTO addReview(String email, Long productId, ReviewCreateDTO dto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Produit introuvable"));

        if (reviewRepository.existsByCustomerIdAndProductId(user.getId(), productId)) {
            throw new IllegalStateException("Vous avez déjà laissé un avis pour ce produit.");
        }

        Review review = Review.builder()
                .customer(user)
                .product(product)
                .note(dto.getNote())
                .commentaire(dto.getCommentaire())
                .approuve(true)
                .build();

        return reviewMapper.toDto(reviewRepository.save(review));
    }

    @Transactional
    public ReviewDTO approve(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Avis introuvable"));
        review.setApprouve(true);
        return reviewMapper.toDto(reviewRepository.save(review));
    }

    @Transactional
    public void deleteReview(Long reviewId, String email) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Avis introuvable"));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        if (!review.getCustomer().getId().equals(user.getId()) && user.getRole() != Role.ADMIN) {
            throw new IllegalArgumentException("Vous n'êtes pas autorisé à supprimer cet avis.");
        }

        reviewRepository.delete(review);
    }

    @Transactional(readOnly = true)
    public List<ReviewableItemDTO> getReviewable(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        Set<Long> alreadyReviewed = new HashSet<>();
        for (Review r : reviewRepository.findByCustomerId(user.getId())) {
            alreadyReviewed.add(r.getProduct().getId());
        }

        List<ReviewableItemDTO> result = new ArrayList<>();
        Set<Long> seen = new HashSet<>();
        List<Order> orders = orderRepository.findByCustomer(user);
        orders.sort((a, b) -> b.getDateCommande().compareTo(a.getDateCommande()));
        for (Order order : orders) {
            if (!REVIEWABLE_STATUSES.contains(order.getStatut())) continue;
            for (OrderItem line : order.getLignes()) {
                Product product = line.getProduct();
                if (product == null) continue;
                Long pid = product.getId();
                if (alreadyReviewed.contains(pid) || !seen.add(pid)) continue;
                String image = (product.getImages() != null && !product.getImages().isEmpty())
                        ? product.getImages().get(0) : null;
                result.add(ReviewableItemDTO.builder()
                        .productId(pid)
                        .productNom(product.getNom())
                        .productImage(image)
                        .orderId(order.getId())
                        .numeroCommande(order.getNumeroCommande())
                        .dateCommande(order.getDateCommande())
                        .orderStatus(order.getStatut().name())
                        .build());
            }
        }
        return result;
    }

    @Transactional(readOnly = true)
    public Double averageNote(Long productId) {
        Double v = reviewRepository.averageNoteForProduct(productId);
        return v == null ? 0.0 : v;
    }
}
