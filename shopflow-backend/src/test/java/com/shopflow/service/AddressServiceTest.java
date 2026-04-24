package com.shopflow.service;

import com.shopflow.dto.AddressDTO;
import com.shopflow.entity.Address;
import com.shopflow.entity.Role;
import com.shopflow.entity.User;
import com.shopflow.exception.ResourceNotFoundException;
import com.shopflow.mapper.AddressMapper;
import com.shopflow.repository.AddressRepository;
import com.shopflow.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AddressServiceTest {

    @Mock private AddressRepository addressRepository;
    @Mock private AddressMapper addressMapper;
    @Mock private UserRepository userRepository;

    @InjectMocks private AddressService addressService;

    private User user;
    private User otherUser;
    private Address address;
    private AddressDTO dto;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).email("u@shopflow.com").role(Role.CUSTOMER).actif(true).build();
        otherUser = User.builder().id(2L).email("other@shopflow.com").role(Role.CUSTOMER).actif(true).build();
        address = Address.builder().id(10L).user(user).rue("1 rue Pasteur")
                .ville("Paris").codePostal("75000").pays("France").principal(false).build();
        dto = AddressDTO.builder().id(10L).rue("1 rue Pasteur").ville("Paris")
                .codePostal("75000").pays("France").principal(false).build();
    }

    @Test
    void getUserAddresses_returnsList() {
        when(userRepository.findByEmail("u@shopflow.com")).thenReturn(Optional.of(user));
        when(addressRepository.findByUserId(1L)).thenReturn(List.of(address));
        when(addressMapper.toDto(address)).thenReturn(dto);

        List<AddressDTO> result = addressService.getUserAddresses("u@shopflow.com");

        assertThat(result).hasSize(1);
    }

    @Test
    void getUserAddresses_userNotFound_throws() {
        when(userRepository.findByEmail("nope@shopflow.com")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> addressService.getUserAddresses("nope@shopflow.com"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void createAddress_firstOne_becomesPrincipal() {
        AddressDTO input = AddressDTO.builder().rue("new").ville("Lyon")
                .codePostal("69000").pays("France").principal(false).build();
        Address newAddress = Address.builder().rue("new").ville("Lyon")
                .codePostal("69000").pays("France").build();

        when(userRepository.findByEmail("u@shopflow.com")).thenReturn(Optional.of(user));
        when(addressMapper.toEntity(input)).thenReturn(newAddress);
        when(addressRepository.findByUserId(1L)).thenReturn(List.of());
        when(addressRepository.save(newAddress)).thenReturn(newAddress);
        when(addressMapper.toDto(newAddress)).thenReturn(dto);

        addressService.createAddress("u@shopflow.com", input);

        assertThat(newAddress.getPrincipal()).isTrue();
    }

    @Test
    void createAddress_flagPrincipal_resetsOthers() {
        Address existing = Address.builder().id(11L).user(user).principal(true).build();
        AddressDTO input = AddressDTO.builder().rue("new").principal(true).build();
        Address newAddress = Address.builder().rue("new").build();

        when(userRepository.findByEmail("u@shopflow.com")).thenReturn(Optional.of(user));
        when(addressMapper.toEntity(input)).thenReturn(newAddress);
        when(addressRepository.findByUserId(1L)).thenReturn(List.of(existing));
        when(addressRepository.save(newAddress)).thenReturn(newAddress);
        when(addressMapper.toDto(newAddress)).thenReturn(dto);

        addressService.createAddress("u@shopflow.com", input);

        assertThat(existing.getPrincipal()).isFalse();
        assertThat(newAddress.getPrincipal()).isTrue();
        verify(addressRepository).saveAll(anyList());
    }

    @Test
    void createAddress_notPrincipal_whenOthersExist() {
        Address existing = Address.builder().id(11L).user(user).principal(true).build();
        AddressDTO input = AddressDTO.builder().rue("new").principal(false).build();
        Address newAddress = Address.builder().rue("new").build();

        when(userRepository.findByEmail("u@shopflow.com")).thenReturn(Optional.of(user));
        when(addressMapper.toEntity(input)).thenReturn(newAddress);
        when(addressRepository.findByUserId(1L)).thenReturn(List.of(existing));
        when(addressRepository.save(newAddress)).thenReturn(newAddress);
        when(addressMapper.toDto(newAddress)).thenReturn(dto);

        addressService.createAddress("u@shopflow.com", input);

        assertThat(newAddress.getPrincipal()).isFalse();
    }

    @Test
    void deleteAddress_success() {
        when(userRepository.findByEmail("u@shopflow.com")).thenReturn(Optional.of(user));
        when(addressRepository.findById(10L)).thenReturn(Optional.of(address));

        addressService.deleteAddress("u@shopflow.com", 10L);

        verify(addressRepository).delete(address);
    }

    @Test
    void deleteAddress_notOwner_throws() {
        address.setUser(otherUser);
        when(userRepository.findByEmail("u@shopflow.com")).thenReturn(Optional.of(user));
        when(addressRepository.findById(10L)).thenReturn(Optional.of(address));

        assertThatThrownBy(() -> addressService.deleteAddress("u@shopflow.com", 10L))
                .isInstanceOf(IllegalArgumentException.class);
        verify(addressRepository, never()).delete(any(Address.class));
    }

    @Test
    void deleteAddress_addressNotFound_throws() {
        when(userRepository.findByEmail("u@shopflow.com")).thenReturn(Optional.of(user));
        when(addressRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> addressService.deleteAddress("u@shopflow.com", 99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
