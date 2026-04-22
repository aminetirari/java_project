package com.shopflow.service;

import com.shopflow.dto.OrderDTO;
import com.shopflow.entity.*;
import com.shopflow.exception.ResourceNotFoundException;
import com.shopflow.mapper.CartMapper;
import com.shopflow.mapper.OrderMapper;
import com.shopflow.repository.AddressRepository;
import com.shopflow.repository.CartRepository;
import com.shopflow.repository.CouponRepository;
import com.shopflow.repository.OrderRepository;
import com.shopflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import com.stripe.model.PaymentIntent;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final CouponRepository couponRepository;
    private final AddressRepository addressRepository;
    private final OrderMapper orderMapper;
    private final CartMapper cartMapper;

    @Transactional
    public OrderDTO createOrderFromCart(String email, Long addressId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        Cart cart = cartRepository.findByCustomer(user)
                .orElseThrow(() -> new ResourceNotFoundException("Panier non trouvé"));

        if (cart.getLignes() == null || cart.getLignes().isEmpty()) {
            throw new IllegalStateException("Le panier est vide");
        }

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Adresse non trouvée"));

        if (!address.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Adresse n'appartient pas à l'utilisateur");
        }

        Order order = new Order();
        order.setNumeroCommande("CMD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        order.setCustomer(user);
        order.setAdresseLivraison(address);
        order.setDateCommande(LocalDateTime.now());
        order.setStatut(OrderStatus.PENDING);
        
        Double calcSousTotal = cartMapper.calculateSousTotal(cart);
        Double calcRemise = cartMapper.calculateRemise(cart);
        Double calcTotal = cartMapper.calculateTotal(cart);
        
        order.setSousTotal(BigDecimal.valueOf(calcSousTotal));
        order.setMontantRemise(BigDecimal.valueOf(calcRemise));
        
        order.setFraisLivraison(BigDecimal.ZERO);
        order.setTotalTTC(BigDecimal.valueOf(calcTotal).add(order.getFraisLivraison()));

        if (cart.getCoupon() != null) {
            Coupon coupon = cart.getCoupon();
            order.setCoupon(coupon);
            coupon.setUsagesActuels(coupon.getUsagesActuels() + 1);
            couponRepository.save(coupon);
        }

        List<OrderItem> orderItems = cart.getLignes().stream().map(cartItem -> {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setVariant(cartItem.getVariant());
            orderItem.setQuantite(cartItem.getQuantite());
            
            Double pu = cartMapper.calculatePrixUnitaire(cartItem);
            orderItem.setPrixUnitaire(BigDecimal.valueOf(pu));
            orderItem.setSousTotal(BigDecimal.valueOf(pu * cartItem.getQuantite()));
            
            return orderItem;
        }).collect(Collectors.toList());

        order.setLignes(orderItems);

        Order savedOrder = orderRepository.save(order);

        cart.getLignes().clear();
        cart.setCoupon(null);
        cartRepository.save(cart);

        return orderMapper.toDto(savedOrder);
    }
    
    public List<OrderDTO> getMyOrders(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));
        return orderRepository.findByCustomer(user).stream()
                .map(orderMapper::toDto)
                .collect(Collectors.toList());
    }
}
