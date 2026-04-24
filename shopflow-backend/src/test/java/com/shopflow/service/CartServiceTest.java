package com.shopflow.service;

import com.shopflow.dto.CartDTO;
import com.shopflow.dto.CartItemCreateDTO;
import com.shopflow.entity.Cart;
import com.shopflow.entity.Coupon;
import com.shopflow.entity.CouponType;
import com.shopflow.entity.Product;
import com.shopflow.entity.Role;
import com.shopflow.entity.User;
import com.shopflow.mapper.CartMapper;
import com.shopflow.repository.CartItemRepository;
import com.shopflow.repository.CartRepository;
import com.shopflow.repository.CouponRepository;
import com.shopflow.repository.ProductRepository;
import com.shopflow.repository.ProductVariantRepository;
import com.shopflow.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock private CartRepository cartRepository;
    @Mock private CartItemRepository cartItemRepository;
    @Mock private UserRepository userRepository;
    @Mock private ProductRepository productRepository;
    @Mock private ProductVariantRepository variantRepository;
    @Mock private CouponRepository couponRepository;
    @Mock private CartMapper cartMapper;

    @InjectMocks private CartService cartService;

    private User user;
    private Cart cart;
    private Product product;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).email("c@shopflow.com").prenom("J").nom("D")
                .role(Role.CUSTOMER).actif(true).build();
        cart = Cart.builder().id(5L).customer(user).build();
        product = Product.builder().id(10L).nom("PS5").stock(3).actif(true).build();
    }

    @Test
    void addItem_stockInsufficient_throws() {
        when(userRepository.findByEmail("c@shopflow.com")).thenReturn(Optional.of(user));
        when(cartRepository.findByCustomer(user)).thenReturn(Optional.of(cart));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));

        CartItemCreateDTO dto = new CartItemCreateDTO();
        dto.setProductId(10L);
        dto.setQuantite(10); // stock = 3

        assertThatThrownBy(() -> cartService.addItemToCart("c@shopflow.com", dto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Stock");

        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    void applyCoupon_expired_throws() {
        Coupon expired = Coupon.builder()
                .id(99L).code("OLD").type(CouponType.PERCENT)
                .valeur(BigDecimal.TEN).actif(true)
                .dateExpiration(LocalDateTime.now().minusDays(1))
                .usagesActuels(0).build();

        when(userRepository.findByEmail("c@shopflow.com")).thenReturn(Optional.of(user));
        when(cartRepository.findByCustomer(user)).thenReturn(Optional.of(cart));
        when(couponRepository.findByCode("OLD")).thenReturn(Optional.of(expired));

        assertThatThrownBy(() -> cartService.applyCoupon("c@shopflow.com", "OLD"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("expiré");

        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    void applyCoupon_disabled_throws() {
        Coupon disabled = Coupon.builder()
                .id(98L).code("OFF").type(CouponType.PERCENT)
                .valeur(BigDecimal.TEN).actif(false)
                .usagesActuels(0).build();

        when(userRepository.findByEmail("c@shopflow.com")).thenReturn(Optional.of(user));
        when(cartRepository.findByCustomer(user)).thenReturn(Optional.of(cart));
        when(couponRepository.findByCode("OFF")).thenReturn(Optional.of(disabled));

        assertThatThrownBy(() -> cartService.applyCoupon("c@shopflow.com", "OFF"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("désactivé");
    }

    @Test
    void applyCoupon_maxUsageReached_throws() {
        Coupon maxed = Coupon.builder()
                .id(97L).code("MAX").type(CouponType.PERCENT)
                .valeur(BigDecimal.TEN).actif(true)
                .usagesMax(5).usagesActuels(5).build();

        when(userRepository.findByEmail("c@shopflow.com")).thenReturn(Optional.of(user));
        when(cartRepository.findByCustomer(user)).thenReturn(Optional.of(cart));
        when(couponRepository.findByCode("MAX")).thenReturn(Optional.of(maxed));

        assertThatThrownBy(() -> cartService.applyCoupon("c@shopflow.com", "MAX"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("limite");
    }

    @Test
    void applyCoupon_valid_setsCouponOnCart() {
        Coupon ok = Coupon.builder()
                .id(96L).code("BIENVENUE10").type(CouponType.PERCENT)
                .valeur(BigDecimal.TEN).actif(true).usagesActuels(0).build();

        when(userRepository.findByEmail("c@shopflow.com")).thenReturn(Optional.of(user));
        when(cartRepository.findByCustomer(user)).thenReturn(Optional.of(cart));
        when(couponRepository.findByCode("BIENVENUE10")).thenReturn(Optional.of(ok));
        when(cartRepository.save(any(Cart.class))).thenAnswer(inv -> inv.getArgument(0));
        when(cartMapper.toDto(any(Cart.class))).thenReturn(CartDTO.builder().build());

        cartService.applyCoupon("c@shopflow.com", "BIENVENUE10");

        org.assertj.core.api.Assertions.assertThat(cart.getCoupon()).isEqualTo(ok);
    }
}
