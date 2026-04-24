package com.shopflow.service;

import com.shopflow.dto.ReviewCreateDTO;
import com.shopflow.dto.ReviewDTO;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock private ReviewRepository reviewRepository;
    @Mock private ProductRepository productRepository;
    @Mock private UserRepository userRepository;
    @Mock private OrderRepository orderRepository;
    @Mock private ReviewMapper reviewMapper;

    @InjectMocks private ReviewService reviewService;

    private User customer;
    private User otherCustomer;
    private User admin;
    private Product product;

    @BeforeEach
    void setUp() {
        customer = User.builder().id(10L).email("c@shopflow.com")
                .prenom("Jean").nom("Dupont").role(Role.CUSTOMER).actif(true).build();
        otherCustomer = User.builder().id(11L).email("o@shopflow.com")
                .prenom("Other").nom("User").role(Role.CUSTOMER).actif(true).build();
        admin = User.builder().id(1L).email("admin@shopflow.com")
                .prenom("Admin").nom("ShopFlow").role(Role.ADMIN).actif(true).build();
        product = Product.builder().id(100L).nom("PS5").actif(true).build();
    }

    @Test
    void addReview_success() {
        when(userRepository.findByEmail("c@shopflow.com")).thenReturn(Optional.of(customer));
        when(productRepository.findById(100L)).thenReturn(Optional.of(product));
        when(reviewRepository.existsByCustomerIdAndProductId(10L, 100L)).thenReturn(false);
        when(reviewRepository.save(any(Review.class))).thenAnswer(inv -> inv.getArgument(0));
        when(reviewMapper.toDto(any(Review.class))).thenReturn(ReviewDTO.builder().note(5).build());

        ReviewDTO dto = reviewService.addReview("c@shopflow.com", 100L,
                ReviewCreateDTO.from(5, "Super produit"));

        assertThat(dto).isNotNull();
        assertThat(dto.getNote()).isEqualTo(5);
        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    void addReview_alreadyReviewed_throws() {
        when(userRepository.findByEmail("c@shopflow.com")).thenReturn(Optional.of(customer));
        when(productRepository.findById(100L)).thenReturn(Optional.of(product));
        when(reviewRepository.existsByCustomerIdAndProductId(10L, 100L)).thenReturn(true);

        assertThatThrownBy(() -> reviewService.addReview("c@shopflow.com", 100L,
                ReviewCreateDTO.from(4, "Test")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("déjà");

        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    void addReview_productNotFound_throws() {
        when(userRepository.findByEmail("c@shopflow.com")).thenReturn(Optional.of(customer));
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.addReview("c@shopflow.com", 999L,
                ReviewCreateDTO.from(5, "X")))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void approve_setsApprouveTrue() {
        Review pending = Review.builder()
                .id(50L).customer(customer).product(product).note(4)
                .commentaire("Bof").approuve(false).build();
        when(reviewRepository.findById(50L)).thenReturn(Optional.of(pending));
        when(reviewRepository.save(any(Review.class))).thenAnswer(inv -> inv.getArgument(0));
        when(reviewMapper.toDto(any(Review.class))).thenReturn(ReviewDTO.builder().approuve(true).build());

        reviewService.approve(50L);

        assertThat(pending.getApprouve()).isTrue();
        verify(reviewRepository).save(pending);
    }

    @Test
    void deleteReview_byOwner_succeeds() {
        Review mine = Review.builder().id(60L).customer(customer).product(product).build();
        when(reviewRepository.findById(60L)).thenReturn(Optional.of(mine));
        when(userRepository.findByEmail("c@shopflow.com")).thenReturn(Optional.of(customer));

        reviewService.deleteReview(60L, "c@shopflow.com");

        verify(reviewRepository).delete(mine);
    }

    @Test
    void deleteReview_byOtherCustomer_forbidden() {
        Review someoneElse = Review.builder().id(70L).customer(customer).product(product).build();
        when(reviewRepository.findById(70L)).thenReturn(Optional.of(someoneElse));
        when(userRepository.findByEmail("o@shopflow.com")).thenReturn(Optional.of(otherCustomer));

        assertThatThrownBy(() -> reviewService.deleteReview(70L, "o@shopflow.com"))
                .isInstanceOf(IllegalArgumentException.class);

        verify(reviewRepository, never()).delete(any(Review.class));
    }

    @Test
    void deleteReview_byAdmin_succeeds() {
        Review someoneElse = Review.builder().id(80L).customer(customer).product(product).build();
        when(reviewRepository.findById(80L)).thenReturn(Optional.of(someoneElse));
        when(userRepository.findByEmail("admin@shopflow.com")).thenReturn(Optional.of(admin));

        reviewService.deleteReview(80L, "admin@shopflow.com");

        verify(reviewRepository).delete(someoneElse);
    }
}
