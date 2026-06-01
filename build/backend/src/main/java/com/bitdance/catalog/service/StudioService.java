package com.bitdance.catalog.service;

import com.bitdance.catalog.domain.Studio;
import com.bitdance.catalog.domain.StudioDanceStyle;
import com.bitdance.catalog.dto.StudioCard;
import com.bitdance.catalog.dto.StudioDetail;
import com.bitdance.catalog.dto.StudioListResponse;
import com.bitdance.catalog.repository.StudioDanceStyleRepository;
import com.bitdance.catalog.repository.StudioRepository;
import com.bitdance.catalog.repository.StudioSearchRepository;
import com.bitdance.catalog.repository.StudioSearchRepository.SearchParams;
import com.bitdance.catalog.repository.StudioSearchRepository.StudioNearbyRow;
import com.bitdance.common.exception.BizException;
import com.bitdance.favorite.repository.FavoriteRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class StudioService {

    private static final String TARGET_TYPE = "studio";

    private final StudioRepository studioRepo;
    private final StudioDanceStyleRepository styleRepo;
    private final StudioSearchRepository searchRepo;
    private final FavoriteRepository favoriteRepo;

    public StudioService(
        StudioRepository studioRepo,
        StudioDanceStyleRepository styleRepo,
        StudioSearchRepository searchRepo,
        FavoriteRepository favoriteRepo
    ) {
        this.studioRepo = studioRepo;
        this.styleRepo = styleRepo;
        this.searchRepo = searchRepo;
        this.favoriteRepo = favoriteRepo;
    }

    @Transactional(readOnly = true)
    public StudioListResponse searchNearby(
        Long cityId, Double latitude, Double longitude, Double distanceKm,
        String keyword, Long danceStyleId, BigDecimal minPrice, BigDecimal maxPrice,
        String timeSlot, Boolean trialAvailable, Boolean zeroBasicFriendly, Boolean nearMetro,
        int page, int pageSize, Long currentUserId
    ) {
        int safePage = Math.max(1, page);
        int safeSize = Math.min(Math.max(1, pageSize), 100);
        SearchParams p = new SearchParams(
            cityId, latitude, longitude, distanceKm, keyword, danceStyleId,
            minPrice, maxPrice, timeSlot, trialAvailable, zeroBasicFriendly, nearMetro,
            safePage, safeSize
        );
        List<StudioNearbyRow> rows = searchRepo.searchNearby(p);

        Set<Long> favored = currentUserId == null
            ? Set.of()
            : new HashSet<>(favoriteRepo.findFavoredIds(currentUserId, TARGET_TYPE,
                rows.stream().map(StudioNearbyRow::id).toList()));

        List<StudioCard> list = rows.stream().map(r -> new StudioCard(
            r.id(), r.studioName(), r.address(), r.cityId(), r.businessDistrictId(),
            r.coverAssetId(), r.distanceKm(), r.latitude(), r.longitude(),
            favored.contains(r.id())
        )).toList();
        return new StudioListResponse(list, safePage, safeSize);
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "studio:detail",
        key = "#id + ':' + (#currentUserId == null ? 0 : #currentUserId)")
    public StudioDetail detail(Long id, Long currentUserId) {
        Studio s = studioRepo.findById(id)
            .orElseThrow(() -> new BizException("STUDIO_NOT_FOUND", "舞室不存在"));
        if (!"active".equals(s.getStatus())) {
            throw new BizException("STUDIO_INACTIVE", "舞室已下架");
        }
        List<Long> styleIds = styleRepo.findByStudioId(id).stream()
            .map(StudioDanceStyle::getDanceStyleId).toList();
        boolean favored = currentUserId != null && favoriteRepo.existsByUserIdAndTargetTypeAndTargetId(
            currentUserId, TARGET_TYPE, id
        );
        return new StudioDetail(
            s.getId(), s.getStudioName(), s.getBrandName(),
            s.getAddress(), s.getTransportInfo(),
            s.getCityId(), s.getBusinessDistrictId(),
            s.getLatitude(), s.getLongitude(),
            s.getContactPhone(), s.getIntro(),
            s.getCoverAssetId(), s.getClaimStatus(),
            styleIds, favored
        );
    }
}
