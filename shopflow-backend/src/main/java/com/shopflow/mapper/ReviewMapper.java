package com.shopflow.mapper;

import com.shopflow.dto.ReviewDTO;
import com.shopflow.entity.Review;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ReviewMapper {

    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productNom", source = "product.nom")
    @Mapping(target = "customerName", expression = "java(review.getCustomer().getPrenom() + ' ' + review.getCustomer().getNom())")
    ReviewDTO toDto(Review review);

    List<ReviewDTO> toDtoList(List<Review> reviews);
}
