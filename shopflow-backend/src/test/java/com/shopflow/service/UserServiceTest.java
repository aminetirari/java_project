package com.shopflow.service;

import com.shopflow.dto.UserCreateDTO;
import com.shopflow.dto.UserDTO;
import com.shopflow.entity.Role;
import com.shopflow.entity.User;
import com.shopflow.exception.ResourceNotFoundException;
import com.shopflow.mapper.UserMapper;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private UserMapper userMapper;

    @InjectMocks private UserService userService;

    private User user;
    private UserDTO dto;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).email("u@shopflow.com").prenom("J").nom("D")
                .role(Role.CUSTOMER).actif(true).build();
        dto = new UserDTO();
        dto.setId(1L);
        dto.setEmail("u@shopflow.com");
    }

    @Test
    void getAllUsers_returnsMappedList() {
        when(userRepository.findAll()).thenReturn(List.of(user));
        when(userMapper.toDtoList(List.of(user))).thenReturn(List.of(dto));

        List<UserDTO> result = userService.getAllUsers();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEmail()).isEqualTo("u@shopflow.com");
    }

    @Test
    void getUserById_found_returnsDto() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(dto);

        UserDTO result = userService.getUserById(1L);

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void getUserById_notFound_throws() {
        when(userRepository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(42L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void createUser_savesAndReturns() {
        UserCreateDTO create = new UserCreateDTO();
        create.setEmail("new@shopflow.com");
        when(userMapper.toEntity(create)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toDto(user)).thenReturn(dto);

        UserDTO result = userService.createUser(create);

        assertThat(result).isNotNull();
        verify(userRepository).save(user);
    }

    @Test
    void updateUser_found_savesMergedEntity() {
        UserCreateDTO update = new UserCreateDTO();
        update.setPrenom("Paul");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toDto(user)).thenReturn(dto);

        UserDTO result = userService.updateUser(1L, update);

        assertThat(result).isNotNull();
        verify(userMapper).updateEntityFromDto(update, user);
        verify(userRepository).save(user);
    }

    @Test
    void updateUser_notFound_throws() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> userService.updateUser(99L, new UserCreateDTO()))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(userRepository, never()).save(any());
    }

    @Test
    void setActive_togglesFlagAndSaves() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toDto(user)).thenReturn(dto);

        userService.setActive(1L, false);

        assertThat(user.getActif()).isFalse();
        verify(userRepository).save(user);
    }

    @Test
    void setActive_notFound_throws() {
        when(userRepository.findById(5L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> userService.setActive(5L, true))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteUser_existing_deletes() {
        when(userRepository.existsById(1L)).thenReturn(true);
        userService.deleteUser(1L);
        verify(userRepository).deleteById(1L);
    }

    @Test
    void deleteUser_missing_throws() {
        when(userRepository.existsById(2L)).thenReturn(false);
        assertThatThrownBy(() -> userService.deleteUser(2L))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(userRepository, never()).deleteById(any());
    }
}
