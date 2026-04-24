package com.shopflow.service;

import com.shopflow.entity.PasswordResetToken;
import com.shopflow.entity.Role;
import com.shopflow.entity.User;
import com.shopflow.repository.PasswordResetTokenRepository;
import com.shopflow.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @Mock private PasswordResetTokenRepository tokenRepository;
    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks private PasswordResetService passwordResetService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).email("u@shopflow.com").role(Role.CUSTOMER).actif(true).build();
        ReflectionTestUtils.setField(passwordResetService, "frontendUrl", "http://localhost:3000");
        ReflectionTestUtils.setField(passwordResetService, "exposeTokenInResponse", true);
    }

    @Test
    void createToken_validUser_returnsToken() {
        when(userRepository.findByEmail("u@shopflow.com")).thenReturn(Optional.of(user));

        Optional<String> token = passwordResetService.createToken("u@shopflow.com");

        assertThat(token).isPresent();
        verify(tokenRepository).deleteByUser(user);
        verify(tokenRepository).save(any(PasswordResetToken.class));
    }

    @Test
    void createToken_unknownUser_returnsEmpty() {
        when(userRepository.findByEmail("nobody@shopflow.com")).thenReturn(Optional.empty());

        Optional<String> token = passwordResetService.createToken("nobody@shopflow.com");

        assertThat(token).isEmpty();
        verify(tokenRepository, never()).save(any());
    }

    @Test
    void createToken_inactiveUser_returnsEmpty() {
        user.setActif(false);
        when(userRepository.findByEmail("u@shopflow.com")).thenReturn(Optional.of(user));

        Optional<String> token = passwordResetService.createToken("u@shopflow.com");

        assertThat(token).isEmpty();
    }

    @Test
    void createToken_notExposing_returnsEmpty() {
        ReflectionTestUtils.setField(passwordResetService, "exposeTokenInResponse", false);
        when(userRepository.findByEmail("u@shopflow.com")).thenReturn(Optional.of(user));

        Optional<String> token = passwordResetService.createToken("u@shopflow.com");

        assertThat(token).isEmpty();
        verify(tokenRepository).save(any(PasswordResetToken.class));
    }

    @Test
    void reset_success() {
        PasswordResetToken t = PasswordResetToken.builder()
                .token("abc").user(user).used(false)
                .expiresAt(LocalDateTime.now().plusMinutes(30)).build();
        when(tokenRepository.findByToken("abc")).thenReturn(Optional.of(t));
        when(passwordEncoder.encode("newpass")).thenReturn("ENC(newpass)");

        passwordResetService.reset("abc", "newpass");

        assertThat(user.getMotDePasse()).isEqualTo("ENC(newpass)");
        assertThat(t.getUsed()).isTrue();
        verify(userRepository).save(user);
        verify(tokenRepository).save(t);
    }

    @Test
    void reset_invalidToken_throws() {
        when(tokenRepository.findByToken("bad")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> passwordResetService.reset("bad", "x"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void reset_alreadyUsed_throws() {
        PasswordResetToken t = PasswordResetToken.builder()
                .token("abc").user(user).used(true)
                .expiresAt(LocalDateTime.now().plusMinutes(30)).build();
        when(tokenRepository.findByToken("abc")).thenReturn(Optional.of(t));

        assertThatThrownBy(() -> passwordResetService.reset("abc", "x"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("déjà été utilisé");
    }

    @Test
    void reset_expired_throws() {
        PasswordResetToken t = PasswordResetToken.builder()
                .token("abc").user(user).used(false)
                .expiresAt(LocalDateTime.now().minusMinutes(1)).build();
        when(tokenRepository.findByToken("abc")).thenReturn(Optional.of(t));

        assertThatThrownBy(() -> passwordResetService.reset("abc", "x"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("expiré");
    }
}
