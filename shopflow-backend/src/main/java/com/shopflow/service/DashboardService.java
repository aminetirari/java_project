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
import com.shopflow.repository.SellerProfileRepository;
import com.shopflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final SellerProfileRepository sellerProfileRepository;
    private final UserRepository userRepository;
    private final OrderMapper orderMapper;
    private final ProductMapper productMapper;

    @Transactional(readOnly = true)
    public Map<String, Object> adminDashboard() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("revenusTotaux", orderRepository.totalRevenue());
        data.put("revenus30j", orderRepository.revenueSince(LocalDateTime.now().minusDays(30)));
        data.put("nbCommandes", orderRepository.count());
        data.put("nbCommandesEnCours", orderRepository.countByStatut(OrderStatus.PROCESSING)
                + orderRepository.countByStatut(OrderStatus.PAID) + orderRepository.countByStatut(OrderStatus.PAYE));
        data.put("nbUtilisateurs", userRepository.count());
        data.put("topProduits", productRepository.findTopSelling(PageRequest.of(0, 5)).stream().map(row -> {
            Map<String, Object> m = new HashMap<>();
            m.put("product", productMapper.toDto((com.shopflow.entity.Product) row[0]));
            m.put("quantiteVendue", row[1]);
            return m;
        }).collect(Collectors.toList()));
        data.put("commandesRecentes", orderRepository.findTop10ByOrderByDateCommandeDesc().stream()
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

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("revenus", orderRepository.revenueBySeller(seller.getId()));
        data.put("commandesEnAttente", orderRepository.countBySellerAndStatut(seller.getId(), OrderStatus.PENDING)
                + orderRepository.countBySellerAndStatut(seller.getId(), OrderStatus.PAID));
        data.put("alertesStock", productRepository.findTop10BySellerIdAndActifTrueAndStockLessThanEqualOrderByStockAsc(seller.getId(), 5)
                .stream().map(productMapper::toDto).collect(Collectors.toList()));
        data.put("mesProduits", productRepository.findBySellerIdAndActifTrue(seller.getId()).size());
        data.put("commandesRecentes", orderRepository.findBySellerProfileId(seller.getId()).stream()
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
}
