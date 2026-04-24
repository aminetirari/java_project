package com.shopflow.service;

import com.shopflow.dto.CartDTO;
import com.shopflow.dto.CartItemCreateDTO;
import com.shopflow.entity.Cart;
import com.shopflow.entity.CartItem;
import com.shopflow.entity.Coupon;
import com.shopflow.entity.Product;
import com.shopflow.entity.ProductVariant;
import com.shopflow.entity.User;
import com.shopflow.exception.ResourceNotFoundException;
import com.shopflow.mapper.CartMapper;
import com.shopflow.repository.CartItemRepository;
import com.shopflow.repository.CartRepository;
import com.shopflow.repository.CouponRepository;
import com.shopflow.repository.ProductRepository;
import com.shopflow.repository.ProductVariantRepository;
import com.shopflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
    private final CouponRepository couponRepository;
    private final CartMapper cartMapper;

    @Transactional
    public CartDTO getOrCreateCart(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User non trouvé"));

        Cart cart = cartRepository.findByCustomer(user)
                .orElseGet(() -> cartRepository.save(Cart.builder().customer(user).build()));

        return cartMapper.toDto(cart);
    }

    @Transactional
    public CartDTO addItemToCart(String email, CartItemCreateDTO itemDto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User non trouvé"));

        Cart cart = cartRepository.findByCustomer(user)
                .orElseGet(() -> cartRepository.save(Cart.builder().customer(user).build()));

        Product product = productRepository.findById(itemDto.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Produit introuvable"));

        ProductVariant variant = null;
        if (itemDto.getVariantId() != null) {
            variant = variantRepository.findById(itemDto.getVariantId())
                    .orElseThrow(() -> new ResourceNotFoundException("Variante introuvable"));
        }

        int stockAvailable = product.getStock() == null ? 0 : product.getStock();
        if (variant != null && variant.getStockSupplementaire() != null) {
            stockAvailable += variant.getStockSupplementaire();
        }
        if (stockAvailable < itemDto.getQuantite()) {
            throw new IllegalStateException("Stock insuffisant");
        }

        // Check if item already exists
        ProductVariant finalVariant = variant;
        Optional<CartItem> existingItem = cart.getLignes().stream()
                .filter(i -> i.getProduct().getId().equals(product.getId()) &&
                        (finalVariant == null ? i.getVariant() == null : i.getVariant() != null && i.getVariant().getId().equals(finalVariant.getId())))
                .findFirst();

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantite(item.getQuantite() + itemDto.getQuantite());
        } else {
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .variant(variant)
                    .quantite(itemDto.getQuantite())
                    .build();
            cart.getLignes().add(newItem);
        }

        return cartMapper.toDto(cartRepository.save(cart));
    }

    @Transactional
    public CartDTO updateItemQuantity(String email, Long itemId, Integer quantite) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User non trouvé"));

        Cart cart = cartRepository.findByCustomer(user)
                .orElseThrow(() -> new ResourceNotFoundException("Panier non trouvé"));

        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Ligne panier introuvable"));

        if (!item.getCart().getId().equals(cart.getId())) {
             throw new IllegalStateException("L'item n'appartient pas à ce panier");
        }

        item.setQuantite(quantite);
        cartItemRepository.save(item);

        return cartMapper.toDto(cart);
    }

    @Transactional
    public CartDTO removeItemFromCart(String email, Long itemId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User non trouvé"));

        Cart cart = cartRepository.findByCustomer(user)
                .orElseThrow(() -> new ResourceNotFoundException("Panier non trouvé"));

        cart.getLignes().removeIf(item -> item.getId().equals(itemId));
        return cartMapper.toDto(cartRepository.save(cart));
    }

    @Transactional
    public CartDTO applyCoupon(String email, String codePromo) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User non trouvé"));

        Cart cart = cartRepository.findByCustomer(user)
                .orElseThrow(() -> new ResourceNotFoundException("Panier non trouvé"));

        Coupon coupon = couponRepository.findByCode(codePromo)
                .orElseThrow(() -> new ResourceNotFoundException("Code de coupon invalide"));

        if (!coupon.getActif()) {
            throw new IllegalStateException("Ce coupon est désactivé");
        }
        
        if (coupon.getDateExpiration() != null && coupon.getDateExpiration().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Ce coupon a expiré");
        }

        if (coupon.getUsagesMax() != null && coupon.getUsagesActuels() >= coupon.getUsagesMax()) {
            throw new IllegalStateException("Ce coupon a atteint sa limite d'utilisation");
        }

        cart.setCoupon(coupon);
        return cartMapper.toDto(cartRepository.save(cart));
    }

    @Transactional
    public CartDTO removeCoupon(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User non trouvé"));

        Cart cart = cartRepository.findByCustomer(user)
                .orElseThrow(() -> new ResourceNotFoundException("Panier non trouvé"));

        cart.setCoupon(null);
        return cartMapper.toDto(cartRepository.save(cart));
    }
}
