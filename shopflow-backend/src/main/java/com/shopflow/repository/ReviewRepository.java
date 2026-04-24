package com.shopflow.repository;

import com.shopflow.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByProductIdAndApprouveTrue(Long productId);

    List<Review> findByProductId(Long productId);

    List<Review> findByApprouveFalse();

    List<Review> findByCustomerId(Long customerId);

    boolean existsByCustomerIdAndProductId(Long customerId, Long productId);

    @Query("SELECT COALESCE(AVG(r.note), 0) FROM Review r WHERE r.product.id = :productId AND r.approuve = true")
    Double averageNoteForProduct(Long productId);

    long countByApprouveFalse();

    @Query("SELECT COALESCE(AVG(r.note), 0) FROM Review r WHERE r.product.seller.id = :sellerId AND r.approuve = true")
    Double averageNoteForSeller(Long sellerId);

    long countByProductSellerIdAndApprouveTrue(Long sellerId);

    @Query("SELECT r.product.id, AVG(r.note), COUNT(r) FROM Review r " +
            "WHERE r.product.id IN :ids AND r.approuve = true " +
            "GROUP BY r.product.id")
    List<Object[]> findAggregatesByProductIds(@Param("ids") Collection<Long> ids);
}
