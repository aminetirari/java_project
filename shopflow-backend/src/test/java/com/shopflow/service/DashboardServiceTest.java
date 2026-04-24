package com.shopflow.service;

import com.shopflow.dto.OrderDTO;
import com.shopflow.dto.ProductDTO;
import com.shopflow.entity.Order;
import com.shopflow.entity.OrderStatus;
import com.shopflow.entity.Product;
import com.shopflow.entity.Role;
import com.shopflow.entity.SellerProfile;
import com.shopflow.entity.User;
import com.shopflow.exception.ResourceNotFoundException;
import com.shopflow.mapper.OrderMapper;
import com.shopflow.mapper.ProductMapper;
import com.shopflow.repository.OrderRepository;
import com.shopflow.repository.ProductRepository;
import com.shopflow.repository.ReviewRepository;
import com.shopflow.repository.SellerProfileRepository;
import com.shopflow.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DashboardServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private ProductRepository productRepository;
    @Mock private SellerProfileRepository sellerProfileRepository;
    @Mock private UserRepository userRepository;
    @Mock private ReviewRepository reviewRepository;
    @Mock private OrderMapper orderMapper;
    @Mock private ProductMapper productMapper;

    @InjectMocks private DashboardService dashboardService;

    private User admin;
    private User seller;
    private User customer;
    private SellerProfile sellerProfile;

    @BeforeEach
    void setUp() {
        admin = User.builder().id(1L).email("a@shopflow.com").role(Role.ADMIN).actif(true).build();
        seller = User.builder().id(2L).email("s@shopflow.com").role(Role.SELLER).actif(true).build();
        customer = User.builder().id(3L).email("c@shopflow.com").role(Role.CUSTOMER).actif(true).build();
        sellerProfile = SellerProfile.builder().id(10L).user(seller).build();
    }

    @Test
    void adminDashboard_returnsAllKpis() {
        when(orderRepository.totalRevenue()).thenReturn(new BigDecimal("1000"));
        when(orderRepository.revenueSince(any())).thenReturn(new BigDecimal("500"));
        when(orderRepository.averageOrderValue()).thenReturn(new BigDecimal("50"));
        when(orderRepository.countByStatut(any(OrderStatus.class))).thenReturn(1L);
        when(orderRepository.count()).thenReturn(10L);
        when(userRepository.count()).thenReturn(5L);
        when(userRepository.countByActifFalse()).thenReturn(1L);
        when(userRepository.countByRole(any(Role.class))).thenReturn(2L);
        when(productRepository.count()).thenReturn(7L);
        when(productRepository.countByActifTrueAndStockLessThanEqual(anyInt())).thenReturn(3L);
        when(reviewRepository.countByApprouveFalse()).thenReturn(4L);

        List<Object[]> series = new ArrayList<>();
        series.add(new Object[]{Date.valueOf(LocalDate.now()), new BigDecimal("120")});
        when(orderRepository.revenueRowsSince(any())).thenReturn(series);

        List<Object[]> cats = new ArrayList<>();
        cats.add(new Object[]{"Tech", new BigDecimal("500")});
        when(orderRepository.revenueByCategory()).thenReturn(cats);

        List<Object[]> topSellers = new ArrayList<>();
        topSellers.add(new Object[]{10L, "Shop", new BigDecimal("300"), 5L});
        when(orderRepository.topSellers(any(Pageable.class))).thenReturn(topSellers);

        List<Object[]> topProd = new ArrayList<>();
        topProd.add(new Object[]{Product.builder().id(1L).nom("P").prix(1.0).stock(1).build(), 10L});
        when(productRepository.findTopSelling(any(Pageable.class))).thenReturn(topProd);
        when(productMapper.toDto(any(Product.class))).thenReturn(ProductDTO.builder().build());

        when(orderRepository.findTop10ByOrderByDateCommandeDesc()).thenReturn(List.of(new Order()));
        when(orderMapper.toDto(any(Order.class))).thenReturn(OrderDTO.builder().build());

        Map<String, Object> result = dashboardService.adminDashboard();

        assertThat(result)
                .containsKeys("revenusTotaux", "revenus30j", "variation30j", "nbCommandes",
                        "nbUtilisateurs", "nbProduits", "ventesParJour", "repartitionCategories",
                        "topVendeurs", "topProduits", "dernieresCommandes");
        assertThat((BigDecimal) result.get("revenusTotaux")).isEqualByComparingTo("1000");
    }

    @Test
    void sellerDashboard_success() {
        when(userRepository.findByEmail("s@shopflow.com")).thenReturn(Optional.of(seller));
        when(sellerProfileRepository.findByUser(seller)).thenReturn(Optional.of(sellerProfile));
        when(orderRepository.findBySellerProfileId(10L)).thenReturn(List.of(new Order()));
        when(orderRepository.revenueBySeller(10L)).thenReturn(new BigDecimal("200"));
        when(orderRepository.revenueBySellerSince(anyLong(), any())).thenReturn(new BigDecimal("100"));
        when(orderRepository.averageOrderValueBySeller(10L)).thenReturn(new BigDecimal("50"));
        when(orderRepository.countBySellerAndStatut(anyLong(), any(OrderStatus.class))).thenReturn(1L);
        when(productRepository.findBySellerIdAndActifTrue(10L))
                .thenReturn(List.of(Product.builder().id(1L).nom("P").prix(1.0).stock(1).build()));
        when(productRepository.countLowStockBySeller(anyLong(), anyInt())).thenReturn(2L);
        when(reviewRepository.averageNoteForSeller(10L)).thenReturn(4.27);
        when(reviewRepository.countByProductSellerIdAndApprouveTrue(10L)).thenReturn(5L);

        List<Object[]> series = new ArrayList<>();
        series.add(new Object[]{LocalDate.now(), new BigDecimal("10"), new BigDecimal("2")});
        when(orderRepository.revenueRowsForSellerSince(anyLong(), any())).thenReturn(series);

        List<Object[]> topProd = new ArrayList<>();
        topProd.add(new Object[]{1L, "P", 10L, new BigDecimal("50")});
        when(orderRepository.topProductsForSeller(anyLong(), any(Pageable.class))).thenReturn(topProd);

        when(productRepository.findTop10BySellerIdAndActifTrueAndStockLessThanEqualOrderByStockAsc(anyLong(), anyInt()))
                .thenReturn(List.of(Product.builder().id(2L).nom("Q").prix(1.0).stock(1).build()));
        when(productMapper.toDto(any(Product.class))).thenReturn(ProductDTO.builder().build());
        when(orderMapper.toDto(any(Order.class))).thenReturn(OrderDTO.builder().build());

        Map<String, Object> result = dashboardService.sellerDashboard("s@shopflow.com");

        assertThat(result)
                .containsKeys("revenus", "nbCommandes", "nbProduits", "noteMoyenne",
                        "ventesParJour", "topProduits", "produitsEnRupture", "commandesRecentes");
        assertThat((Double) result.get("noteMoyenne")).isEqualTo(4.3);
    }

    @Test
    void sellerDashboard_customer_forbidden() {
        when(userRepository.findByEmail("c@shopflow.com")).thenReturn(Optional.of(customer));
        assertThatThrownBy(() -> dashboardService.sellerDashboard("c@shopflow.com"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void sellerDashboard_noProfile_throws() {
        when(userRepository.findByEmail("s@shopflow.com")).thenReturn(Optional.of(seller));
        when(sellerProfileRepository.findByUser(seller)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> dashboardService.sellerDashboard("s@shopflow.com"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void customerDashboard_returnsData() {
        when(userRepository.findByEmail("c@shopflow.com")).thenReturn(Optional.of(customer));
        when(orderRepository.findByCustomer(customer)).thenReturn(List.of(new Order(), new Order()));
        when(orderMapper.toDto(any(Order.class))).thenReturn(OrderDTO.builder().build());

        Map<String, Object> result = dashboardService.customerDashboard("c@shopflow.com");

        assertThat(result.get("mesCommandes")).isEqualTo(2);
        assertThat((List<?>) result.get("commandesRecentes")).hasSize(2);
    }

    @Test
    void customerDashboard_userNotFound_throws() {
        when(userRepository.findByEmail("x@shopflow.com")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> dashboardService.customerDashboard("x@shopflow.com"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private static int anyInt() {
        return org.mockito.ArgumentMatchers.anyInt();
    }
}
