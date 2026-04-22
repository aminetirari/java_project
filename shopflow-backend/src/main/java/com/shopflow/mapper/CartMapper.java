package com.shopflow.mapper;

import com.shopflow.dto.CartDTO;
import com.shopflow.dto.CartItemDTO;
import com.shopflow.entity.Cart;
import com.shopflow.entity.CartItem;
import com.shopflow.entity.CouponType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CartMapper {

    @Mapping(source = "customer.id", target = "customerId")
    @Mapping(source = "coupon.code", target = "codePromo")
    @Mapping(target = "sousTotal", expression = "java(calculateSousTotal(cart))")
    @Mapping(target = "remise", expression = "java(calculateRemise(cart))")
    @Mapping(target = "totalCart", expression = "java(calculateTotal(cart))")
    CartDTO toDto(Cart cart);

    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "variant.id", target = "variantId")
    @Mapping(source = "product.nom", target = "productNom")
    @Mapping(target = "prixUnitaire", expression = "java(calculatePrixUnitaire(item))")
    @Mapping(target = "sousTotal", expression = "java(calculatePrixUnitaire(item) * item.getQuantite())")
    CartItemDTO toItemDto(CartItem item);

    @Named("calculateSousTotal")
    default Double calculateSousTotal(Cart cart) {
        if (cart == null || cart.getLignes() == null) return 0.0;
        return cart.getLignes().stream()
                .mapToDouble(item -> calculatePrixUnitaire(item) * item.getQuantite())
                .sum();
    }

    @Named("calculateRemise")
    default Double calculateRemise(Cart cart) {
        if (cart == null || cart.getCoupon() == null) return 0.0;
        Double sousTotal = calculateSousTotal(cart);
        if (cart.getCoupon().getType() == CouponType.PERCENT) {
            return sousTotal * (cart.getCoupon().getValeur().doubleValue() / 100.0);
        } else {
            return cart.getCoupon().getValeur().doubleValue();
        }
    }

    @Named("calculateTotal")
    default Double calculateTotal(Cart cart) {
        Double total = calculateSousTotal(cart) - calculateRemise(cart);
        return Math.max(0.0, total); // Pas de total négatif
    }

    @Named("calculatePrixUnitaire")
    default Double calculatePrixUnitaire(CartItem item) {
        if (item == null || item.getProduct() == null) return 0.0;
        double basePrix = item.getProduct().getPrixPromo() != null ? item.getProduct().getPrixPromo() : item.getProduct().getPrix();
        if (item.getVariant() != null && item.getVariant().getPrixDelta() != null) {
            basePrix += item.getVariant().getPrixDelta();
        }
        return basePrix;
    }
}
