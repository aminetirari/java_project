package com.shopflow.service;

import com.shopflow.dto.AddressDTO;
import com.shopflow.entity.Address;
import com.shopflow.entity.User;
import com.shopflow.exception.ResourceNotFoundException;
import com.shopflow.mapper.AddressMapper;
import com.shopflow.repository.AddressRepository;
import com.shopflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository addressRepository;
    private final AddressMapper addressMapper;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<AddressDTO> getUserAddresses(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));
        return addressRepository.findByUserId(user.getId()).stream()
                .map(addressMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public AddressDTO createAddress(String email, AddressDTO addressDTO) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));
        
        Address address = addressMapper.toEntity(addressDTO);
        address.setUser(user);

        // Si l'utilisateur n'a pas d'adresse principale, celle-ci le devient automatiquement
        List<Address> userAddresses = addressRepository.findByUserId(user.getId());
        if (userAddresses.isEmpty() || Boolean.TRUE.equals(addressDTO.getPrincipal())) {
            address.setPrincipal(true);
            // reset others if any
            userAddresses.forEach(a -> a.setPrincipal(false));
            addressRepository.saveAll(userAddresses);
        } else {
            address.setPrincipal(false);
        }

        Address savedAddress = addressRepository.save(address);
        return addressMapper.toDto(savedAddress);
    }

    @Transactional
    public void deleteAddress(String email, Long addressId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Adresse non trouvée"));

        if (!address.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Action non autorisée");
        }

        addressRepository.delete(address);
    }
}
