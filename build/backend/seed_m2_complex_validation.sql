-- M2 complex validation seed.
-- Run after dev_seed_mock_data.sql. This script is idempotent and only prepares
-- review validation facts, result states, media, replies, and appeals.

SET search_path TO bitdance;

DO $$
BEGIN
    -- M2 来源验证：试听事实覆盖 studio/course/coach 三类评价来源。
    INSERT INTO trial_booking (
        id, attended_at, booking_note, booking_status, cancel_reason, canceled_at, confirmed_at,
        confirmed_by_user_id, contact_phone, course_id, course_schedule_id, created_at, studio_id, user_id
    )
    VALUES
        (190001, now() - interval '1 day', 'M2 验证：已到课试听，可证明课程/舞室/老师评价', 'attended', NULL, NULL, now() - interval '2 day',
         100002, '13900000008', 100001, 100001, now() - interval '3 day', 100001, 100008),
        (190002, NULL, 'M2 验证：已确认试听，可证明课程/舞室评价', 'confirmed', NULL, NULL, now() - interval '1 day',
         100002, '13900000009', 100002, 100002, now() - interval '2 day', 100002, 100009)
    ON CONFLICT (id) DO UPDATE SET
        booking_status = EXCLUDED.booking_status,
        attended_at = EXCLUDED.attended_at,
        confirmed_at = EXCLUDED.confirmed_at,
        user_id = EXCLUDED.user_id,
        course_id = EXCLUDED.course_id,
        studio_id = EXCLUDED.studio_id;

    -- M2/M6 联验：Workshop 订单和签到事实用于 order/checkin 来源评价。
    INSERT INTO workshop_order (
        id, amount_paid, amount_payable, canceled_at, completed_at, created_at, order_no,
        order_status, paid_at, payment_txn_no, refund_reason, refunded_at, user_id,
        workshop_id, workshop_session_id
    )
    VALUES
        (190001, 229.00, 229.00, NULL, NULL, now() - interval '2 day', 'M2_ORDER_190001',
         'paid', now() - interval '2 day', 'M2_PAY_190001', NULL, NULL, 100008, 100001, 100001),
        (190002, 259.00, 259.00, NULL, now() - interval '1 day', now() - interval '3 day', 'M2_ORDER_190002',
         'completed', now() - interval '3 day', 'M2_PAY_190002', NULL, NULL, 100009, 100002, 100002)
    ON CONFLICT (id) DO UPDATE SET
        order_status = EXCLUDED.order_status,
        amount_paid = EXCLUDED.amount_paid,
        paid_at = EXCLUDED.paid_at,
        completed_at = EXCLUDED.completed_at,
        user_id = EXCLUDED.user_id,
        workshop_id = EXCLUDED.workshop_id,
        workshop_session_id = EXCLUDED.workshop_session_id;

    INSERT INTO workshop_checkin (
        id, checked_in_at, checked_in_by_user_id, checkin_code, checkin_status, created_at,
        workshop_order_id, workshop_session_id
    )
    VALUES
        (190001, now() - interval '1 day', 100008, 'M2-CHECKIN-190001', 'checked_in', now() - interval '1 day', 190001, 100001),
        (190002, now() - interval '1 day', 100009, 'M2-CHECKIN-190002', 'checked_in', now() - interval '1 day', 190002, 100002)
    ON CONFLICT (id) DO UPDATE SET
        checked_in_by_user_id = EXCLUDED.checked_in_by_user_id,
        checkin_status = EXCLUDED.checkin_status,
        checkin_code = EXCLUDED.checkin_code,
        workshop_order_id = EXCLUDED.workshop_order_id,
        workshop_session_id = EXCLUDED.workshop_session_id;

    -- M2 UI 认证：补足角色，便于“我的/教练回复/管理员审批”路径用真实登录态访问。
    INSERT INTO user_role_binding (id, created_at, updated_at, role, status, user_id)
    VALUES
        (190001, now(), now(), 'USER', 'ACTIVE', 100008),
        (190002, now(), now(), 'COACH', 'ACTIVE', 100001),
        (190003, now(), now(), 'STUDIO_ADMIN', 'ACTIVE', 100002),
        (190004, now(), now(), 'PLATFORM_ADMIN', 'ACTIVE', 100001)
    ON CONFLICT (id) DO UPDATE SET
        updated_at = EXCLUDED.updated_at,
        role = EXCLUDED.role,
        status = EXCLUDED.status,
        user_id = EXCLUDED.user_id;

    -- M2 自验证：覆盖普通、已验证、折叠、隐藏评价；包含 studio/course/coach 三类对象。
    INSERT INTO review (
        id, content_text, created_at, helpful_count, is_pinned, is_verified, overall_score,
        published_at, review_status, risk_level, target_id, target_type, user_id,
        verified_source_ref_id, verified_source_type, weight_factor
    )
    VALUES
        (190101, 'M2 普通舞室评价：环境整洁，交通方便，但暂未带来源验证。', now(), 3, false, false, 4.20,
         now() - interval '4 hour', 'published', 0, 100001, 'studio', 100008, NULL, NULL, 1.000),
        (190102, 'M2 试听已验证课程评价：节奏适合零基础，老师拆动作清楚。', now(), 8, true, true, 4.80,
         now() - interval '3 hour', 'published', 0, 100001, 'course', 100008, 190001, 'trial', 1.500),
        (190103, 'M2 试听已验证老师评价：纠错及时，能指出发力问题。', now(), 6, false, true, 4.70,
         now() - interval '2 hour', 'published', 0, 100001, 'coach', 100008, 190001, 'trial', 1.500),
        (190104, 'M2 Workshop 签到已验证老师评价：现场组织顺畅，互动充分。', now(), 5, false, true, 4.60,
         now() - interval '90 minute', 'published', 0, 100001, 'coach', 100008, 190001, 'checkin', 1.500),
        (190105, 'M2 折叠样例：短时间重复评价导致系统先降低展示优先级。', now(), 0, false, false, 2.00,
         now() - interval '50 minute', 'folded', 2, 100001, 'studio', 100009, NULL, NULL, 0.500),
        (190106, 'M2 隐藏样例：申诉成立后不再进入公开评价列表。', now(), 0, false, false, 1.00,
         now() - interval '40 minute', 'hidden', 3, 100002, 'studio', 100009, NULL, NULL, 0.100)
    ON CONFLICT (id) DO UPDATE SET
        content_text = EXCLUDED.content_text,
        is_verified = EXCLUDED.is_verified,
        overall_score = EXCLUDED.overall_score,
        review_status = EXCLUDED.review_status,
        risk_level = EXCLUDED.risk_level,
        verified_source_ref_id = EXCLUDED.verified_source_ref_id,
        verified_source_type = EXCLUDED.verified_source_type,
        weight_factor = EXCLUDED.weight_factor;

    DELETE FROM review_dimension_score WHERE review_id IN (190101, 190102, 190103, 190104, 190105, 190106);

    INSERT INTO review_dimension_score (id, review_id, dimension_code, dimension_name, score)
    VALUES
        (19010101, 190101, 'traffic', '交通便利度', 5),
        (19010102, 190101, 'hygiene', '环境卫生', 4),
        (19010103, 190101, 'venue', '场地条件', 4),
        (19010104, 190101, 'vibe', '整体氛围', 4),
        (19010201, 190102, 'difficulty', '上手难度', 5),
        (19010202, 190102, 'rhythm', '节奏合理性', 5),
        (19010203, 190102, 'intensity', '练习强度', 4),
        (19010204, 190102, 'gain', '实际收获', 5),
        (19010301, 190103, 'patience', '耐心程度', 5),
        (19010302, 190103, 'correction', '纠错质量', 5),
        (19010303, 190103, 'explanation', '讲解清晰度', 4),
        (19010304, 190103, 'beginnerFriendly', '零基础友好', 5),
        (19010401, 190104, 'patience', '耐心程度', 5),
        (19010402, 190104, 'correction', '纠错质量', 4),
        (19010403, 190104, 'explanation', '讲解清晰度', 5),
        (19010404, 190104, 'beginnerFriendly', '零基础友好', 4),
        (19010501, 190105, 'traffic', '交通便利度', 2),
        (19010502, 190105, 'hygiene', '环境卫生', 2),
        (19010503, 190105, 'venue', '场地条件', 2),
        (19010504, 190105, 'vibe', '整体氛围', 2),
        (19010601, 190106, 'traffic', '交通便利度', 1),
        (19010602, 190106, 'hygiene', '环境卫生', 1),
        (19010603, 190106, 'venue', '场地条件', 1),
        (19010604, 190106, 'vibe', '整体氛围', 1)
    ON CONFLICT (id) DO UPDATE SET
        review_id = EXCLUDED.review_id,
        dimension_code = EXCLUDED.dimension_code,
        dimension_name = EXCLUDED.dimension_name,
        score = EXCLUDED.score;

    -- M2 媒体展示：评价媒体走真实 media_asset + media_attachment。
    INSERT INTO media_asset (
        id, asset_type, biz_type, storage_provider, bucket_name, object_key, origin_file_name,
        mime_type, file_size, uploader_user_id, audit_status, is_public
    )
    VALUES
        (190101, 'image', 'review', 'external', 'external-url',
         'https://images.unsplash.com/photo-1547153760-18fc86324498?w=960&q=80&auto=format&fit=crop&m2=190101',
         'm2-review-studio.jpg', 'image/jpeg', 180000, 100008, 'approved', true),
        (190102, 'image', 'review', 'external', 'external-url',
         'https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=960&q=80&auto=format&fit=crop&m2=190102',
         'm2-review-course.jpg', 'image/jpeg', 180000, 100008, 'approved', true),
        (190103, 'video', 'review', 'external', 'external-url',
         'https://interactive-examples.mdn.mozilla.net/media/cc0-videos/flower.mp4?m2=190103',
         'm2-review-coach.mp4', 'video/mp4', 2400000, 100008, 'approved', true)
    ON CONFLICT (id) DO UPDATE SET
        object_key = EXCLUDED.object_key,
        asset_type = EXCLUDED.asset_type,
        audit_status = EXCLUDED.audit_status,
        is_public = EXCLUDED.is_public;

    DELETE FROM media_attachment
    WHERE target_type = 'review'
      AND target_id IN (190101, 190102, 190103)
      AND usage_type = 'review_media';

    INSERT INTO media_attachment (id, asset_id, target_type, target_id, usage_type, sort_order)
    VALUES
        (190101, 190101, 'review', 190101, 'review_media', 0),
        (190102, 190102, 'review', 190102, 'review_media', 0),
        (190103, 190103, 'review', 190103, 'review_media', 0)
    ON CONFLICT (id) DO UPDATE SET
        asset_id = EXCLUDED.asset_id,
        target_type = EXCLUDED.target_type,
        target_id = EXCLUDED.target_id,
        usage_type = EXCLUDED.usage_type,
        sort_order = EXCLUDED.sort_order;

    -- M2 治理回流：回复和申诉覆盖待处理、驳回、成立隐藏三种状态。
    INSERT INTO review_reply (id, created_at, is_official, replier_user_id, reply_content, review_id)
    VALUES
        (190101, now(), true, 100002, '感谢反馈，我们会继续保持环境和课表稳定。', 190101),
        (190102, now(), true, 100001, '感谢到课体验，后续课程会继续保留零基础拆解。', 190102)
    ON CONFLICT (id) DO UPDATE SET
        reply_content = EXCLUDED.reply_content,
        replier_user_id = EXCLUDED.replier_user_id;

    INSERT INTO review_appeal (
        id, appeal_reason, appeal_status, appellant_user_id, created_at, evidence_note,
        review_id, review_remark, reviewed_at, reviewed_by_user_id
    )
    VALUES
        (190101, '恶意差评：与实际签到记录不符', 'pending', 100002, now(), '商家提供签到记录和课堂监控时间说明', 190105, NULL, NULL, NULL),
        (190102, '与事实不符：该用户未到店', 'approved', 100002, now(), '平台核验后确认申诉成立', 190106,
         '申诉成立，评价已隐藏', now(), 100001),
        (190103, '同行攻击：评价内容疑似不实', 'rejected', 100003, now(), '平台未发现违规证据', 190101,
         '证据不足，维持原展示状态', now(), 100001)
    ON CONFLICT (id) DO UPDATE SET
        appeal_status = EXCLUDED.appeal_status,
        appeal_reason = EXCLUDED.appeal_reason,
        evidence_note = EXCLUDED.evidence_note,
        review_remark = EXCLUDED.review_remark,
        reviewed_at = EXCLUDED.reviewed_at,
        reviewed_by_user_id = EXCLUDED.reviewed_by_user_id;

END $$;
