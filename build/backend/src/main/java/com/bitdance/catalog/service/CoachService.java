package com.bitdance.catalog.service;

import com.bitdance.catalog.domain.Coach;
import com.bitdance.catalog.domain.CoachDanceStyle;
import com.bitdance.catalog.domain.Course;
import com.bitdance.catalog.dto.CoachDetail;
import com.bitdance.catalog.dto.CoachStyleDto;
import com.bitdance.catalog.dto.CourseCard;
import com.bitdance.catalog.repository.CoachDanceStyleRepository;
import com.bitdance.catalog.repository.CoachRepository;
import com.bitdance.catalog.repository.CourseRepository;
import com.bitdance.common.exception.BizException;
import com.bitdance.favorite.repository.FavoriteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CoachService {

    private static final String TARGET_TYPE = "coach";

    private final CoachRepository coachRepo;
    private final CoachDanceStyleRepository styleRepo;
    private final CourseRepository courseRepo;
    private final FavoriteRepository favoriteRepo;

    public CoachService(
        CoachRepository coachRepo,
        CoachDanceStyleRepository styleRepo,
        CourseRepository courseRepo,
        FavoriteRepository favoriteRepo
    ) {
        this.coachRepo = coachRepo;
        this.styleRepo = styleRepo;
        this.courseRepo = courseRepo;
        this.favoriteRepo = favoriteRepo;
    }

    @Transactional(readOnly = true)
    public CoachDetail detail(Long id, Long currentUserId) {
        Coach c = coachRepo.findById(id)
            .orElseThrow(() -> new BizException("COACH_NOT_FOUND", "教练不存在"));
        List<CoachStyleDto> styles = styleRepo.findByCoachId(id).stream()
            .map(s -> new CoachStyleDto(s.getDanceStyleId(), s.getProficiencyLevel()))
            .toList();
        boolean favored = currentUserId != null && favoriteRepo
            .existsByUserIdAndTargetTypeAndTargetId(currentUserId, TARGET_TYPE, id);
        return new CoachDetail(
            c.getId(), c.getUserId(), c.getDisplayName(),
            c.getIntro(), c.getTeachingStyle(),
            c.getAvailableTimeSlots(), c.getCertificationStatus(),
            c.getHomeStudioId(), c.getCoverAssetId(), c.getAvgRating(),
            styles, favored
        );
    }

    @Transactional(readOnly = true)
    public List<CourseCard> coursesOfCoach(Long coachId) {
        return courseRepo.findByCoachIdAndStatusOrderByIdDesc(coachId, "published").stream()
            .map(this::toCard)
            .toList();
    }

    private CourseCard toCard(Course c) {
        return new CourseCard(
            c.getId(), c.getStudioId(), c.getCoachId(), c.getDanceStyleId(),
            c.getCourseName(), c.getDifficultyLevel(), c.getPriceAmount(),
            c.getDurationMinutes(), c.getZeroBasicFriendly(), c.getCoverAssetId()
        );
    }
}
