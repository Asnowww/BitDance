-- BitDance development seed data.
-- This file targets the current Spring/JPA database shape and uses the full schema file as business reference.
-- It is intentionally idempotent so local/cloud dev databases can be reseeded without duplicate-key failures.

BEGIN;

SET search_path TO bitdance, public;

-- The schema file defines fn_haversine_km; the extra numeric overload matches the current studio latitude/longitude column type.
CREATE OR REPLACE FUNCTION fn_haversine_km(
    p_lat1 double precision,
    p_lon1 double precision,
    p_lat2 double precision,
    p_lon2 double precision
)
RETURNS numeric
LANGUAGE sql
IMMUTABLE
AS $$
    SELECT ROUND(
        (
            6371 * acos(
                LEAST(
                    1.0,
                    GREATEST(
                        -1.0,
                        cos(radians(p_lat1)) * cos(radians(p_lat2)) * cos(radians(p_lon2) - radians(p_lon1))
                        + sin(radians(p_lat1)) * sin(radians(p_lat2))
                    )
                )
            )
        )::numeric,
        3
    );
$$;

-- Current JPA native query passes numeric studio coordinates, so this wrapper prevents function-resolution errors.
CREATE OR REPLACE FUNCTION fn_haversine_km(
    p_lat1 double precision,
    p_lon1 double precision,
    p_lat2 numeric,
    p_lon2 numeric
)
RETURNS numeric
LANGUAGE sql
IMMUTABLE
AS $$
    SELECT fn_haversine_km(p_lat1, p_lon1, p_lat2::double precision, p_lon2::double precision);
$$;

DO $$
DECLARE
    i integer;
    seed_id bigint;
    next_user_id bigint;
    style_names text[] := ARRAY['HipHop', 'Jazz', 'Breaking', 'Locking', 'Popping', 'Kpop', 'Waacking', 'Urban', 'House', '中国舞'];
    anchor_lats numeric[] := ARRAY[39.984000, 39.995000, 39.828000, 39.903000, 40.067000, 39.914000, 39.804000, 40.074000, 39.735000, 39.941000];
    anchor_lons numeric[] := ARRAY[116.316000, 116.469000, 116.289000, 116.654000, 116.333000, 116.190000, 116.506000, 116.562000, 116.143000, 116.101000];
    lat_jitter numeric;
    lon_jitter numeric;
    seeded_lat numeric;
    seeded_lon numeric;
BEGIN
    FOR i IN 1..10 LOOP
        seed_id := 100000 + i;
        next_user_id := 100000 + CASE WHEN i = 10 THEN 1 ELSE i + 1 END;
        -- 舞室定位随机化：使用北京多片区锚点加小幅抖动，避开故宫周边中心区。
        lat_jitter := (((i * 37) % 7) - 3) * 0.003500;
        lon_jitter := (((i * 53) % 7) - 3) * 0.004500;
        seeded_lat := anchor_lats[i] + lat_jitter;
        seeded_lon := anchor_lons[i] + lon_jitter;

        -- app_user: 10 seed accounts for login, reviews, orders, and social content.
        INSERT INTO app_user (id, created_at, updated_at, open_id, phone, status, union_id)
        VALUES (seed_id, now(), now(), 'seed-open-' || i, '1390000' || lpad(i::text, 4, '0'), 'ACTIVE', 'seed-union-' || i)
        ON CONFLICT (id) DO UPDATE SET
            updated_at = EXCLUDED.updated_at,
            phone = EXCLUDED.phone,
            status = EXCLUDED.status;

        -- user_role_binding: role mix gives user, coach, merchant, and platform paths usable test identities.
        INSERT INTO user_role_binding (id, created_at, updated_at, role, status, user_id)
        VALUES (
            seed_id,
            now(),
            now(),
            CASE
                WHEN i = 1 THEN 'PLATFORM_ADMIN'
                WHEN i BETWEEN 2 AND 4 THEN 'STUDIO_ADMIN'
                WHEN i BETWEEN 5 AND 7 THEN 'COACH'
                ELSE 'USER'
            END,
            'ACTIVE',
            seed_id
        )
        ON CONFLICT (id) DO UPDATE SET
            updated_at = EXCLUDED.updated_at,
            role = EXCLUDED.role,
            status = EXCLUDED.status,
            user_id = EXCLUDED.user_id;

        -- dance_style: fixed ids are used by all dependent catalog, practice, and growth seed rows.
        INSERT INTO dance_style (id, code, name_zh)
        VALUES (i, lower(replace(style_names[i], ' ', '_')), style_names[i])
        ON CONFLICT (id) DO UPDATE SET
            code = EXCLUDED.code,
            name_zh = EXCLUDED.name_zh;

        -- user_profile: visible H5 profile data for every seed account.
        INSERT INTO user_profile (
            user_id, avatar_asset_id, bio, birthday, city_id, created_at, current_level, gender,
            learning_goal, nickname, updated_at
        )
        VALUES (
            seed_id, NULL, '模拟舞者资料 ' || i, DATE '1995-01-01' + i, 1, now(),
            CASE WHEN i % 3 = 0 THEN 'advanced' WHEN i % 2 = 0 THEN 'intermediate' ELSE 'beginner' END,
            CASE WHEN i % 3 = 0 THEN 'unknown' WHEN i % 2 = 0 THEN 'female' ELSE 'male' END,
            '每周稳定练舞并记录成长', '模拟舞者' || i, now()
        )
        ON CONFLICT (user_id) DO UPDATE SET
            bio = EXCLUDED.bio,
            nickname = EXCLUDED.nickname,
            updated_at = EXCLUDED.updated_at;

        -- privacy_setting: each seed user gets a complete visibility row.
        INSERT INTO privacy_setting (user_id, content_visibility, growth_visibility, practice_visibility, profile_visibility)
        VALUES (seed_id, 'public', 'followers', 'public', 'public')
        ON CONFLICT (user_id) DO UPDATE SET
            content_visibility = EXCLUDED.content_visibility,
            growth_visibility = EXCLUDED.growth_visibility,
            practice_visibility = EXCLUDED.practice_visibility,
            profile_visibility = EXCLUDED.profile_visibility;

        -- user_dance_preference: one primary style preference per seed user.
        INSERT INTO user_dance_preference (id, dance_style_id, is_primary, preference_weight, skill_level, user_id)
        VALUES (seed_id, i, true, 1.00 + i / 10.0, CASE WHEN i % 2 = 0 THEN 'intermediate' ELSE 'beginner' END, seed_id)
        ON CONFLICT (id) DO UPDATE SET
            dance_style_id = EXCLUDED.dance_style_id,
            is_primary = EXCLUDED.is_primary,
            preference_weight = EXCLUDED.preference_weight,
            skill_level = EXCLUDED.skill_level,
            user_id = EXCLUDED.user_id;

        -- studio: 10 active Beijing-area studios so public discovery returns real backend data.
        INSERT INTO studio (
            id, address, brand_name, business_district_id, city_id, claim_status, contact_phone,
            cover_asset_id, created_at, geo_hash, intro, latitude, longitude, source_type,
            status, studio_code, studio_name, transport_info
        )
        VALUES (
            seed_id,
            '北京市朝阳区舞蹈街 ' || i || ' 号',
            'BitDance 模拟品牌',
            i,
            1,
            CASE WHEN i % 3 = 0 THEN 'claimed' ELSE 'unclaimed' END,
            '010-8800' || lpad(i::text, 4, '0'),
            NULL,
            now(),
            'wx4g0' || i,
            '用于开发联调的模拟舞室，包含课程、评价、约练和活动数据。',
            seeded_lat,
            seeded_lon,
            'dev_seed',
            'active',
            'SEED_STUDIO_' || lpad(i::text, 3, '0'),
            'BitDance 模拟舞室 ' || i,
            '地铁步行约 ' || (5 + (i % 6)) || ' 分钟'
        )
        ON CONFLICT (id) DO UPDATE SET
            studio_name = EXCLUDED.studio_name,
            status = EXCLUDED.status,
            latitude = EXCLUDED.latitude,
            longitude = EXCLUDED.longitude,
            source_type = EXCLUDED.source_type;

        -- studio_dance_style: one primary style per studio for filtering and detail pages.
        INSERT INTO studio_dance_style (dance_style_id, studio_id, is_primary)
        VALUES (i, seed_id, true)
        ON CONFLICT (dance_style_id, studio_id) DO UPDATE SET
            is_primary = EXCLUDED.is_primary;

        -- coach: one coach account per studio to support catalog and workshop flows.
        INSERT INTO coach (
            id, available_time_slots, avg_rating, certification_status, cover_asset_id, display_name,
            home_studio_id, intro, teaching_style, user_id
        )
        VALUES (
            seed_id,
            '[{"weekday":"Sat","time":"14:00-16:00"}]'::jsonb,
            4.0 + i / 10.0,
            'approved',
            NULL,
            '模拟教练 ' || i,
            seed_id,
            '擅长零基础入门和舞感训练。',
            '耐心拆解动作，重视音乐性。',
            seed_id
        )
        ON CONFLICT (id) DO UPDATE SET
            display_name = EXCLUDED.display_name,
            certification_status = EXCLUDED.certification_status,
            home_studio_id = EXCLUDED.home_studio_id,
            user_id = EXCLUDED.user_id;

        -- coach_dance_style: one style binding per seed coach.
        INSERT INTO coach_dance_style (coach_id, dance_style_id, proficiency_level)
        VALUES (seed_id, i, CASE WHEN i % 2 = 0 THEN 'senior' ELSE 'intermediate' END)
        ON CONFLICT (coach_id, dance_style_id) DO UPDATE SET
            proficiency_level = EXCLUDED.proficiency_level;

        -- coach_certification_application: mirrors certification review states for operations pages.
        INSERT INTO coach_certification_application (
            id, application_status, application_type, created_at, remark, review_remark,
            reviewed_at, reviewed_by_user_id, user_id
        )
        VALUES (
            seed_id,
            CASE WHEN i % 4 = 0 THEN 'pending' ELSE 'approved' END,
            'independent',
            now(),
            '模拟教练认证申请 ' || i,
            '开发种子数据审核通过',
            now(),
            100001,
            seed_id
        )
        ON CONFLICT (id) DO UPDATE SET
            application_status = EXCLUDED.application_status,
            remark = EXCLUDED.remark,
            user_id = EXCLUDED.user_id;

        -- studio_coach_relation: connects seed studios and coaches for merchant-side operations.
        INSERT INTO studio_coach_relation (
            id, approved_by_user_id, coach_id, created_at, effective_from, effective_to,
            invited_by_user_id, permission_scope, relation_status, relation_type,
            settlement_mode, settlement_ratio, studio_id
        )
        VALUES (
            seed_id, 100001, seed_id, now(), current_date - i, NULL, 100002,
            '{"course":true,"workshop":true}'::jsonb, 'active',
            CASE WHEN i % 2 = 0 THEN 'signed' ELSE 'full_time' END,
            'ratio', 50.00, seed_id
        )
        ON CONFLICT (id) DO UPDATE SET
            relation_status = EXCLUDED.relation_status,
            coach_id = EXCLUDED.coach_id,
            studio_id = EXCLUDED.studio_id;

        -- course: published classes attached to every seed studio.
        INSERT INTO course (
            id, coach_id, course_name, course_type, cover_asset_id, dance_style_id, description,
            difficulty_level, duration_minutes, intensity_level, price_amount, status, studio_id,
            target_audience, zero_basic_friendly
        )
        VALUES (
            seed_id, seed_id, style_names[i] || ' 基础课 ' || i, 'regular', NULL, i,
            '模拟课程，适合联调课程详情、排期和预约流程。',
            CASE WHEN i % 3 = 0 THEN 'advanced' WHEN i % 2 = 0 THEN 'intermediate' ELSE 'beginner' END,
            60 + i * 5,
            CASE WHEN i % 2 = 0 THEN 'medium' ELSE 'light' END,
            99 + i * 20,
            'published',
            seed_id,
            ARRAY['adult','beginner']::text[],
            i % 2 = 1
        )
        ON CONFLICT (id) DO UPDATE SET
            course_name = EXCLUDED.course_name,
            status = EXCLUDED.status,
            price_amount = EXCLUDED.price_amount;

        -- course_schedule: one future schedule per seed course for trial booking.
        INSERT INTO course_schedule (
            id, booked_count, capacity, classroom_name, coach_id, course_id, end_at, start_at, status, studio_id
        )
        VALUES (
            seed_id, i % 5, 20 + i, 'A' || i, seed_id, seed_id,
            now() + (i + 7) * interval '1 day' + interval '2 hours',
            now() + (i + 7) * interval '1 day',
            'scheduled',
            seed_id
        )
        ON CONFLICT (id) DO UPDATE SET
            start_at = EXCLUDED.start_at,
            end_at = EXCLUDED.end_at,
            status = EXCLUDED.status;

        -- trial_booking: one booking per seed course/schedule for user-side order pages.
        INSERT INTO trial_booking (
            id, attended_at, booking_note, booking_status, cancel_reason, canceled_at, confirmed_at,
            confirmed_by_user_id, contact_phone, course_id, course_schedule_id, created_at, studio_id, user_id
        )
        VALUES (
            seed_id, NULL, '想先体验一节基础课', CASE WHEN i % 3 = 0 THEN 'confirmed' ELSE 'pending' END,
            NULL, NULL, CASE WHEN i % 3 = 0 THEN now() ELSE NULL END, 100002,
            '1390000' || lpad(i::text, 4, '0'), seed_id, seed_id, now(), seed_id, next_user_id
        )
        ON CONFLICT (id) DO UPDATE SET
            booking_status = EXCLUDED.booking_status,
            contact_phone = EXCLUDED.contact_phone;

        -- favorite: every seed user favorites a different seed studio.
        INSERT INTO favorite (id, created_at, target_id, target_type, user_id)
        VALUES (seed_id, now(), seed_id, 'studio', seed_id)
        ON CONFLICT (id) DO UPDATE SET
            target_id = EXCLUDED.target_id,
            target_type = EXCLUDED.target_type,
            user_id = EXCLUDED.user_id;

        -- review: public studio reviews for list, summary, and detail pages.
        INSERT INTO review (
            id, content_text, created_at, helpful_count, is_pinned, is_verified, overall_score,
            published_at, review_status, risk_level, target_id, target_type, user_id,
            verified_source_ref_id, verified_source_type, weight_factor
        )
        VALUES (
            seed_id, '环境不错，老师讲得很细，适合开发联调展示。', now(), i * 2,
            i = 1, i % 2 = 0, 3.50 + (i % 3) * 0.50, now(), 'published', 0,
            seed_id, 'studio', next_user_id, seed_id, 'trial_booking', 1.000
        )
        ON CONFLICT (id) DO UPDATE SET
            content_text = EXCLUDED.content_text,
            overall_score = EXCLUDED.overall_score,
            review_status = EXCLUDED.review_status;

        -- review_dimension_score: one dimension row per review keeps review radar data populated.
        INSERT INTO review_dimension_score (id, dimension_code, dimension_name, review_id, score)
        VALUES (seed_id, 'environment', '环境', seed_id, (i % 5) + 1)
        ON CONFLICT (id) DO UPDATE SET
            review_id = EXCLUDED.review_id,
            score = EXCLUDED.score;

        -- review_reply: official replies make merchant/coach reply views non-empty.
        INSERT INTO review_reply (id, created_at, is_official, replier_user_id, reply_content, review_id)
        VALUES (seed_id, now(), true, seed_id, '感谢反馈，欢迎继续来练舞。', seed_id)
        ON CONFLICT (id) DO UPDATE SET
            reply_content = EXCLUDED.reply_content,
            review_id = EXCLUDED.review_id;

        -- review_appeal: appeal rows cover pending and handled admin states.
        INSERT INTO review_appeal (
            id, appeal_reason, appeal_status, appellant_user_id, created_at, evidence_note,
            review_id, review_remark, reviewed_at, reviewed_by_user_id
        )
        VALUES (
            seed_id, '模拟评价申诉原因 ' || i,
            CASE WHEN i % 3 = 0 THEN 'approved' ELSE 'pending' END,
            seed_id, now(), '截图和订单记录', seed_id, '平台模拟审核备注', now(), 100001
        )
        ON CONFLICT (id) DO UPDATE SET
            appeal_status = EXCLUDED.appeal_status,
            appeal_reason = EXCLUDED.appeal_reason;

        -- practice_post: published practice posts for square, detail, and join request flows.
        INSERT INTO practice_post (
            id, cancel_limit_hours, city_id, created_at, creator_user_id, current_people_count,
            dance_style_id, description, end_at, expected_people_max, expected_people_min,
            expires_at, geo_hash, latitude, location_address, location_name, longitude,
            post_status, skill_level, start_at, studio_id
        )
        VALUES (
            seed_id, 2, 1, now(), seed_id, 1 + (i % 3), i,
            '周末一起练 ' || style_names[i],
            now() + (i + 4) * interval '1 day' + interval '2 hours',
            4, 2,
            now() + (i + 4) * interval '1 day' - interval '2 hours',
            'wx4g0' || i,
            39.900000 + i * 0.010000,
            '北京市朝阳区舞蹈街 ' || i || ' 号',
            'BitDance 模拟舞室 ' || i,
            116.300000 + i * 0.010000,
            'published',
            CASE WHEN i % 2 = 0 THEN 'intermediate' ELSE 'beginner' END,
            now() + (i + 4) * interval '1 day',
            seed_id
        )
        ON CONFLICT (id) DO UPDATE SET
            post_status = EXCLUDED.post_status,
            start_at = EXCLUDED.start_at,
            end_at = EXCLUDED.end_at;

        -- practice_join_request: one applicant row per practice post.
        INSERT INTO practice_join_request (
            id, acted_at, acted_by_user_id, applicant_user_id, created_at, join_message,
            join_status, practice_post_id
        )
        VALUES (
            seed_id, CASE WHEN i % 2 = 0 THEN now() ELSE NULL END, seed_id, next_user_id, now(),
            '想一起练习，时间合适。', CASE WHEN i % 2 = 0 THEN 'accepted' ELSE 'pending' END, seed_id
        )
        ON CONFLICT (id) DO UPDATE SET
            join_status = EXCLUDED.join_status,
            applicant_user_id = EXCLUDED.applicant_user_id;

        -- buddy_relation: ten user-pair relations derived from practice posts.
        INSERT INTO buddy_relation (id, created_at, relation_status, source_practice_post_id, user_id_high, user_id_low)
        VALUES (
            seed_id, now(), 'active', seed_id,
            CASE WHEN i = 10 THEN 100010 ELSE 100001 + i END,
            CASE WHEN i = 10 THEN 100001 ELSE 100000 + i END
        )
        ON CONFLICT (id) DO UPDATE SET
            relation_status = EXCLUDED.relation_status,
            source_practice_post_id = EXCLUDED.source_practice_post_id;

        -- practice_rating: post-session ratings for buddy recommendation signals.
        INSERT INTO practice_rating (
            id, created_at, friendliness_score, from_user_id, practice_post_id, punctuality_score,
            rating_comment, skill_match_score, to_user_id
        )
        VALUES (
            seed_id, now(), (i % 5) + 1, seed_id, seed_id, ((i + 1) % 5) + 1,
            '练习体验不错，节奏匹配。', ((i + 2) % 5) + 1, next_user_id
        )
        ON CONFLICT (id) DO UPDATE SET
            friendliness_score = EXCLUDED.friendliness_score,
            punctuality_score = EXCLUDED.punctuality_score,
            skill_match_score = EXCLUDED.skill_match_score;

        -- growth_checkin: growth timeline and statistics source rows.
        INSERT INTO growth_checkin (
            id, checkin_at, course_schedule_id, created_at, dance_style_id, duration_minutes,
            feeling_text, is_public, practice_post_id, studio_id, user_id
        )
        VALUES (
            seed_id, now() - i * interval '1 day', seed_id, now(), i, 45 + i * 5,
            '今天练习状态不错，继续保持。', true, seed_id, seed_id, seed_id
        )
        ON CONFLICT (id) DO UPDATE SET
            checkin_at = EXCLUDED.checkin_at,
            duration_minutes = EXCLUDED.duration_minutes;

        -- growth_goal: active weekly/monthly goals for the seed users.
        INSERT INTO growth_goal (
            id, created_at, current_minutes, current_times, end_date, goal_period, goal_status,
            start_date, target_minutes, target_times, user_id
        )
        VALUES (
            seed_id, now(), 60 + i * 10, i, current_date + 7,
            CASE WHEN i % 2 = 0 THEN 'monthly' ELSE 'weekly' END,
            'active', current_date, 300 + i * 30, 3 + i, seed_id
        )
        ON CONFLICT (id) DO UPDATE SET
            current_minutes = EXCLUDED.current_minutes,
            current_times = EXCLUDED.current_times,
            goal_status = EXCLUDED.goal_status;

        -- badge_definition: achievement definitions used by growth badges.
        INSERT INTO badge_definition (
            id, badge_code, badge_name, created_at, description, icon_asset_id, rule_config, rule_type, status
        )
        VALUES (
            seed_id, 'SEED_BADGE_' || lpad(i::text, 3, '0'), '模拟徽章 ' || i, now(),
            '用于成长体系联调的徽章定义。', NULL,
            jsonb_build_object('threshold', i), 'checkin_count', 'active'
        )
        ON CONFLICT (id) DO UPDATE SET
            badge_name = EXCLUDED.badge_name,
            rule_config = EXCLUDED.rule_config,
            status = EXCLUDED.status;

        -- growth_badge: every seed user owns one badge.
        INSERT INTO growth_badge (id, awarded_at, badge_id, source_ref_id, source_type, user_id)
        VALUES (seed_id, now() - i * interval '2 days', seed_id, seed_id, 'growth_checkin', seed_id)
        ON CONFLICT (id) DO UPDATE SET
            badge_id = EXCLUDED.badge_id,
            user_id = EXCLUDED.user_id;

        -- growth_work: portfolio items for coach/user home pages.
        INSERT INTO growth_work (
            id, cover_asset_id, created_at, dance_style_id, is_public, user_id, work_description, work_title
        )
        VALUES (seed_id, NULL, now(), i, true, seed_id, '模拟作品描述 ' || i, '练舞作品 ' || i)
        ON CONFLICT (id) DO UPDATE SET
            work_title = EXCLUDED.work_title,
            work_description = EXCLUDED.work_description;

        -- workshop: published approved workshops for activity discovery and order flows.
        INSERT INTO workshop (
            id, address, audit_status, city_id, coach_id, cover_asset_id, created_at, creator_user_id,
            dance_style_id, intro, latitude, location_name, longitude, max_people, min_people,
            price_amount, publish_status, signup_deadline, source_type, studio_id, workshop_name
        )
        VALUES (
            seed_id, '北京市朝阳区活动空间 ' || i || ' 号', 'approved', 1, seed_id, NULL, now(),
            seed_id, i, '模拟 Workshop 活动介绍。', 39.910000 + i * 0.010000,
            'BitDance 活动空间 ' || i, 116.310000 + i * 0.010000, 30, 6,
            199 + i * 30, 'published', now() + (i + 2) * interval '1 day',
            CASE WHEN i % 2 = 0 THEN 'coach' ELSE 'studio' END, seed_id, style_names[i] || ' 周末工作坊'
        )
        ON CONFLICT (id) DO UPDATE SET
            workshop_name = EXCLUDED.workshop_name,
            audit_status = EXCLUDED.audit_status,
            publish_status = EXCLUDED.publish_status;

        -- workshop_session: one session per workshop for booking.
        INSERT INTO workshop_session (
            id, capacity, checkin_count, end_at, session_name, session_status, sold_count, start_at, workshop_id
        )
        VALUES (
            seed_id, 30, i % 3,
            now() + (i + 10) * interval '1 day' + interval '3 hours',
            '第 ' || i || ' 场',
            'open',
            i,
            now() + (i + 10) * interval '1 day',
            seed_id
        )
        ON CONFLICT (id) DO UPDATE SET
            session_status = EXCLUDED.session_status,
            sold_count = EXCLUDED.sold_count;

        -- workshop_order: ten orders cover unpaid and paid states.
        INSERT INTO workshop_order (
            id, amount_paid, amount_payable, canceled_at, completed_at, created_at, order_no,
            order_status, paid_at, payment_txn_no, refund_reason, refunded_at, user_id,
            workshop_id, workshop_session_id
        )
        VALUES (
            seed_id,
            CASE WHEN i % 2 = 0 THEN 199 + i * 30 ELSE 0 END,
            199 + i * 30,
            NULL,
            NULL,
            now(),
            'SEED_ORDER_' || lpad(i::text, 6, '0'),
            CASE WHEN i % 2 = 0 THEN 'paid' ELSE 'pending_payment' END,
            CASE WHEN i % 2 = 0 THEN now() ELSE NULL END,
            CASE WHEN i % 2 = 0 THEN 'SEED_PAY_' || i ELSE NULL END,
            NULL,
            NULL,
            next_user_id,
            seed_id,
            seed_id
        )
        ON CONFLICT (id) DO UPDATE SET
            order_status = EXCLUDED.order_status,
            amount_paid = EXCLUDED.amount_paid,
            user_id = EXCLUDED.user_id;

        -- workshop_checkin: status strings stay within the current varchar(16) column length.
        INSERT INTO workshop_checkin (
            id, checked_in_at, checked_in_by_user_id, checkin_code, checkin_status, created_at,
            workshop_order_id, workshop_session_id
        )
        VALUES (
            seed_id, now(), seed_id, 'SEED-CHECKIN-' || lpad(i::text, 3, '0'),
            CASE WHEN i % 3 = 0 THEN 'manual_checked' ELSE 'checked_in' END,
            now(), seed_id, seed_id
        )
        ON CONFLICT (id) DO UPDATE SET
            checkin_status = EXCLUDED.checkin_status,
            checkin_code = EXCLUDED.checkin_code;

        -- topic_tag: system and creator tags for community content.
        INSERT INTO topic_tag (
            id, created_at, creator_user_id, description, is_system, status, topic_code, topic_name
        )
        VALUES (
            seed_id, now(), seed_id, '模拟社区话题 ' || i, i <= 3, 'active',
            'seed_topic_' || i, '模拟话题' || i
        )
        ON CONFLICT (id) DO UPDATE SET
            topic_name = EXCLUDED.topic_name,
            status = EXCLUDED.status;

        -- content_post: public community posts linked to styles, courses, workshops, and city.
        INSERT INTO content_post (
            id, author_user_id, city_id, content_text, created_at, dance_style_id, geo_hash,
            latitude, location_name, longitude, post_status, post_type, published_at,
            related_course_id, related_workshop_id, visibility
        )
        VALUES (
            seed_id, seed_id, 1, '今天在模拟舞室打卡，动作终于顺了。', now(), i,
            'wx4g0' || i, 39.900000 + i * 0.010000, 'BitDance 模拟舞室 ' || i,
            116.300000 + i * 0.010000, 'published',
            CASE WHEN i % 2 = 0 THEN 'experience' ELSE 'note' END,
            now(), seed_id, seed_id, 'public'
        )
        ON CONFLICT (id) DO UPDATE SET
            content_text = EXCLUDED.content_text,
            post_status = EXCLUDED.post_status;

        -- content_post_topic: one topic per content post.
        INSERT INTO content_post_topic (content_post_id, topic_tag_id)
        VALUES (seed_id, seed_id)
        ON CONFLICT (content_post_id, topic_tag_id) DO NOTHING;

        -- content_comment: one top-level comment per content post.
        INSERT INTO content_comment (
            id, comment_status, comment_text, content_post_id, created_at, parent_comment_id,
            reply_to_user_id, user_id
        )
        VALUES (seed_id, 'published', '这个动作很有感觉，收藏了。', seed_id, now(), NULL, seed_id, next_user_id)
        ON CONFLICT (id) DO UPDATE SET
            comment_text = EXCLUDED.comment_text,
            comment_status = EXCLUDED.comment_status;

        -- content_like: one like relation per content post.
        INSERT INTO content_like (content_post_id, user_id, created_at)
        VALUES (seed_id, next_user_id, now())
        ON CONFLICT (content_post_id, user_id) DO NOTHING;

        -- follow_relation: ten directed follow relationships.
        INSERT INTO follow_relation (followee_user_id, follower_user_id, created_at)
        VALUES (next_user_id, seed_id, now())
        ON CONFLICT (followee_user_id, follower_user_id) DO NOTHING;

        -- notification: messages for the H5 notification center.
        INSERT INTO notification (
            id, category, content, created_at, is_read, notice_type, read_at, sent_at,
            target_id, target_type, title, user_id
        )
        VALUES (
            seed_id, CASE WHEN i % 2 = 0 THEN 'practice' ELSE 'system' END,
            '这是一条开发模拟通知 ' || i, now(), i % 2 = 0, 'seed_notice',
            CASE WHEN i % 2 = 0 THEN now() ELSE NULL END, now(),
            seed_id, 'practice_post', '模拟通知 ' || i, seed_id
        )
        ON CONFLICT (id) DO UPDATE SET
            title = EXCLUDED.title,
            is_read = EXCLUDED.is_read;

        -- report_ticket: report-workbench rows for admin moderation.
        INSERT INTO report_ticket (
            id, created_at, handle_result, handled_at, handled_by_user_id, reason_code,
            reason_detail, report_status, reporter_user_id, target_id, target_type
        )
        VALUES (
            seed_id, now(), CASE WHEN i % 2 = 0 THEN '已提醒用户规范发布' ELSE NULL END,
            CASE WHEN i % 2 = 0 THEN now() ELSE NULL END,
            CASE WHEN i % 2 = 0 THEN 100001 ELSE NULL END,
            'spam', '模拟举报详情 ' || i,
            CASE WHEN i % 2 = 0 THEN 'closed' ELSE 'pending' END,
            seed_id, seed_id, 'content_post'
        )
        ON CONFLICT (id) DO UPDATE SET
            report_status = EXCLUDED.report_status,
            reason_detail = EXCLUDED.reason_detail;

        -- audit_log: operational audit rows for admin traceability.
        INSERT INTO audit_log (
            id, action_code, actor_role_code, actor_user_id, after_data, before_data,
            created_at, target_id, target_type, user_agent
        )
        VALUES (
            seed_id, 'SEED_UPSERT', 'PLATFORM_ADMIN', 100001,
            jsonb_build_object('seeded', true, 'row', i),
            jsonb_build_object('seeded', false),
            now(), seed_id, 'studio', 'dev-seed-script'
        )
        ON CONFLICT (id) DO UPDATE SET
            after_data = EXCLUDED.after_data,
            action_code = EXCLUDED.action_code;

        -- studio_claim: merchant claim rows for studio audit flows.
        INSERT INTO studio_claim (
            id, applicant_user_id, business_license_asset_id, claim_status, claim_type, created_at,
            review_remark, reviewed_at, reviewed_by_user_id, studio_id, submitted_remark
        )
        VALUES (
            seed_id, seed_id, NULL, CASE WHEN i % 3 = 0 THEN 'approved' ELSE 'pending' END,
            'owner_claim', now(), '模拟认领审核备注', now(), 100001, seed_id,
            '我是该舞室运营人员。'
        )
        ON CONFLICT (id) DO UPDATE SET
            claim_status = EXCLUDED.claim_status,
            submitted_remark = EXCLUDED.submitted_remark;
    END LOOP;
END;
$$;

-- Keep identity sequences ahead of explicit seed ids so later application inserts do not collide.
DO $$
DECLARE
    t text;
    seq_name text;
    id_tables text[] := ARRAY[
        'app_user','audit_log','badge_definition','buddy_relation','coach',
        'coach_certification_application','content_comment','content_post','course',
        'course_schedule','favorite','growth_badge','growth_checkin','growth_goal',
        'growth_work','notification','practice_join_request','practice_post',
        'practice_rating','report_ticket','review','review_appeal',
        'review_dimension_score','review_reply','studio','studio_claim',
        'studio_coach_relation','topic_tag','trial_booking','user_dance_preference',
        'user_role_binding','workshop','workshop_checkin','workshop_order','workshop_session'
    ];
BEGIN
    FOREACH t IN ARRAY id_tables LOOP
        SELECT pg_get_serial_sequence('bitdance.' || t, 'id') INTO seq_name;
        IF seq_name IS NOT NULL THEN
            EXECUTE format(
                'SELECT setval(%L, (SELECT GREATEST(COALESCE(MAX(id), 1), 1) FROM bitdance.%I), true)',
                seq_name,
                t
            );
        END IF;
    END LOOP;
END;
$$;

COMMIT;
