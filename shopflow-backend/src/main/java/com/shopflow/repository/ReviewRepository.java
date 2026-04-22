package com.shopflow.repository;

import com.shopflow.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

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
}
