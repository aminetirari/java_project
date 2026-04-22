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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private static final Set<OrderStatus> CANCELLABLE = EnumSet.of(OrderStatus.PENDING, OrderStatus.PAID, OrderStatus.PAYE);

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

        // Vérification finale du stock et décrémentation
        for (CartItem item : cart.getLignes()) {
            Product product = item.getProduct();
            if (product.getStock() == null || product.getStock() < item.getQuantite()) {
                throw new IllegalStateException("Stock insuffisant pour le produit " + product.getNom());
            }
            product.setStock(product.getStock() - item.getQuantite());
        }

        Order order = new Order();
        order.setNumeroCommande(generateOrderNumber());
        order.setCustomer(user);
        order.setAdresseLivraison(address);
        order.setDateCommande(LocalDateTime.now());
        order.setStatut(OrderStatus.PENDING);

        Double calcSousTotal = cartMapper.calculateSousTotal(cart);
        Double calcRemise = cartMapper.calculateRemise(cart);
        Double calcTotal = cartMapper.calculateTotal(cart);

        order.setSousTotal(BigDecimal.valueOf(calcSousTotal));
        order.setMontantRemise(BigDecimal.valueOf(calcRemise));

        BigDecimal fraisLivraison = calcTotal >= 50.0 ? BigDecimal.ZERO : BigDecimal.valueOf(4.99);
        order.setFraisLivraison(fraisLivraison);
        order.setTotalTTC(BigDecimal.valueOf(calcTotal).add(fraisLivraison));

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

    @Transactional(readOnly = true)
    public List<OrderDTO> getMyOrders(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));
        return orderRepository.findByCustomer(user).stream()
                .map(orderMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public OrderDTO getOrderById(String email, Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Commande introuvable"));
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        boolean isOwner = order.getCustomer().getId().equals(user.getId());
        boolean isAdmin = user.getRole() == Role.ADMIN;
        boolean isSellerOfOrder = user.getRole() == Role.SELLER &&
                order.getLignes().stream().anyMatch(l -> l.getProduct().getSeller().getUser().getId().equals(user.getId()));

        if (!isOwner && !isAdmin && !isSellerOfOrder) {
            throw new IllegalArgumentException("Accès non autorisé à cette commande");
        }
        return orderMapper.toDto(order);
    }

    @Transactional
    public OrderDTO cancelOrder(String email, Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Commande introuvable"));
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        boolean isOwner = order.getCustomer().getId().equals(user.getId());
        boolean isAdmin = user.getRole() == Role.ADMIN;
        if (!isOwner && !isAdmin) {
            throw new IllegalArgumentException("Action non autorisée");
        }
        if (!CANCELLABLE.contains(order.getStatut())) {
            throw new IllegalStateException("Cette commande ne peut plus être annulée");
        }

        // Remboursement simulé: restauration des stocks
        order.getLignes().forEach(l -> {
            Product p = l.getProduct();
            p.setStock((p.getStock() == null ? 0 : p.getStock()) + l.getQuantite());
        });
        order.setStatut(OrderStatus.CANCELLED);
        return orderMapper.toDto(orderRepository.save(order));
    }

    @Transactional
    public OrderDTO updateStatus(String email, Long id, OrderStatus status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Commande introuvable"));
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        boolean isAdmin = user.getRole() == Role.ADMIN;
        boolean isSellerOfOrder = user.getRole() == Role.SELLER &&
                order.getLignes().stream().anyMatch(l -> l.getProduct().getSeller().getUser().getId().equals(user.getId()));
        if (!isAdmin && !isSellerOfOrder) {
            throw new IllegalArgumentException("Action non autorisée");
        }
        order.setStatut(status);
        return orderMapper.toDto(orderRepository.save(order));
    }

    @Transactional(readOnly = true)
    public List<OrderDTO> getAllOrders() {
        return orderRepository.findAll().stream().map(orderMapper::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<OrderDTO> getSellerOrders(Long sellerProfileId) {
        return orderRepository.findBySellerProfileId(sellerProfileId).stream()
                .map(orderMapper::toDto).collect(Collectors.toList());
    }

    private String generateOrderNumber() {
        return String.format("ORD-%d-%s", Year.now().getValue(),
                UUID.randomUUID().toString().substring(0, 5).toUpperCase());
    }
}
