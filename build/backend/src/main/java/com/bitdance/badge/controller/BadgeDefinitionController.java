package com.bitdance.badge.controller;

import com.bitdance.badge.domain.BadgeDefinition;
import com.bitdance.badge.dto.BadgeDefinitionDto;
import com.bitdance.badge.repository.BadgeDefinitionRepository;
import com.bitdance.common.web.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/public/badges")
public class BadgeDefinitionController {

    private final BadgeDefinitionRepository repo;

    public BadgeDefinitionController(BadgeDefinitionRepository repo) {
        this.repo = repo;
    }

    @GetMapping("/definitions")
    public ApiResponse<List<BadgeDefinitionDto>> listActive() {
        return ApiResponse.ok(repo.findByStatusOrderByIdAsc("active").stream()
            .map(this::toDto)
            .toList());
    }

    private BadgeDefinitionDto toDto(BadgeDefinition d) {
        return new BadgeDefinitionDto(
            d.getId(), d.getBadgeCode(), d.getBadgeName(), d.getDescription(),
            d.getIconAssetId(), d.getRuleType(), d.getRuleConfig(), d.getStatus()
        );
    }
}
