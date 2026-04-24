package com.shopflow.service;

import com.shopflow.dto.OrderDTO;
import com.shopflow.entity.Address;
import com.shopflow.entity.Cart;
import com.shopflow.entity.CartItem;
import com.shopflow.entity.Coupon;
import com.shopflow.entity.CouponType;
import com.shopflow.entity.Order;
import com.shopflow.entity.OrderItem;
import com.shopflow.entity.OrderStatus;
import com.shopflow.entity.Product;
import com.shopflow.entity.Role;
import com.shopflow.entity.SellerProfile;
import com.shopflow.entity.User;
import com.shopflow.exception.ResourceNotFoundException;
import com.shopflow.mapper.CartMapper;
import com.shopflow.mapper.OrderMapper;
import com.shopflow.repository.AddressRepository;
import com.shopflow.repository.CartRepository;
import com.shopflow.repository.CouponRepository;
import com.shopflow.repository.OrderRepository;
import com.shopflow.repository.SellerProfileRepository;
import com.shopflow.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private CartRepository cartRepository;
    @Mock private UserRepository userRepository;
    @Mock private CouponRepository couponRepository;
    @Mock private AddressRepository addressRepository;
    @Mock private SellerProfileRepository sellerProfileRepository;
    @Mock private OrderMapper orderMapper;
    @Mock private CartMapper cartMapper;

    @InjectMocks private OrderService orderService;

    private User customer;
    private User admin;
    private User seller;
    private SellerProfile sellerProfile;
    private Product product;
    private Address address;
    private Cart cart;
    private OrderDTO orderDto;

    @BeforeEach
    void setUp() {
        customer = User.builder().id(1L).email("c@shopflow.com").role(Role.CUSTOMER).actif(true).build();
        admin = User.builder().id(2L).email("a@shopflow.com").role(Role.ADMIN).actif(true).build();
        seller = User.builder().id(3L).email("s@shopflow.com").role(Role.SELLER).actif(true).build();
        sellerProfile = SellerProfile.builder().id(10L).user(seller).nomBoutique("Shop").build();
        product = Product.builder().id(100L).nom("PS5").prix(500.0)
                .stock(10).actif(true).seller(sellerProfile).build();
        address = Address.builder().id(50L).user(customer).rue("Rue").ville("Paris")
                .codePostal("75000").pays("France").principal(true).build();
        cart = Cart.builder().id(5L).customer(customer).lignes(new ArrayList<>()).build();
        orderDto = OrderDTO.builder().build();
    }

    private CartItem item(int qty) {
        CartItem ci = new CartItem();
        ci.setProduct(product);
        ci.setQuantite(qty);
        ci.setCart(cart);
        return ci;
    }

    @Test
    void createOrderFromCart_success() {
        cart.getLignes().add(item(2));
        when(userRepository.findByEmail("c@shopflow.com")).thenReturn(Optional.of(customer));
        when(cartRepository.findByCustomer(customer)).thenReturn(Optional.of(cart));
        when(addressRepository.findById(50L)).thenReturn(Optional.of(address));
        when(cartMapper.calculateSousTotal(cart)).thenReturn(1000.0);
        when(cartMapper.calculateRemise(cart)).thenReturn(0.0);
        when(cartMapper.calculateTotal(cart)).thenReturn(1000.0);
        when(cartMapper.calculatePrixUnitaire(any())).thenReturn(500.0);
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));
        when(orderMapper.toDto(any(Order.class))).thenReturn(orderDto);

        OrderDTO result = orderService.createOrderFromCart("c@shopflow.com", 50L);

        assertThat(result).isNotNull();
        assertThat(product.getStock()).isEqualTo(8);
        verify(cartRepository).save(cart);
        assertThat(cart.getLignes()).isEmpty();
    }

    @Test
    void createOrderFromCart_appliesCoupon() {
        cart.getLignes().add(item(1));
        Coupon coupon = Coupon.builder().id(7L).code("X").type(CouponType.PERCENT)
                .valeur(BigDecimal.TEN).actif(true).usagesActuels(0).build();
        cart.setCoupon(coupon);

        when(userRepository.findByEmail("c@shopflow.com")).thenReturn(Optional.of(customer));
        when(cartRepository.findByCustomer(customer)).thenReturn(Optional.of(cart));
        when(addressRepository.findById(50L)).thenReturn(Optional.of(address));
        when(cartMapper.calculateSousTotal(cart)).thenReturn(500.0);
        when(cartMapper.calculateRemise(cart)).thenReturn(50.0);
        when(cartMapper.calculateTotal(cart)).thenReturn(450.0);
        when(cartMapper.calculatePrixUnitaire(any())).thenReturn(500.0);
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));
        when(orderMapper.toDto(any(Order.class))).thenReturn(orderDto);

        orderService.createOrderFromCart("c@shopflow.com", 50L);

        assertThat(coupon.getUsagesActuels()).isEqualTo(1);
        verify(couponRepository).save(coupon);
    }

    @Test
    void createOrderFromCart_emptyCart_throws() {
        when(userRepository.findByEmail("c@shopflow.com")).thenReturn(Optional.of(customer));
        when(cartRepository.findByCustomer(customer)).thenReturn(Optional.of(cart));

        assertThatThrownBy(() -> orderService.createOrderFromCart("c@shopflow.com", 50L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("vide");
    }

    @Test
    void createOrderFromCart_stockInsufficient_throws() {
        product.setStock(1);
        cart.getLignes().add(item(5));
        when(userRepository.findByEmail("c@shopflow.com")).thenReturn(Optional.of(customer));
        when(cartRepository.findByCustomer(customer)).thenReturn(Optional.of(cart));
        when(addressRepository.findById(50L)).thenReturn(Optional.of(address));

        assertThatThrownBy(() -> orderService.createOrderFromCart("c@shopflow.com", 50L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Stock");
    }

    @Test
    void createOrderFromCart_addressNotOwned_throws() {
        cart.getLignes().add(item(1));
        address.setUser(admin);
        when(userRepository.findByEmail("c@shopflow.com")).thenReturn(Optional.of(customer));
        when(cartRepository.findByCustomer(customer)).thenReturn(Optional.of(cart));
        when(addressRepository.findById(50L)).thenReturn(Optional.of(address));

        assertThatThrownBy(() -> orderService.createOrderFromCart("c@shopflow.com", 50L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void getMyOrders_returnsMappedList() {
        Order o = new Order();
        when(userRepository.findByEmail("c@shopflow.com")).thenReturn(Optional.of(customer));
        when(orderRepository.findByCustomer(customer)).thenReturn(List.of(o));
        when(orderMapper.toDto(o)).thenReturn(orderDto);

        assertThat(orderService.getMyOrders("c@shopflow.com")).hasSize(1);
    }

    @Test
    void getOrderById_owner_succeeds() {
        Order order = buildOrder(OrderStatus.PENDING);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(userRepository.findByEmail("c@shopflow.com")).thenReturn(Optional.of(customer));
        when(orderMapper.toDto(order)).thenReturn(orderDto);

        assertThat(orderService.getOrderById("c@shopflow.com", 1L)).isNotNull();
    }

    @Test
    void getOrderById_admin_succeeds() {
        Order order = buildOrder(OrderStatus.PENDING);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(userRepository.findByEmail("a@shopflow.com")).thenReturn(Optional.of(admin));
        when(orderMapper.toDto(order)).thenReturn(orderDto);

        assertThat(orderService.getOrderById("a@shopflow.com", 1L)).isNotNull();
    }

    @Test
    void getOrderById_unauthorized_throws() {
        Order order = buildOrder(OrderStatus.PENDING);
        User stranger = User.builder().id(99L).email("x@shopflow.com").role(Role.CUSTOMER).build();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(userRepository.findByEmail("x@shopflow.com")).thenReturn(Optional.of(stranger));

        assertThatThrownBy(() -> orderService.getOrderById("x@shopflow.com", 1L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void cancelOrder_success_restoresStock() {
        Order order = buildOrder(OrderStatus.PENDING);
        product.setStock(5);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(userRepository.findByEmail("c@shopflow.com")).thenReturn(Optional.of(customer));
        when(orderRepository.save(order)).thenReturn(order);
        when(orderMapper.toDto(order)).thenReturn(orderDto);

        orderService.cancelOrder("c@shopflow.com", 1L);

        assertThat(order.getStatut()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(product.getStock()).isEqualTo(7);
    }

    @Test
    void cancelOrder_wrongStatus_throws() {
        Order order = buildOrder(OrderStatus.SHIPPED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(userRepository.findByEmail("c@shopflow.com")).thenReturn(Optional.of(customer));

        assertThatThrownBy(() -> orderService.cancelOrder("c@shopflow.com", 1L))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void updateStatus_admin_success() {
        Order order = buildOrder(OrderStatus.PENDING);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(userRepository.findByEmail("a@shopflow.com")).thenReturn(Optional.of(admin));
        when(orderRepository.save(order)).thenReturn(order);
        when(orderMapper.toDto(order)).thenReturn(orderDto);

        orderService.updateStatus("a@shopflow.com", 1L, OrderStatus.SHIPPED);

        assertThat(order.getStatut()).isEqualTo(OrderStatus.SHIPPED);
    }

    @Test
    void updateStatus_customer_forbidden() {
        Order order = buildOrder(OrderStatus.PENDING);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(userRepository.findByEmail("c@shopflow.com")).thenReturn(Optional.of(customer));

        assertThatThrownBy(() -> orderService.updateStatus("c@shopflow.com", 1L, OrderStatus.SHIPPED))
                .isInstanceOf(IllegalArgumentException.class);
        verify(orderRepository, never()).save(any());
    }

    @Test
    void getAllOrders_returnsList() {
        Order o = new Order();
        when(orderRepository.findAll()).thenReturn(List.of(o));
        when(orderMapper.toDto(o)).thenReturn(orderDto);

        assertThat(orderService.getAllOrders()).hasSize(1);
    }

    @Test
    void getSellerOrders_returnsList() {
        Order o = new Order();
        when(orderRepository.findBySellerProfileId(10L)).thenReturn(List.of(o));
        when(orderMapper.toDto(o)).thenReturn(orderDto);

        assertThat(orderService.getSellerOrders(10L)).hasSize(1);
    }

    @Test
    void getSalesForUser_admin_returnsAll() {
        when(userRepository.findByEmail("a@shopflow.com")).thenReturn(Optional.of(admin));
        when(orderRepository.findAllByOrderByDateCommandeDesc(any(Pageable.class)))
                .thenReturn(new org.springframework.data.domain.PageImpl<>(List.of(new Order())));
        when(orderMapper.toDto(any(Order.class))).thenReturn(orderDto);

        assertThat(orderService.getSalesForUser("a@shopflow.com")).hasSize(1);
    }

    @Test
    void getSalesForUser_seller_returnsSellerOrders() {
        when(userRepository.findByEmail("s@shopflow.com")).thenReturn(Optional.of(seller));
        when(sellerProfileRepository.findByUserId(3L)).thenReturn(Optional.of(sellerProfile));
        when(orderRepository.findBySellerProfileId(10L)).thenReturn(List.of(new Order()));
        when(orderMapper.toDto(any(Order.class))).thenReturn(orderDto);

        assertThat(orderService.getSalesForUser("s@shopflow.com")).hasSize(1);
    }

    @Test
    void getSalesForUser_customer_forbidden() {
        when(userRepository.findByEmail("c@shopflow.com")).thenReturn(Optional.of(customer));
        assertThatThrownBy(() -> orderService.getSalesForUser("c@shopflow.com"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void cancelOrder_notFound_throws() {
        when(orderRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> orderService.cancelOrder("c@shopflow.com", 999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private Order buildOrder(OrderStatus status) {
        OrderItem oi = new OrderItem();
        oi.setProduct(product);
        oi.setQuantite(2);
        oi.setPrixUnitaire(BigDecimal.valueOf(500));

        Order order = new Order();
        order.setId(1L);
        order.setNumeroCommande("ORD-2025-ABC");
        order.setCustomer(customer);
        order.setAdresseLivraison(address);
        order.setStatut(status);
        order.setLignes(new ArrayList<>(List.of(oi)));
        oi.setOrder(order);
        return order;
    }
}
