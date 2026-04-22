package com.shopflow.repository;

import com.shopflow.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByProductIdAndApprouveTrue(Long productId);
    boolean existsByCustomerIdAndProductId(Long customerId, Long productId);
}
