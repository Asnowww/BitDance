-- BitDance M2 review baseline seed data.
-- Run after dev_seed_mock_data.sql and seed_m1_m2_media_data.sql.
-- The script is idempotent: fixed review ids plus ON CONFLICT make repeated runs safe.

BEGIN;

SET search_path TO bitdance, public;

-- M2 当前学员口径：优先使用开发登录账号；缺失时退回 USER 种子号，保证“我的评价”页有可测数据。
DO $$
DECLARE
    current_student_id bigint;
    admin_user_id bigint;
    first_course_id bigint;
BEGIN
    SELECT COALESCE(
        (SELECT id FROM app_user WHERE phone = '18511695975' ORDER BY id LIMIT 1),
        (SELECT user_id FROM user_role_binding WHERE role = 'USER' ORDER BY user_id LIMIT 1),
        (SELECT id FROM app_user ORDER BY id LIMIT 1)
    )
    INTO current_student_id;

    SELECT COALESCE(
        (SELECT user_id FROM user_role_binding WHERE role = 'PLATFORM_ADMIN' ORDER BY user_id LIMIT 1),
        current_student_id
    )
    INTO admin_user_id;

    SELECT id INTO first_course_id FROM course ORDER BY id LIMIT 1;

    -- M2 公开舞室评价：每家舞室至少有一条当前学员评价，支撑舞室详情聚合评分、维度条和带图评价。
    WITH ranked AS (
        SELECT
            s.id AS target_id,
            row_number() OVER (ORDER BY s.id) AS rn
        FROM studio s
        WHERE s.status = 'active'
    )
    INSERT INTO review (
        id, user_id, target_type, target_id, overall_score, content_text,
        verified_source_type, verified_source_ref_id, is_verified, weight_factor,
        review_status, risk_level, helpful_count, is_pinned, published_at, created_at, updated_at
    )
    SELECT
        220000000 + target_id,
        current_student_id,
        'studio',
        target_id,
        (4.10 + ((rn % 4) * 0.20))::numeric(4, 2),
        'M2 基线评价：交通、环境和练习氛围都有可观察数据，适合前端聚合面板验收。',
        'trial_booking',
        target_id,
        rn % 2 = 0,
        CASE WHEN rn % 2 = 0 THEN 1.200 ELSE 1.000 END,
        'published',
        0,
        rn * 3,
        rn = 1,
        now() - (rn || ' days')::interval,
        now() - (rn || ' days')::interval,
        now()
    FROM ranked
    ON CONFLICT (id) DO UPDATE SET
        user_id = EXCLUDED.user_id,
        overall_score = EXCLUDED.overall_score,
        content_text = EXCLUDED.content_text,
        is_verified = EXCLUDED.is_verified,
        weight_factor = EXCLUDED.weight_factor,
        review_status = EXCLUDED.review_status,
        risk_level = EXCLUDED.risk_level,
        helpful_count = EXCLUDED.helpful_count,
        updated_at = now();

    -- M2 公开教练评价：每位老师至少有一条当前学员评价，支撑老师详情页 ReviewAggregatePanel。
    WITH ranked AS (
        SELECT
            c.id AS target_id,
            row_number() OVER (ORDER BY c.id) AS rn
        FROM coach c
    )
    INSERT INTO review (
        id, user_id, target_type, target_id, overall_score, content_text,
        verified_source_type, verified_source_ref_id, is_verified, weight_factor,
        review_status, risk_level, helpful_count, is_pinned, published_at, created_at, updated_at
    )
    SELECT
        221000000 + target_id,
        current_student_id,
        'coach',
        target_id,
        (4.00 + ((rn % 5) * 0.18))::numeric(4, 2),
        'M2 基线评价：老师讲解、纠错和零基础友好程度都有维度分，便于老师详情验收。',
        NULL,
        NULL,
        rn % 3 = 0,
        CASE WHEN rn % 3 = 0 THEN 1.150 ELSE 1.000 END,
        'published',
        0,
        rn * 2,
        false,
        now() - (rn || ' hours')::interval,
        now() - (rn || ' hours')::interval,
        now()
    FROM ranked
    ON CONFLICT (id) DO UPDATE SET
        user_id = EXCLUDED.user_id,
        overall_score = EXCLUDED.overall_score,
        content_text = EXCLUDED.content_text,
        is_verified = EXCLUDED.is_verified,
        weight_factor = EXCLUDED.weight_factor,
        review_status = EXCLUDED.review_status,
        risk_level = EXCLUDED.risk_level,
        helpful_count = EXCLUDED.helpful_count,
        updated_at = now();

    -- M2 我的评价状态样例：补一条课程待审核记录，确保学员端能看到 pending 状态说明。
    IF first_course_id IS NOT NULL THEN
        INSERT INTO review (
            id, user_id, target_type, target_id, overall_score, content_text,
            verified_source_type, verified_source_ref_id, is_verified, weight_factor,
            review_status, risk_level, helpful_count, is_pinned, published_at, created_at, updated_at
        )
        VALUES (
            222000000 + first_course_id,
            current_student_id,
            'course',
            first_course_id,
            4.20,
            'M2 状态样例：课程评价已提交，等待系统核验来源和异常波动。',
            'trial_booking',
            first_course_id,
            false,
            0.500,
            'pending',
            1,
            0,
            false,
            now(),
            now(),
            now()
        )
        ON CONFLICT (id) DO UPDATE SET
            user_id = EXCLUDED.user_id,
            target_id = EXCLUDED.target_id,
            content_text = EXCLUDED.content_text,
            review_status = EXCLUDED.review_status,
            risk_level = EXCLUDED.risk_level,
            updated_at = now();
    END IF;

    -- M2 维度分：按目标类型补齐 4 个结构化维度，避免前端雷达图只显示单维度。
    WITH seed_reviews AS (
        SELECT id, target_type, row_number() OVER (ORDER BY id) AS rn
        FROM review
        WHERE id >= 220000000 AND id < 223000000
    ),
    dim_seed AS (
        SELECT * FROM (VALUES
            ('studio', 'traffic', '交通便利度', 0),
            ('studio', 'hygiene', '环境卫生', 1),
            ('studio', 'venue', '场地条件', 2),
            ('studio', 'vibe', '整体氛围', 3),
            ('coach', 'patience', '耐心程度', 0),
            ('coach', 'correction', '纠错质量', 1),
            ('coach', 'explanation', '讲解清晰度', 2),
            ('coach', 'beginnerFriendly', '零基础友好', 3),
            ('course', 'difficulty', '上手难度', 0),
            ('course', 'rhythm', '节奏合理性', 1),
            ('course', 'intensity', '练习强度', 2),
            ('course', 'gain', '实际收获', 3)
        ) AS d(target_type, code, name, sort_order)
    )
    -- M2 云库兼容：当前远端 review_dimension_score 表没有 created_at 字段，只写业务必需列。
    INSERT INTO review_dimension_score (id, review_id, dimension_code, dimension_name, score)
    SELECT
        r.id * 10 + d.sort_order,
        r.id,
        d.code,
        d.name,
        LEAST(5, 3 + ((r.rn + d.sort_order) % 3))::smallint
    FROM seed_reviews r
    JOIN dim_seed d ON d.target_type = r.target_type
    -- M2 云库兼容：当前远端未建 review_id + dimension_code 唯一约束，固定 id 可保证幂等。
    ON CONFLICT (id) DO UPDATE SET
        review_id = EXCLUDED.review_id,
        dimension_code = EXCLUDED.dimension_code,
        dimension_name = EXCLUDED.dimension_name,
        score = EXCLUDED.score;

    -- M2 媒体评价：用公开外链生成每条评价的图片附件，便于验证“带图”筛选和评价卡图片展示。
    WITH seed_reviews AS (
        SELECT id, row_number() OVER (ORDER BY id) AS rn
        FROM review
        WHERE id >= 220000000 AND id < 223000000
    ),
    asset_seed AS (
        SELECT
            225000000 + id AS asset_id,
            id AS review_id,
            'https://images.unsplash.com/photo-1547153760-18fc86324498?w=960&q=80&auto=format&fit=crop&m2baseline=' || id AS object_key,
            'm2-baseline-review-' || id || '.jpg' AS origin_file_name
        FROM seed_reviews
    )
    INSERT INTO media_asset (
        id, asset_type, biz_type, storage_provider, bucket_name, object_key, origin_file_name,
        mime_type, file_size, image_width, image_height, uploader_user_id, audit_status, is_public,
        created_at, updated_at
    )
    SELECT
        asset_id, 'image', 'review', 'external', 'external-url', object_key, origin_file_name,
        'image/jpeg', 180000, 960, 640, current_student_id, 'approved', true, now(), now()
    FROM asset_seed
    ON CONFLICT (id) DO UPDATE SET
        object_key = EXCLUDED.object_key,
        origin_file_name = EXCLUDED.origin_file_name,
        audit_status = 'approved',
        is_public = true,
        updated_at = now();

    -- M2 媒体评价：把图片资产绑定到 review，前端 ReviewAggregatePanel 和“我的评价”可直接读取 mediaAssets。
    WITH seed_reviews AS (
        SELECT id
        FROM review
        WHERE id >= 220000000 AND id < 223000000
    )
    INSERT INTO media_attachment (id, asset_id, target_type, target_id, usage_type, sort_order, created_at)
    SELECT
        226000000 + id,
        225000000 + id,
        'review',
        id,
        'review_media',
        0,
        now()
    FROM seed_reviews
    ON CONFLICT (asset_id, target_type, target_id, usage_type) DO UPDATE SET
        sort_order = EXCLUDED.sort_order;

    -- M2 商家/老师回复：每条基线评价生成官方回复，方便后续回复列表和评价治理链路联调。
    WITH seed_reviews AS (
        SELECT id, row_number() OVER (ORDER BY id) AS rn
        FROM review
        WHERE id >= 220000000 AND id < 223000000
          AND review_status = 'published'
    )
    -- M2 云库兼容：当前远端 review_reply 表没有 updated_at 字段，只写回复展示必需列。
    INSERT INTO review_reply (id, review_id, replier_user_id, reply_content, is_official, created_at)
    SELECT
        227000000 + id,
        id,
        admin_user_id,
        'M2 基线回复：感谢你的真实反馈，我们会持续优化课程和场地体验。',
        true,
        now() - (rn || ' minutes')::interval
    FROM seed_reviews
    ON CONFLICT (id) DO UPDATE SET
        reply_content = EXCLUDED.reply_content,
        is_official = true;

    -- M2 申诉/风控样例：给待审核课程评价补一条 pending 申诉，让我的评价页能展示复核状态。
    IF first_course_id IS NOT NULL THEN
        INSERT INTO review_appeal (
            id, review_id, appellant_user_id, appeal_reason, appeal_status, evidence_note,
            reviewed_by_user_id, reviewed_at, review_remark, created_at, updated_at
        )
        VALUES (
            228000000 + first_course_id,
            222000000 + first_course_id,
            admin_user_id,
            'M2 基线申诉：模拟商家认为课程评价需要平台复核。',
            'pending',
            '包含签到与课程记录截图，供平台后续审核。',
            NULL,
            NULL,
            NULL,
            now(),
            now()
        )
        ON CONFLICT (id) DO UPDATE SET
            appeal_status = EXCLUDED.appeal_status,
            evidence_note = EXCLUDED.evidence_note,
            updated_at = now();
    END IF;

    -- M2 数据导入留痕：标记本次评价体系基线数据已经执行，便于后续排查云库状态。
    INSERT INTO data_import_batch (
        batch_no, import_type, source_type, created_by_user_id, total_count,
        success_count, failure_count, import_status, error_summary, finished_at, created_at, updated_at
    )
    VALUES (
        'M2_REVIEW_BASELINE_20260609',
        'other',
        'platform_import',
        admin_user_id,
        (SELECT count(*)::integer FROM review WHERE id >= 220000000 AND id < 223000000),
        (SELECT count(*)::integer FROM review WHERE id >= 220000000 AND id < 223000000),
        0,
        'succeeded',
        'M2 baseline reviews, dimensions, media attachments, replies, and one pending-state sample.',
        now(),
        now(),
        now()
    )
    ON CONFLICT (batch_no) DO UPDATE SET
        total_count = EXCLUDED.total_count,
        success_count = EXCLUDED.success_count,
        import_status = EXCLUDED.import_status,
        error_summary = EXCLUDED.error_summary,
        finished_at = now(),
        updated_at = now();
END $$;

COMMIT;
