package br.gov.formosa.backup.dashboard.api.controller;

import br.gov.formosa.backup.dashboard.api.dto.DashboardDTO;
import br.gov.formosa.backup.dashboard.domain.service.DashboardService;
import br.gov.formosa.backup.user.domain.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final UserService userService;

    @GetMapping
    public DashboardDTO getDashboardData() {
        return dashboardService.getDashboardData();
    }
}