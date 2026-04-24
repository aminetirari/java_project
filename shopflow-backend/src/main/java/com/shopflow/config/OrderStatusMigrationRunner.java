package com.shopflow.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Migrates legacy OrderStatus values persisted before the PAYE enum was
 * removed. Runs before {@link DatabaseSeeder} and any repository reads so
 * Hibernate never deserializes an unknown enum constant.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
@Slf4j
public class OrderStatusMigrationRunner implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        try {
            int updated = jdbcTemplate.update(
                    "UPDATE orders SET statut = 'PAID' WHERE statut = 'PAYE'");
            if (updated > 0) {
                log.info("Migration OrderStatus: {} ligne(s) PAYE convertie(s) en PAID.", updated);
            }
        } catch (Exception e) {
            log.warn("Migration OrderStatus ignorée (table orders absente ou BDD vide): {}", e.getMessage());
        }
    }
}
