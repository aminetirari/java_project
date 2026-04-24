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

    @Query("SELECT COALESCE(SUM(l.prixUnitaire * l.quantite), 0) FROM OrderItem l WHERE l.product.seller.id = :sellerId AND l.order.statut IN ('PAID','PAYE','PROCESSING','SHIPPED','DELIVERED') AND l.order.dateCommande >= :since")
    java.math.BigDecimal revenueBySellerSince(Long sellerId, java.time.LocalDateTime since);

    @Query("SELECT o.dateCommande, o.totalTTC FROM Order o " +
            "WHERE o.statut IN ('PAID','PAYE','PROCESSING','SHIPPED','DELIVERED') AND o.dateCommande >= :since")
    List<Object[]> revenueRowsSince(java.time.LocalDateTime since);

    @Query("SELECT l.order.dateCommande, l.prixUnitaire, l.quantite FROM OrderItem l " +
            "WHERE l.product.seller.id = :sellerId AND l.order.statut IN ('PAID','PAYE','PROCESSING','SHIPPED','DELIVERED') AND l.order.dateCommande >= :since")
    List<Object[]> revenueRowsForSellerSince(Long sellerId, java.time.LocalDateTime since);

    @Query("SELECT s.id, s.nomBoutique, COALESCE(SUM(l.prixUnitaire * l.quantite), 0) AS revenue, COUNT(DISTINCT l.order.id) AS nbOrders " +
            "FROM OrderItem l JOIN l.product.seller s " +
            "WHERE l.order.statut IN ('PAID','PAYE','PROCESSING','SHIPPED','DELIVERED') " +
            "GROUP BY s.id, s.nomBoutique ORDER BY revenue DESC")
    List<Object[]> topSellers(org.springframework.data.domain.Pageable pageable);

    @Query("SELECT c.nom, COALESCE(SUM(l.prixUnitaire * l.quantite), 0) AS revenue FROM OrderItem l " +
            "JOIN l.product p JOIN p.categories c " +
            "WHERE l.order.statut IN ('PAID','PAYE','PROCESSING','SHIPPED','DELIVERED') " +
            "GROUP BY c.id, c.nom ORDER BY revenue DESC")
    List<Object[]> revenueByCategory();

    @Query("SELECT COALESCE(AVG(o.totalTTC), 0) FROM Order o WHERE o.statut IN ('PAID','PAYE','PROCESSING','SHIPPED','DELIVERED')")
    java.math.BigDecimal averageOrderValue();

    @Query("SELECT COALESCE(AVG(l.prixUnitaire * l.quantite), 0) FROM OrderItem l WHERE l.product.seller.id = :sellerId AND l.order.statut IN ('PAID','PAYE','PROCESSING','SHIPPED','DELIVERED')")
    java.math.BigDecimal averageOrderValueBySeller(Long sellerId);

    @Query("SELECT p.id, p.nom, COALESCE(SUM(l.quantite), 0) AS sold, COALESCE(SUM(l.prixUnitaire * l.quantite), 0) AS revenue FROM OrderItem l JOIN l.product p " +
            "WHERE p.seller.id = :sellerId AND l.order.statut IN ('PAID','PAYE','PROCESSING','SHIPPED','DELIVERED') " +
            "GROUP BY p.id, p.nom ORDER BY sold DESC")
    List<Object[]> topProductsForSeller(Long sellerId, org.springframework.data.domain.Pageable pageable);
}
