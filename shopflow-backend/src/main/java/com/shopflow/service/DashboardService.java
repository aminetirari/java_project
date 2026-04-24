package com.shopflow.service;

import com.shopflow.entity.OrderStatus;
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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private static final int LOW_STOCK_THRESHOLD = 5;
    private static final int TIME_SERIES_DAYS = 30;

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final SellerProfileRepository sellerProfileRepository;
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final OrderMapper orderMapper;
    private final ProductMapper productMapper;

    @Transactional(readOnly = true)
    public Map<String, Object> adminDashboard() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime since30 = now.minusDays(TIME_SERIES_DAYS);
        LocalDateTime since60 = now.minusDays(TIME_SERIES_DAYS * 2L);

        BigDecimal revenusTotaux = orderRepository.totalRevenue();
        BigDecimal revenus30j = orderRepository.revenueSince(since30);
        BigDecimal revenus30jPrev = orderRepository.revenueSince(since60).subtract(revenus30j);
        BigDecimal panierMoyen = orderRepository.averageOrderValue();

        long nbEnCours = orderRepository.countByStatut(OrderStatus.PROCESSING)
                + orderRepository.countByStatut(OrderStatus.PAID)
                + orderRepository.countByStatut(OrderStatus.PAYE)
                + orderRepository.countByStatut(OrderStatus.PENDING);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("revenusTotaux", revenusTotaux);
        data.put("revenus30j", revenus30j);
        data.put("revenus30jPrev", revenus30jPrev);
        data.put("variation30j", percentVariation(revenus30j, revenus30jPrev));
        data.put("panierMoyen", panierMoyen);
        data.put("nbCommandes", orderRepository.count());
        data.put("nbCommandesEnCours", nbEnCours);
        data.put("nbUtilisateurs", userRepository.count());
        data.put("nbUtilisateursDesactives", userRepository.countByActifFalse());
        data.put("nbClients", userRepository.countByRole(Role.CUSTOMER));
        data.put("nbVendeurs", userRepository.countByRole(Role.SELLER));
        data.put("nbProduits", productRepository.count());
        data.put("nbProduitsStockFaible", productRepository.countByActifTrueAndStockLessThanEqual(LOW_STOCK_THRESHOLD));
        data.put("avisEnAttente", reviewRepository.countByApprouveFalse());

        data.put("ventesParJour", buildAdminSeries(orderRepository.revenueRowsSince(since30), since30.toLocalDate(), TIME_SERIES_DAYS));

        List<Map<String, Object>> categories = orderRepository.revenueByCategory().stream().map(row -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("nom", row[0]);
            m.put("revenus", row[1]);
            return m;
        }).collect(Collectors.toList());
        data.put("repartitionCategories", categories);

        List<Map<String, Object>> topSellers = orderRepository.topSellers(PageRequest.of(0, 5)).stream().map(row -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", row[0]);
            m.put("nom", row[1]);
            m.put("revenus", row[2]);
            m.put("nbCommandes", row[3]);
            return m;
        }).collect(Collectors.toList());
        data.put("topVendeurs", topSellers);

        data.put("topProduits", productRepository.findTopSelling(PageRequest.of(0, 5)).stream()
                .map(row -> productMapper.toDto((com.shopflow.entity.Product) row[0]))
                .collect(Collectors.toList()));
        data.put("dernieresCommandes", orderRepository.findTop10ByOrderByDateCommandeDesc().stream()
                .map(orderMapper::toDto).collect(Collectors.toList()));
        return data;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> sellerDashboard(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));
        if (user.getRole() != Role.SELLER && user.getRole() != Role.ADMIN) {
            throw new IllegalArgumentException("Accès réservé aux vendeurs");
        }
        SellerProfile seller = sellerProfileRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Profil vendeur introuvable. Contactez l'admin pour activer votre boutique."));

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime since30 = now.minusDays(TIME_SERIES_DAYS);

        List<com.shopflow.entity.Order> sellerOrders = orderRepository.findBySellerProfileId(seller.getId());
        BigDecimal revenus = orderRepository.revenueBySeller(seller.getId());
        BigDecimal revenus30j = orderRepository.revenueBySellerSince(seller.getId(), since30);
        BigDecimal panierMoyen = orderRepository.averageOrderValueBySeller(seller.getId());

        long commandesEnAttente = orderRepository.countBySellerAndStatut(seller.getId(), OrderStatus.PENDING)
                + orderRepository.countBySellerAndStatut(seller.getId(), OrderStatus.PAID)
                + orderRepository.countBySellerAndStatut(seller.getId(), OrderStatus.PAYE);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("revenus", revenus);
        data.put("revenus30j", revenus30j);
        data.put("panierMoyen", panierMoyen);
        data.put("nbCommandes", sellerOrders.size());
        data.put("commandesEnAttente", commandesEnAttente);
        data.put("nbProduits", productRepository.findBySellerIdAndActifTrue(seller.getId()).size());
        data.put("nbProduitsStockFaible", productRepository.countLowStockBySeller(seller.getId(), LOW_STOCK_THRESHOLD));
        data.put("noteMoyenne", round1(reviewRepository.averageNoteForSeller(seller.getId())));
        data.put("nbAvis", reviewRepository.countByProductSellerIdAndApprouveTrue(seller.getId()));

        data.put("ventesParJour", buildSellerSeries(
                orderRepository.revenueRowsForSellerSince(seller.getId(), since30),
                since30.toLocalDate(), TIME_SERIES_DAYS));

        List<Map<String, Object>> topProducts = orderRepository.topProductsForSeller(seller.getId(), PageRequest.of(0, 5)).stream().map(row -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", row[0]);
            m.put("nom", row[1]);
            m.put("quantiteVendue", row[2]);
            m.put("revenus", row[3]);
            return m;
        }).collect(Collectors.toList());
        data.put("topProduits", topProducts);

        data.put("produitsEnRupture", productRepository.findTop10BySellerIdAndActifTrueAndStockLessThanEqualOrderByStockAsc(seller.getId(), LOW_STOCK_THRESHOLD)
                .stream().map(productMapper::toDto).collect(Collectors.toList()));
        data.put("commandesRecentes", sellerOrders.stream()
                .limit(10).map(orderMapper::toDto).collect(Collectors.toList()));
        return data;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> customerDashboard(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("mesCommandes", orderRepository.findByCustomer(user).size());
        data.put("commandesRecentes", orderRepository.findByCustomer(user).stream()
                .limit(10).map(orderMapper::toDto).collect(Collectors.toList()));
        return data;
    }

    private List<Map<String, Object>> buildAdminSeries(List<Object[]> rows, LocalDate start, int days) {
        TreeMap<LocalDate, BigDecimal> lookup = new TreeMap<>();
        for (Object[] row : rows) {
            LocalDate day = toLocalDate(row[0]);
            BigDecimal amount = toBigDecimal(row[1]);
            lookup.merge(day, amount, BigDecimal::add);
        }
        return materializeSeries(lookup, start, days);
    }

    private List<Map<String, Object>> buildSellerSeries(List<Object[]> rows, LocalDate start, int days) {
        TreeMap<LocalDate, BigDecimal> lookup = new TreeMap<>();
        for (Object[] row : rows) {
            LocalDate day = toLocalDate(row[0]);
            BigDecimal unit = toBigDecimal(row[1]);
            BigDecimal qty = toBigDecimal(row[2]);
            lookup.merge(day, unit.multiply(qty), BigDecimal::add);
        }
        return materializeSeries(lookup, start, days);
    }

    private List<Map<String, Object>> materializeSeries(TreeMap<LocalDate, BigDecimal> lookup, LocalDate start, int days) {
        List<Map<String, Object>> series = new ArrayList<>(days);
        for (int i = 0; i < days; i++) {
            LocalDate d = start.plusDays(i);
            Map<String, Object> point = new LinkedHashMap<>();
            point.put("date", d.toString());
            point.put("total", lookup.getOrDefault(d, BigDecimal.ZERO));
            series.add(point);
        }
        return series;
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) return BigDecimal.ZERO;
        if (value instanceof BigDecimal) return (BigDecimal) value;
        if (value instanceof Number) return BigDecimal.valueOf(((Number) value).doubleValue());
        return new BigDecimal(value.toString());
    }

    private LocalDate toLocalDate(Object value) {
        if (value instanceof LocalDate) return (LocalDate) value;
        if (value instanceof LocalDateTime) return ((LocalDateTime) value).toLocalDate();
        if (value instanceof Date) return ((Date) value).toLocalDate();
        if (value instanceof java.util.Date) return new Date(((java.util.Date) value).getTime()).toLocalDate();
        return LocalDate.parse(value.toString());
    }

    private BigDecimal percentVariation(BigDecimal current, BigDecimal previous) {
        if (previous == null || previous.compareTo(BigDecimal.ZERO) == 0) {
            return current != null && current.compareTo(BigDecimal.ZERO) > 0
                    ? new BigDecimal("100.0") : BigDecimal.ZERO;
        }
        return current.subtract(previous)
                .multiply(new BigDecimal(100))
                .divide(previous, 1, RoundingMode.HALF_UP);
    }

    private Double round1(Double value) {
        if (value == null) return 0.0;
        return Math.round(value * 10.0) / 10.0;
    }
}
