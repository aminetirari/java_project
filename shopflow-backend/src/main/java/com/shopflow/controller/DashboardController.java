package com.shopflow.controller;

import com.shopflow.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> admin() {
        return ResponseEntity.ok(dashboardService.adminDashboard());
    }

    @GetMapping("/seller")
    @PreAuthorize("hasAnyRole('SELLER','ADMIN')")
    public ResponseEntity<Map<String, Object>> seller(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(dashboardService.sellerDashboard(userDetails.getUsername()));
    }

    @GetMapping("/customer")
    @PreAuthorize("hasAnyRole('CUSTOMER','ADMIN','SELLER')")
    public ResponseEntity<Map<String, Object>> customer(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(dashboardService.customerDashboard(userDetails.getUsername()));
    }
}
