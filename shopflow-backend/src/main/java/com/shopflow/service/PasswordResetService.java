package com.shopflow.service;

import com.shopflow.entity.PasswordResetToken;
import com.shopflow.entity.User;
import com.shopflow.repository.PasswordResetTokenRepository;
import com.shopflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetService.class);
    private static final long TOKEN_TTL_MINUTES = 60;

    private final PasswordResetTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    @Value("${app.password-reset.expose-token:true}")
    private boolean exposeTokenInResponse;

    /**
     * Creates a reset token if the email matches an active user.
     * Returns the token only when {@code app.password-reset.expose-token} is true
     * (dev mode without SMTP); otherwise returns empty so the controller can
     * answer with a generic message without leaking account existence.
     */
    @Transactional
    public Optional<String> createToken(String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null || Boolean.FALSE.equals(user.getActif())) {
            log.info("Demande de reset ignorée pour email inconnu/inactif: {}", email);
            return Optional.empty();
        }
        tokenRepository.deleteByUser(user);
        String token = UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "");
        PasswordResetToken entity = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiresAt(LocalDateTime.now().plusMinutes(TOKEN_TTL_MINUTES))
                .used(false)
                .build();
        tokenRepository.save(entity);

        String resetUrl = frontendUrl + "/reset-password?token=" + token;
        log.info("Reset password — lien pour {} (valide {} min) : {}", user.getEmail(), TOKEN_TTL_MINUTES, resetUrl);

        return exposeTokenInResponse ? Optional.of(token) : Optional.empty();
    }

    @Transactional
    public void reset(String token, String newPassword) {
        PasswordResetToken entity = tokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Lien invalide ou expiré."));
        if (Boolean.TRUE.equals(entity.getUsed())) {
            throw new IllegalArgumentException("Ce lien a déjà été utilisé.");
        }
        if (entity.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Lien invalide ou expiré.");
        }
        User user = entity.getUser();
        user.setMotDePasse(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        entity.setUsed(true);
        tokenRepository.save(entity);
        log.info("Mot de passe réinitialisé pour {}", user.getEmail());
    }
}
