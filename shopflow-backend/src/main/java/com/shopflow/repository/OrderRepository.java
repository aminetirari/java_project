package com.shopflow.repository;

import com.shopflow.entity.Order;
import com.shopflow.entity.OrderStatus;
import com.shopflow.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByCustomer(User customer);

    Page<Order> findByCustomer(User customer, Pageable pageable);

    Page<Order> findAllByOrderByDateCommandeDesc(Pageable pageable);

    List<Order> findTop10ByOrderByDateCommandeDesc();

    long countByStatut(OrderStatus statut);

    @Query("SELECT COALESCE(SUM(o.totalTTC), 0) FROM Order o WHERE o.statut IN ('PAID','PAYE','PROCESSING','SHIPPED','DELIVERED')")
    java.math.BigDecimal totalRevenue();

    @Query("SELECT COALESCE(SUM(o.totalTTC), 0) FROM Order o WHERE o.statut IN ('PAID','PAYE','PROCESSING','SHIPPED','DELIVERED') AND o.dateCommande >= :since")
    java.math.BigDecimal revenueSince(java.time.LocalDateTime since);

    @Query("SELECT DISTINCT o FROM Order o JOIN o.lignes l WHERE l.product.seller.id = :sellerId ORDER BY o.dateCommande DESC")
    List<Order> findBySellerProfileId(Long sellerId);

    @Query("SELECT COUNT(DISTINCT o) FROM Order o JOIN o.lignes l WHERE l.product.seller.id = :sellerId AND o.statut = :statut")
    long countBySellerAndStatut(Long sellerId, OrderStatus statut);

    @Query("SELECT COALESCE(SUM(l.prixUnitaire * l.quantite), 0) FROM OrderItem l WHERE l.product.seller.id = :sellerId AND l.order.statut IN ('PAID','PAYE','PROCESSING','SHIPPED','DELIVERED')")
    java.math.BigDecimal revenueBySeller(Long sellerId);
}
