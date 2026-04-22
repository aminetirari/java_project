package com.shopflow.mapper;

import com.shopflow.dto.AddressDTO;
import com.shopflow.entity.Address;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AddressMapper {

    @Mapping(target = "userId", source = "user.id")
    AddressDTO toDto(Address address);

    @Mapping(target = "user", ignore = true) // Set by service explicitly
    Address toEntity(AddressDTO addressDTO);
}
