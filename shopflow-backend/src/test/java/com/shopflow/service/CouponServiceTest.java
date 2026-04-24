package com.shopflow.service;

import com.shopflow.dto.CouponCreateDTO;
import com.shopflow.dto.CouponDTO;
import com.shopflow.entity.Coupon;
import com.shopflow.entity.CouponType;
import com.shopflow.exception.ResourceNotFoundException;
import com.shopflow.mapper.CouponMapper;
import com.shopflow.repository.CouponRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CouponServiceTest {

    @Mock private CouponRepository couponRepository;
    @Mock private CouponMapper couponMapper;

    @InjectMocks private CouponService couponService;

    private Coupon coupon;
    private CouponDTO dto;
    private CouponCreateDTO createDTO;

    @BeforeEach
    void setUp() {
        coupon = Coupon.builder()
                .id(1L).code("WELCOME10").type(CouponType.PERCENT)
                .valeur(BigDecimal.TEN).actif(true)
                .dateExpiration(LocalDateTime.now().plusDays(10))
                .usagesMax(100).usagesActuels(0).build();
        dto = CouponDTO.builder().id(1L).code("WELCOME10").build();
        createDTO = new CouponCreateDTO();
        createDTO.setCode("WELCOME10");
        createDTO.setType(CouponType.PERCENT);
        createDTO.setValeur(BigDecimal.TEN);
        createDTO.setDateExpiration(LocalDateTime.now().plusDays(10));
        createDTO.setUsagesMax(100);
    }

    @Test
    void getAllCoupons_returnsList() {
        when(couponRepository.findAll()).thenReturn(List.of(coupon));
        when(couponMapper.toDtoList(List.of(coupon))).thenReturn(List.of(dto));

        assertThat(couponService.getAllCoupons()).hasSize(1);
    }

    @Test
    void getCouponById_found() {
        when(couponRepository.findById(1L)).thenReturn(Optional.of(coupon));
        when(couponMapper.toDto(coupon)).thenReturn(dto);

        assertThat(couponService.getCouponById(1L).getCode()).isEqualTo("WELCOME10");
    }

    @Test
    void getCouponById_notFound_throws() {
        when(couponRepository.findById(9L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> couponService.getCouponById(9L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getCouponByCode_found() {
        when(couponRepository.findByCode("WELCOME10")).thenReturn(Optional.of(coupon));
        when(couponMapper.toDto(coupon)).thenReturn(dto);

        assertThat(couponService.getCouponByCode("WELCOME10")).isNotNull();
    }

    @Test
    void getCouponByCode_notFound_throws() {
        when(couponRepository.findByCode("NOPE")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> couponService.getCouponByCode("NOPE"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void createCoupon_success() {
        when(couponRepository.findByCode("WELCOME10")).thenReturn(Optional.empty());
        when(couponMapper.toEntity(createDTO)).thenReturn(coupon);
        when(couponRepository.save(coupon)).thenReturn(coupon);
        when(couponMapper.toDto(coupon)).thenReturn(dto);

        CouponDTO result = couponService.createCoupon(createDTO);

        assertThat(result.getCode()).isEqualTo("WELCOME10");
        verify(couponRepository).save(coupon);
    }

    @Test
    void createCoupon_duplicate_throws() {
        when(couponRepository.findByCode("WELCOME10")).thenReturn(Optional.of(coupon));

        assertThatThrownBy(() -> couponService.createCoupon(createDTO))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("existe déjà");
        verify(couponRepository, never()).save(any());
    }

    @Test
    void updateCoupon_sameCode_success() {
        when(couponRepository.findById(1L)).thenReturn(Optional.of(coupon));
        when(couponRepository.save(coupon)).thenReturn(coupon);
        when(couponMapper.toDto(coupon)).thenReturn(dto);

        CouponDTO result = couponService.updateCoupon(1L, createDTO);

        assertThat(result).isNotNull();
        verify(couponRepository).save(coupon);
    }

    @Test
    void updateCoupon_newUniqueCode_success() {
        CouponCreateDTO renamed = new CouponCreateDTO();
        renamed.setCode("FRESH");
        renamed.setType(CouponType.FIXED);
        renamed.setValeur(BigDecimal.valueOf(5));
        renamed.setDateExpiration(LocalDateTime.now().plusDays(5));
        renamed.setUsagesMax(50);

        when(couponRepository.findById(1L)).thenReturn(Optional.of(coupon));
        when(couponRepository.findByCode("FRESH")).thenReturn(Optional.empty());
        when(couponRepository.save(coupon)).thenReturn(coupon);
        when(couponMapper.toDto(coupon)).thenReturn(dto);

        couponService.updateCoupon(1L, renamed);

        assertThat(coupon.getCode()).isEqualTo("FRESH");
        assertThat(coupon.getType()).isEqualTo(CouponType.FIXED);
    }

    @Test
    void updateCoupon_conflictingCode_throws() {
        CouponCreateDTO conflict = new CouponCreateDTO();
        conflict.setCode("OTHER");
        Coupon other = Coupon.builder().id(2L).code("OTHER").build();

        when(couponRepository.findById(1L)).thenReturn(Optional.of(coupon));
        when(couponRepository.findByCode("OTHER")).thenReturn(Optional.of(other));

        assertThatThrownBy(() -> couponService.updateCoupon(1L, conflict))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("existe déjà");
        verify(couponRepository, never()).save(any());
    }

    @Test
    void updateCoupon_notFound_throws() {
        when(couponRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> couponService.updateCoupon(99L, createDTO))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteCoupon_softDeletes() {
        when(couponRepository.findById(1L)).thenReturn(Optional.of(coupon));

        couponService.deleteCoupon(1L);

        assertThat(coupon.getActif()).isFalse();
        verify(couponRepository).save(coupon);
    }

    @Test
    void deleteCoupon_notFound_throws() {
        when(couponRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> couponService.deleteCoupon(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
