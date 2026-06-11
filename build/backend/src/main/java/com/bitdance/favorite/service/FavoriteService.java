package com.bitdance.favorite.service;

import com.bitdance.catalog.domain.Coach;
import com.bitdance.catalog.domain.Course;
import com.bitdance.catalog.domain.Studio;
import com.bitdance.catalog.repository.CoachRepository;
import com.bitdance.catalog.repository.CourseRepository;
import com.bitdance.catalog.repository.StudioRepository;
import com.bitdance.community.domain.ContentPost;
import com.bitdance.community.repository.ContentPostRepository;
import com.bitdance.common.exception.BizException;
import com.bitdance.favorite.domain.Favorite;
import com.bitdance.favorite.dto.FavoriteCardDto;
import com.bitdance.favorite.dto.FavoriteDto;
import com.bitdance.favorite.repository.FavoriteRepository;
import com.bitdance.media.service.MediaAssetService;
import com.bitdance.workshop.domain.Workshop;
import com.bitdance.workshop.repository.WorkshopRepository;
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
    private final StudioRepository studioRepo;
    private final CourseRepository courseRepo;
    private final CoachRepository coachRepo;
    private final WorkshopRepository workshopRepo;
    private final ContentPostRepository contentPostRepo;
    private final MediaAssetService mediaAssetService;

    public FavoriteService(
        FavoriteRepository repo,
        StudioRepository studioRepo,
        CourseRepository courseRepo,
        CoachRepository coachRepo,
        WorkshopRepository workshopRepo,
        ContentPostRepository contentPostRepo,
        MediaAssetService mediaAssetService
    ) {
        this.repo = repo;
        this.studioRepo = studioRepo;
        this.courseRepo = courseRepo;
        this.coachRepo = coachRepo;
        this.workshopRepo = workshopRepo;
        this.contentPostRepo = contentPostRepo;
        this.mediaAssetService = mediaAssetService;
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
            .map(f -> new FavoriteDto(f.getId(), f.getTargetType(), f.getTargetId(), f.getCreatedAt(), card(f)))
            .toList();
    }

    @Transactional(readOnly = true)
    public boolean check(Long userId, String targetType, Long targetId) {
        return repo.existsByUserIdAndTargetTypeAndTargetId(userId, targetType, targetId);
    }

    private FavoriteCardDto card(Favorite f) {
        return switch (f.getTargetType()) {
            case "studio" -> studioRepo.findById(f.getTargetId())
                .map(this::studioCard)
                .orElse(missingCard(f));
            case "course" -> courseRepo.findById(f.getTargetId())
                .map(this::courseCard)
                .orElse(missingCard(f));
            case "coach" -> coachRepo.findById(f.getTargetId())
                .map(this::coachCard)
                .orElse(missingCard(f));
            case "workshop" -> workshopRepo.findById(f.getTargetId())
                .map(this::workshopCard)
                .orElse(missingCard(f));
            case "content_post" -> contentPostRepo.findById(f.getTargetId())
                .map(this::postCard)
                .orElse(missingCard(f));
            default -> missingCard(f);
        };
    }

    private FavoriteCardDto studioCard(Studio s) {
        return new FavoriteCardDto(s.getStudioName(), s.getAddress(), cover(s.getCoverAssetId()),
            "/studio/" + s.getId(), "预约试听");
    }

    private FavoriteCardDto courseCard(Course c) {
        return new FavoriteCardDto(c.getCourseName(), c.getDifficultyLevel() + " · " + c.getDurationMinutes() + "分钟",
            cover(c.getCoverAssetId()), "/course/" + c.getId(), "查看课程");
    }

    private FavoriteCardDto coachCard(Coach c) {
        return new FavoriteCardDto(c.getDisplayName(), c.getTeachingStyle(), cover(c.getCoverAssetId()),
            "/coach/" + c.getId(), "查看老师");
    }

    private FavoriteCardDto workshopCard(Workshop w) {
        return new FavoriteCardDto(w.getWorkshopName(), w.getLocationName() + " · " + w.getPriceAmount(),
            cover(w.getCoverAssetId()), "/workshop/" + w.getId(), "报名活动");
    }

    private FavoriteCardDto postCard(ContentPost p) {
        return new FavoriteCardDto("社区内容", truncate(p.getContentText()), null,
            "/community/post/" + p.getId(), "查看动态");
    }

    private FavoriteCardDto missingCard(Favorite f) {
        return new FavoriteCardDto("已收藏内容", f.getTargetType() + " #" + f.getTargetId(),
            null, "/", "查看");
    }

    private String cover(Long assetId) {
        return assetId == null ? null : mediaAssetService.url(assetId);
    }

    private String truncate(String text) {
        if (text == null) return "";
        return text.length() <= 40 ? text : text.substring(0, 40) + "...";
    }
}
