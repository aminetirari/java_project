package com.shopflow.repository;

import com.shopflow.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    Page<Product> findByActifTrue(Pageable pageable);

    Page<Product> findBySellerIdAndActifTrue(Long sellerId, Pageable pageable);

    List<Product> findBySellerIdAndActifTrue(Long sellerId);

    List<Product> findBySellerId(Long sellerId);

    @Query("SELECT p FROM Product p WHERE p.actif = true AND " +
            "(LOWER(p.nom) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
            "LOWER(COALESCE(p.description, '')) LIKE LOWER(CONCAT('%', :q, '%')))")
    Page<Product> searchByText(@Param("q") String q, Pageable pageable);

    @Query("SELECT p, COALESCE(SUM(oi.quantite), 0) AS sold FROM Product p " +
            "LEFT JOIN OrderItem oi ON oi.product = p " +
            "WHERE p.actif = true " +
            "GROUP BY p ORDER BY sold DESC")
    List<Object[]> findTopSelling(Pageable pageable);

    @Query("SELECT COUNT(p) FROM Product p WHERE p.seller.id = :sellerId AND p.actif = true AND p.stock <= :threshold")
    long countLowStockBySeller(@Param("sellerId") Long sellerId, @Param("threshold") Integer threshold);

    List<Product> findTop10BySellerIdAndActifTrueAndStockLessThanEqualOrderByStockAsc(Long sellerId, Integer threshold);

    long countByActifTrueAndStockLessThanEqual(Integer threshold);
}
