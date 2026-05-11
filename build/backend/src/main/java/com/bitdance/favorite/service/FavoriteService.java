package com.bitdance.favorite.service;

import com.bitdance.common.exception.BizException;
import com.bitdance.favorite.domain.Favorite;
import com.bitdance.favorite.dto.FavoriteDto;
import com.bitdance.favorite.repository.FavoriteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
public class FavoriteService {

    private static final Set<String> ALLOWED_TYPES = Set.of(
        "studio", "course", "coach", "workshop", "content_post"
    );

    private final FavoriteRepository repo;

    public FavoriteService(FavoriteRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public boolean toggle(Long userId, String targetType, Long targetId) {
        if (!ALLOWED_TYPES.contains(targetType)) {
            throw new BizException("INVALID_ARGUMENT", "targetType 非法");
        }
        return repo.findByUserIdAndTargetTypeAndTargetId(userId, targetType, targetId)
            .map(existing -> {
                repo.delete(existing);
                return false;
            })
            .orElseGet(() -> {
                Favorite f = new Favorite();
                f.setUserId(userId);
                f.setTargetType(targetType);
                f.setTargetId(targetId);
                repo.save(f);
                return true;
            });
    }

    @Transactional(readOnly = true)
    public List<FavoriteDto> list(Long userId, String targetType) {
        if (targetType != null && !ALLOWED_TYPES.contains(targetType)) {
            throw new BizException("INVALID_ARGUMENT", "targetType 非法");
        }
        List<Favorite> items = targetType == null
            ? repo.findAll().stream().filter(f -> f.getUserId().equals(userId)).toList()
            : repo.findByUserIdAndTargetTypeOrderByIdDesc(userId, targetType);
        return items.stream()
            .map(f -> new FavoriteDto(f.getId(), f.getTargetType(), f.getTargetId(), f.getCreatedAt()))
            .toList();
    }

    @Transactional(readOnly = true)
    public boolean check(Long userId, String targetType, Long targetId) {
        return repo.existsByUserIdAndTargetTypeAndTargetId(userId, targetType, targetId);
    }
}
