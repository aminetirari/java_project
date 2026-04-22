package com.shopflow.mapper;

import com.shopflow.dto.CouponCreateDTO;
import com.shopflow.dto.CouponDTO;
import com.shopflow.entity.Coupon;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CouponMapper {

    CouponDTO toDto(Coupon entity);

    List<CouponDTO> toDtoList(List<Coupon> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "actif", constant = "true")
    @Mapping(target = "usagesActuels", constant = "0")
    Coupon toEntity(CouponCreateDTO createDto);
}
