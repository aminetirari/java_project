package com.shopflow.service;

import com.shopflow.dto.CouponCreateDTO;
import com.shopflow.dto.CouponDTO;
import com.shopflow.entity.Coupon;
import com.shopflow.exception.ResourceNotFoundException;
import com.shopflow.mapper.CouponMapper;
import com.shopflow.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;
    private final CouponMapper couponMapper;

    @Transactional(readOnly = true)
    public List<CouponDTO> getAllCoupons() {
        return couponMapper.toDtoList(couponRepository.findAll());
    }

    @Transactional(readOnly = true)
    public CouponDTO getCouponById(Long id) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon introuvable"));
        return couponMapper.toDto(coupon);
    }

    @Transactional(readOnly = true)
    public CouponDTO getCouponByCode(String code) {
        Coupon coupon = couponRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Code de coupon invalide"));
        return couponMapper.toDto(coupon);
    }

    @Transactional
    public CouponDTO createCoupon(CouponCreateDTO createDTO) {
        if (couponRepository.findByCode(createDTO.getCode()).isPresent()) {
            throw new IllegalStateException("Ce code de coupon existe déjà");
        }
        Coupon coupon = couponMapper.toEntity(createDTO);
        return couponMapper.toDto(couponRepository.save(coupon));
    }

    @Transactional
    public CouponDTO updateCoupon(Long id, CouponCreateDTO updateDTO) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon introuvable"));

        if (!coupon.getCode().equals(updateDTO.getCode())
                && couponRepository.findByCode(updateDTO.getCode()).isPresent()) {
            throw new IllegalStateException("Ce code de coupon existe déjà");
        }

        coupon.setCode(updateDTO.getCode());
        coupon.setType(updateDTO.getType());
        coupon.setValeur(updateDTO.getValeur());
        coupon.setDateExpiration(updateDTO.getDateExpiration());
        coupon.setUsagesMax(updateDTO.getUsagesMax());

        return couponMapper.toDto(couponRepository.save(coupon));
    }

    @Transactional
    public void deleteCoupon(Long id) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon introuvable"));
        coupon.setActif(false);
        couponRepository.save(coupon);
    }
}
