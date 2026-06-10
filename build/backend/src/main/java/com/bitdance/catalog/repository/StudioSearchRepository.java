package com.bitdance.catalog.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Studio 附近搜索：bounding box 粗筛 + fn_haversine_km 精排，两段式 native query。
 *
 * 接口 fn_haversine_km 由 PostgreSQL 部署时随 schema 一并注册（见 bitdance_postgresql_schema.sql 2233 行）。
 * H2 内存库测试不能调用该函数 → 搜索逻辑在 Controller 切片测试中通过 mock Service 验证，
 * 实际 SQL 行为留到能连云库时做集成测试覆盖。
 */
@Repository
public class StudioSearchRepository {

    @PersistenceContext
    private EntityManager em;

    public record StudioNearbyRow(
        Long id,
        String studioName,
        String address,
        Long cityId,
        Long businessDistrictId,
        Long coverAssetId,
        BigDecimal distanceKm,
        BigDecimal latitude,
        BigDecimal longitude
    ) {}

    public record SearchParams(
        Long cityId,
        Double latitude,
        Double longitude,
        Double distanceKm,
        String keyword,
        Long danceStyleId,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        String timeSlot,
        Boolean trialAvailable,
        Boolean zeroBasicFriendly,
        Boolean nearMetro,
        int page,
        int pageSize
    ) {}

    public List<StudioNearbyRow> searchNearby(SearchParams p) {
        boolean hasGeo = p.latitude() != null && p.longitude() != null;
        StringBuilder sql = new StringBuilder(hasGeo
            ? """
                SELECT s.id, s.studio_name, s.address, s.city_id, s.business_district_id,
                       s.cover_asset_id, s.latitude, s.longitude,
                       fn_haversine_km(:lat, :lon, s.latitude, s.longitude) AS distance_km
                FROM studio s
                WHERE s.status = 'active'
                """
            : """
                SELECT s.id, s.studio_name, s.address, s.city_id, s.business_district_id,
                       s.cover_asset_id, s.latitude, s.longitude,
                       NULL AS distance_km
                FROM studio s
                WHERE s.status = 'active'
                """);

        if (p.cityId() != null) sql.append(" AND s.city_id = :cityId ");
        if (p.keyword() != null && !p.keyword().isBlank()) {
            sql.append(" AND s.studio_name ILIKE :kw ");
        }
        if (p.danceStyleId() != null) {
            sql.append("""
                 AND EXISTS (
                    SELECT 1 FROM studio_dance_style sds
                    WHERE sds.studio_id = s.id AND sds.dance_style_id = :styleId
                 )
                """);
        }
        if (p.minPrice() != null || p.maxPrice() != null) {
            sql.append("""
                 AND EXISTS (
                    SELECT 1 FROM course c
                    WHERE c.studio_id = s.id AND c.status = 'published'
                      AND (:minPrice IS NULL OR c.price_amount >= :minPrice)
                      AND (:maxPrice IS NULL OR c.price_amount <= :maxPrice)
                 )
                """);
        }
        if (p.timeSlot() != null && !p.timeSlot().isBlank()) {
            sql.append("""
                 AND EXISTS (
                    SELECT 1 FROM course_schedule cs
                    WHERE cs.studio_id = s.id AND cs.status = 'scheduled' AND cs.start_at >= now()
                      AND (
                        (:timeSlot = 'morning' AND EXTRACT(HOUR FROM cs.start_at) < 12)
                        OR (:timeSlot = 'afternoon' AND EXTRACT(HOUR FROM cs.start_at) >= 12 AND EXTRACT(HOUR FROM cs.start_at) < 18)
                        OR (:timeSlot = 'evening' AND EXTRACT(HOUR FROM cs.start_at) >= 18)
                        OR (:timeSlot = 'weekend' AND EXTRACT(ISODOW FROM cs.start_at) IN (6, 7))
                      )
                 )
                """);
        }
        if (Boolean.TRUE.equals(p.trialAvailable())) {
            sql.append("""
                 AND EXISTS (
                    SELECT 1 FROM course c
                    JOIN course_schedule cs ON cs.course_id = c.id
                    WHERE c.studio_id = s.id AND c.status = 'published'
                      AND cs.status = 'scheduled' AND cs.start_at >= now()
                      AND (cs.capacity IS NULL OR cs.booked_count < cs.capacity)
                 )
                """);
        }
        if (Boolean.TRUE.equals(p.zeroBasicFriendly())) {
            sql.append("""
                 AND EXISTS (
                    SELECT 1 FROM course c
                    WHERE c.studio_id = s.id AND c.status = 'published' AND c.zero_basic_friendly = true
                 )
                """);
        }
        if (Boolean.TRUE.equals(p.nearMetro())) {
            sql.append(" AND s.transport_info IS NOT NULL AND btrim(s.transport_info) <> '' ");
        }
        if (hasGeo) {
            // bounding box 粗筛
            double dist = p.distanceKm() == null ? 5.0 : p.distanceKm();
            double dLat = dist / 111.0;
            double dLon = dist / (111.0 * Math.max(0.01, Math.cos(Math.toRadians(p.latitude()))));
            sql.append("""
                 AND s.latitude IS NOT NULL AND s.longitude IS NOT NULL
                 AND s.latitude  BETWEEN :latMin AND :latMax
                 AND s.longitude BETWEEN :lonMin AND :lonMax
                AND fn_haversine_km(:lat, :lon, s.latitude, s.longitude) <= :distMax
                ORDER BY distance_km ASC, s.id ASC
                """);
            Query q = em.createNativeQuery(sql.toString());
            bindGeo(q, p, dLat, dLon, dist);
            bindCommon(q, p);
            q.setFirstResult(Math.max(0, (p.page() - 1) * p.pageSize()));
            q.setMaxResults(p.pageSize());
            return mapRows(q.getResultList());
        }

        sql.append(" ORDER BY s.id DESC ");
        Query q = em.createNativeQuery(sql.toString());
        bindCommon(q, p);
        q.setFirstResult(Math.max(0, (p.page() - 1) * p.pageSize()));
        q.setMaxResults(p.pageSize());
        return mapRows(q.getResultList());
    }

    public long countNearby(SearchParams p) {
        StringBuilder sql = new StringBuilder("""
            SELECT COUNT(*)
            FROM studio s
            WHERE s.status = 'active'
            """);

        if (p.cityId() != null) sql.append(" AND s.city_id = :cityId ");
        if (p.keyword() != null && !p.keyword().isBlank()) {
            sql.append(" AND s.studio_name ILIKE :kw ");
        }
        if (p.danceStyleId() != null) {
            sql.append("""
                 AND EXISTS (
                    SELECT 1 FROM studio_dance_style sds
                    WHERE sds.studio_id = s.id AND sds.dance_style_id = :styleId
                 )
                """);
        }
        if (p.minPrice() != null || p.maxPrice() != null) {
            sql.append("""
                 AND EXISTS (
                    SELECT 1 FROM course c
                    WHERE c.studio_id = s.id AND c.status = 'published'
                      AND (:minPrice IS NULL OR c.price_amount >= :minPrice)
                      AND (:maxPrice IS NULL OR c.price_amount <= :maxPrice)
                 )
                """);
        }
        if (p.timeSlot() != null && !p.timeSlot().isBlank()) {
            sql.append("""
                 AND EXISTS (
                    SELECT 1 FROM course_schedule cs
                    WHERE cs.studio_id = s.id AND cs.status = 'scheduled' AND cs.start_at >= now()
                      AND (
                        (:timeSlot = 'morning' AND EXTRACT(HOUR FROM cs.start_at) < 12)
                        OR (:timeSlot = 'afternoon' AND EXTRACT(HOUR FROM cs.start_at) >= 12 AND EXTRACT(HOUR FROM cs.start_at) < 18)
                        OR (:timeSlot = 'evening' AND EXTRACT(HOUR FROM cs.start_at) >= 18)
                        OR (:timeSlot = 'weekend' AND EXTRACT(ISODOW FROM cs.start_at) IN (6, 7))
                      )
                 )
                """);
        }
        if (Boolean.TRUE.equals(p.trialAvailable())) {
            sql.append("""
                 AND EXISTS (
                    SELECT 1 FROM course c
                    JOIN course_schedule cs ON cs.course_id = c.id
                    WHERE c.studio_id = s.id AND c.status = 'published'
                      AND cs.status = 'scheduled' AND cs.start_at >= now()
                      AND (cs.capacity IS NULL OR cs.booked_count < cs.capacity)
                 )
                """);
        }
        if (Boolean.TRUE.equals(p.zeroBasicFriendly())) {
            sql.append("""
                 AND EXISTS (
                    SELECT 1 FROM course c
                    WHERE c.studio_id = s.id AND c.status = 'published' AND c.zero_basic_friendly = true
                 )
                """);
        }
        if (Boolean.TRUE.equals(p.nearMetro())) {
            sql.append(" AND s.transport_info IS NOT NULL AND btrim(s.transport_info) <> '' ");
        }
        boolean hasGeo = p.latitude() != null && p.longitude() != null;
        double dist = p.distanceKm() == null ? 5.0 : p.distanceKm();
        if (hasGeo) {
            double dLat = dist / 111.0;
            double dLon = dist / (111.0 * Math.max(0.01, Math.cos(Math.toRadians(p.latitude()))));
            sql.append("""
                 AND s.latitude IS NOT NULL AND s.longitude IS NOT NULL
                 AND s.latitude  BETWEEN :latMin AND :latMax
                 AND s.longitude BETWEEN :lonMin AND :lonMax
                 AND fn_haversine_km(:lat, :lon, s.latitude, s.longitude) <= :distMax
                """);
            Query q = em.createNativeQuery(sql.toString());
            bindGeo(q, p, dLat, dLon, dist);
            bindCommon(q, p);
            return ((Number) q.getSingleResult()).longValue();
        }

        Query q = em.createNativeQuery(sql.toString());
        bindCommon(q, p);
        return ((Number) q.getSingleResult()).longValue();
    }

    private void bindCommon(Query q, SearchParams p) {
        if (p.cityId() != null) q.setParameter("cityId", p.cityId());
        if (p.keyword() != null && !p.keyword().isBlank()) {
            q.setParameter("kw", "%" + p.keyword().trim() + "%");
        }
        if (p.danceStyleId() != null) q.setParameter("styleId", p.danceStyleId());
        if (p.minPrice() != null || p.maxPrice() != null) {
            q.setParameter("minPrice", p.minPrice());
            q.setParameter("maxPrice", p.maxPrice());
        }
        if (p.timeSlot() != null && !p.timeSlot().isBlank()) q.setParameter("timeSlot", p.timeSlot());
    }

    private void bindGeo(Query q, SearchParams p, double dLat, double dLon, double dist) {
        q.setParameter("lat", p.latitude());
        q.setParameter("lon", p.longitude());
        q.setParameter("latMin", p.latitude() - dLat);
        q.setParameter("latMax", p.latitude() + dLat);
        q.setParameter("lonMin", p.longitude() - dLon);
        q.setParameter("lonMax", p.longitude() + dLon);
        q.setParameter("distMax", dist);
    }

    @SuppressWarnings("unchecked")
    private List<StudioNearbyRow> mapRows(List<?> rows) {
        List<StudioNearbyRow> out = new ArrayList<>(rows.size());
        for (Object row : rows) {
            Object[] arr = (Object[]) row;
            out.add(new StudioNearbyRow(
                ((Number) arr[0]).longValue(),
                (String) arr[1],
                (String) arr[2],
                arr[3] == null ? null : ((Number) arr[3]).longValue(),
                arr[4] == null ? null : ((Number) arr[4]).longValue(),
                arr[5] == null ? null : ((Number) arr[5]).longValue(),
                arr[8] == null ? null : toBig(arr[8]),
                arr[6] == null ? null : toBig(arr[6]),
                arr[7] == null ? null : toBig(arr[7])
            ));
        }
        return out;
    }

    private BigDecimal toBig(Object o) {
        if (o instanceof BigDecimal b) return b;
        return new BigDecimal(o.toString());
    }
}
