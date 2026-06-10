-- BitDance M1/M2 media and cover seed data.
-- Run after dev_seed_mock_data.sql. The script is idempotent and only enriches media/cover/attachment fields.

BEGIN;

SET search_path TO bitdance, public;

-- M1/M2 使用公开外链图片做开发种子；对象存储接入前，不把图片二进制写入数据库。
WITH seed_assets(asset_type, biz_type, object_key, origin_file_name, mime_type, file_size, image_width, image_height) AS (
    VALUES
        ('image', 'studio', 'https://images.unsplash.com/photo-1547153760-18fc86324498?w=960&q=80&auto=format&fit=crop', 'm1-studio-01.jpg', 'image/jpeg', 180000, 960, 640),
        ('image', 'studio', 'https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=960&q=80&auto=format&fit=crop', 'm1-studio-02.jpg', 'image/jpeg', 180000, 960, 640),
        ('image', 'studio', 'https://images.unsplash.com/photo-1524594152303-9fd13543fe6e?w=960&q=80&auto=format&fit=crop', 'm1-studio-03.jpg', 'image/jpeg', 180000, 960, 640),
        ('image', 'studio', 'https://images.unsplash.com/photo-1518611012118-696072aa579a?w=960&q=80&auto=format&fit=crop', 'm1-studio-04.jpg', 'image/jpeg', 180000, 960, 640),
        ('image', 'studio', 'https://images.unsplash.com/photo-1535525153412-5a42439a210d?w=960&q=80&auto=format&fit=crop', 'm1-studio-05.jpg', 'image/jpeg', 180000, 960, 640),
        ('image', 'studio', 'https://images.unsplash.com/photo-1504609813442-a8924e83f76e?w=960&q=80&auto=format&fit=crop', 'm1-studio-06.jpg', 'image/jpeg', 180000, 960, 640),
        ('image', 'studio', 'https://images.unsplash.com/photo-1524594154908-edd4933a053b?w=960&q=80&auto=format&fit=crop', 'm1-studio-07.jpg', 'image/jpeg', 180000, 960, 640),
        ('image', 'studio', 'https://images.unsplash.com/photo-1517457373958-b7bdd4587205?w=960&q=80&auto=format&fit=crop', 'm1-studio-08.jpg', 'image/jpeg', 180000, 960, 640),
        ('image', 'studio', 'https://images.unsplash.com/photo-1516280440614-37939bbacd81?w=960&q=80&auto=format&fit=crop', 'm1-studio-09.jpg', 'image/jpeg', 180000, 960, 640),
        ('image', 'studio', 'https://images.unsplash.com/photo-1519741497674-611481863552?w=960&q=80&auto=format&fit=crop', 'm1-studio-10.jpg', 'image/jpeg', 180000, 960, 640),
        ('image', 'course', 'https://images.unsplash.com/photo-1547153760-18fc86324498?w=960&q=80&auto=format&fit=crop&course=1', 'm1-course-01.jpg', 'image/jpeg', 180000, 960, 640),
        ('image', 'course', 'https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=960&q=80&auto=format&fit=crop&course=2', 'm1-course-02.jpg', 'image/jpeg', 180000, 960, 640),
        ('image', 'course', 'https://images.unsplash.com/photo-1524594152303-9fd13543fe6e?w=960&q=80&auto=format&fit=crop&course=3', 'm1-course-03.jpg', 'image/jpeg', 180000, 960, 640),
        ('image', 'course', 'https://images.unsplash.com/photo-1518611012118-696072aa579a?w=960&q=80&auto=format&fit=crop&course=4', 'm1-course-04.jpg', 'image/jpeg', 180000, 960, 640),
        ('image', 'course', 'https://images.unsplash.com/photo-1535525153412-5a42439a210d?w=960&q=80&auto=format&fit=crop&course=5', 'm1-course-05.jpg', 'image/jpeg', 180000, 960, 640),
        ('image', 'course', 'https://images.unsplash.com/photo-1504609813442-a8924e83f76e?w=960&q=80&auto=format&fit=crop&course=6', 'm1-course-06.jpg', 'image/jpeg', 180000, 960, 640),
        ('image', 'course', 'https://images.unsplash.com/photo-1524594154908-edd4933a053b?w=960&q=80&auto=format&fit=crop&course=7', 'm1-course-07.jpg', 'image/jpeg', 180000, 960, 640),
        ('image', 'course', 'https://images.unsplash.com/photo-1517457373958-b7bdd4587205?w=960&q=80&auto=format&fit=crop&course=8', 'm1-course-08.jpg', 'image/jpeg', 180000, 960, 640),
        ('image', 'course', 'https://images.unsplash.com/photo-1516280440614-37939bbacd81?w=960&q=80&auto=format&fit=crop&course=9', 'm1-course-09.jpg', 'image/jpeg', 180000, 960, 640),
        ('image', 'course', 'https://images.unsplash.com/photo-1519741497674-611481863552?w=960&q=80&auto=format&fit=crop&course=10', 'm1-course-10.jpg', 'image/jpeg', 180000, 960, 640),
        ('image', 'coach', 'https://images.unsplash.com/photo-1544717305-2782549b5136?w=960&q=80&auto=format&fit=crop', 'm1-coach-01.jpg', 'image/jpeg', 180000, 960, 640),
        ('image', 'coach', 'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=960&q=80&auto=format&fit=crop', 'm1-coach-02.jpg', 'image/jpeg', 180000, 960, 640),
        ('image', 'coach', 'https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=960&q=80&auto=format&fit=crop', 'm1-coach-03.jpg', 'image/jpeg', 180000, 960, 640),
        ('image', 'coach', 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=960&q=80&auto=format&fit=crop', 'm1-coach-04.jpg', 'image/jpeg', 180000, 960, 640),
        ('image', 'coach', 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=960&q=80&auto=format&fit=crop', 'm1-coach-05.jpg', 'image/jpeg', 180000, 960, 640),
        ('image', 'coach', 'https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=960&q=80&auto=format&fit=crop', 'm1-coach-06.jpg', 'image/jpeg', 180000, 960, 640),
        ('image', 'coach', 'https://images.unsplash.com/photo-1517841905240-472988babdf9?w=960&q=80&auto=format&fit=crop', 'm1-coach-07.jpg', 'image/jpeg', 180000, 960, 640),
        ('image', 'coach', 'https://images.unsplash.com/photo-1524504388940-b1c1722653e1?w=960&q=80&auto=format&fit=crop', 'm1-coach-08.jpg', 'image/jpeg', 180000, 960, 640),
        ('image', 'coach', 'https://images.unsplash.com/photo-1520813792240-56fc4a3765a7?w=960&q=80&auto=format&fit=crop', 'm1-coach-09.jpg', 'image/jpeg', 180000, 960, 640),
        ('image', 'coach', 'https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=960&q=80&auto=format&fit=crop', 'm1-coach-10.jpg', 'image/jpeg', 180000, 960, 640),
        ('image', 'review', 'https://images.unsplash.com/photo-1524594152303-9fd13543fe6e?w=960&q=80&auto=format&fit=crop&review=1', 'm2-review-01.jpg', 'image/jpeg', 180000, 960, 640),
        ('image', 'review', 'https://images.unsplash.com/photo-1518611012118-696072aa579a?w=960&q=80&auto=format&fit=crop&review=2', 'm2-review-02.jpg', 'image/jpeg', 180000, 960, 640),
        ('image', 'review', 'https://images.unsplash.com/photo-1547153760-18fc86324498?w=960&q=80&auto=format&fit=crop&review=3', 'm2-review-03.jpg', 'image/jpeg', 180000, 960, 640),
        ('image', 'review', 'https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=960&q=80&auto=format&fit=crop&review=4', 'm2-review-04.jpg', 'image/jpeg', 180000, 960, 640),
        ('image', 'review', 'https://images.unsplash.com/photo-1535525153412-5a42439a210d?w=960&q=80&auto=format&fit=crop&review=5', 'm2-review-05.jpg', 'image/jpeg', 180000, 960, 640),
        ('image', 'review', 'https://images.unsplash.com/photo-1504609813442-a8924e83f76e?w=960&q=80&auto=format&fit=crop&review=6', 'm2-review-06.jpg', 'image/jpeg', 180000, 960, 640),
        ('image', 'review', 'https://images.unsplash.com/photo-1524594154908-edd4933a053b?w=960&q=80&auto=format&fit=crop&review=7', 'm2-review-07.jpg', 'image/jpeg', 180000, 960, 640),
        ('image', 'review', 'https://images.unsplash.com/photo-1517457373958-b7bdd4587205?w=960&q=80&auto=format&fit=crop&review=8', 'm2-review-08.jpg', 'image/jpeg', 180000, 960, 640),
        ('image', 'review', 'https://images.unsplash.com/photo-1516280440614-37939bbacd81?w=960&q=80&auto=format&fit=crop&review=9', 'm2-review-09.jpg', 'image/jpeg', 180000, 960, 640),
        ('image', 'review', 'https://images.unsplash.com/photo-1519741497674-611481863552?w=960&q=80&auto=format&fit=crop&review=10', 'm2-review-10.jpg', 'image/jpeg', 180000, 960, 640)
)
INSERT INTO media_asset (
    asset_type, biz_type, storage_provider, bucket_name, object_key, origin_file_name,
    mime_type, file_size, image_width, image_height, uploader_user_id, audit_status, is_public
)
SELECT
    asset_type, biz_type, 'external', 'external-url', object_key, origin_file_name,
    mime_type, file_size, image_width, image_height,
    (SELECT id FROM app_user ORDER BY id LIMIT 1), 'approved', true
FROM seed_assets
ON CONFLICT (bucket_name, object_key) DO UPDATE SET
    biz_type = EXCLUDED.biz_type,
    origin_file_name = EXCLUDED.origin_file_name,
    mime_type = EXCLUDED.mime_type,
    audit_status = 'approved',
    is_public = true,
    updated_at = now();

-- M1 封面增强：把前 10 个舞室/课程/教练补上 cover_asset_id，保证列表和详情有可见图片元数据。
WITH ranked_studio AS (
    SELECT id, row_number() OVER (ORDER BY id) AS rn FROM studio ORDER BY id LIMIT 10
),
ranked_asset AS (
    SELECT id, row_number() OVER (ORDER BY origin_file_name) AS rn
    FROM media_asset
    WHERE bucket_name = 'external-url' AND origin_file_name LIKE 'm1-studio-%'
)
UPDATE studio s
-- 当前云库 studio 表未必包含完整 schema 中的 source_note，本脚本只补 M1 封面必需字段。
SET cover_asset_id = a.id
FROM ranked_studio rs
JOIN ranked_asset a ON a.rn = rs.rn
WHERE s.id = rs.id;

WITH ranked_course AS (
    SELECT id, row_number() OVER (ORDER BY id) AS rn FROM course ORDER BY id LIMIT 10
),
ranked_asset AS (
    SELECT id, row_number() OVER (ORDER BY origin_file_name) AS rn
    FROM media_asset
    WHERE bucket_name = 'external-url' AND origin_file_name LIKE 'm1-course-%'
)
UPDATE course c
SET cover_asset_id = a.id
FROM ranked_course rc
JOIN ranked_asset a ON a.rn = rc.rn
WHERE c.id = rc.id;

WITH ranked_coach AS (
    SELECT id, row_number() OVER (ORDER BY id) AS rn FROM coach ORDER BY id LIMIT 10
),
ranked_asset AS (
    SELECT id, row_number() OVER (ORDER BY origin_file_name) AS rn
    FROM media_asset
    WHERE bucket_name = 'external-url' AND origin_file_name LIKE 'm1-coach-%'
)
UPDATE coach c
SET cover_asset_id = a.id
FROM ranked_coach rc
JOIN ranked_asset a ON a.rn = rc.rn
WHERE c.id = rc.id;

-- M2 评价媒体增强：给前 10 条评价关联可公开访问的图片资产，驱动评价列表和聚合面板展示。
WITH ranked_review AS (
    SELECT id, row_number() OVER (ORDER BY id) AS rn FROM review ORDER BY id LIMIT 10
),
ranked_asset AS (
    SELECT id, row_number() OVER (ORDER BY origin_file_name) AS rn
    FROM media_asset
    WHERE bucket_name = 'external-url' AND origin_file_name LIKE 'm2-review-%'
)
INSERT INTO media_attachment (asset_id, target_type, target_id, usage_type, sort_order)
SELECT a.id, 'review', rr.id, 'review_media', rr.rn - 1
FROM ranked_review rr
JOIN ranked_asset a ON a.rn = rr.rn
ON CONFLICT (asset_id, target_type, target_id, usage_type) DO UPDATE SET
    sort_order = EXCLUDED.sort_order;

-- 批次留痕：用于区分本脚本只做 M1/M2 媒体增强，不代表全量生产导入完成。
INSERT INTO data_import_batch (
    batch_no, import_type, source_type, created_by_user_id, total_count,
    success_count, failure_count, import_status, error_summary, finished_at
)
VALUES (
    'BD-F-M1-M2-MEDIA-SEED-20260605', 'other', 'platform_import',
    (SELECT id FROM app_user ORDER BY id LIMIT 1), 40, 40, 0,
    'succeeded', 'M1/M2 media cover and review attachment enrichment', now()
)
ON CONFLICT (batch_no) DO UPDATE SET
    success_count = EXCLUDED.success_count,
    failure_count = EXCLUDED.failure_count,
    import_status = EXCLUDED.import_status,
    error_summary = EXCLUDED.error_summary,
    finished_at = EXCLUDED.finished_at,
    updated_at = now();

-- Keep identity sequences ahead of generated/enriched media rows for later application writes.
DO $$
DECLARE
    t text;
    seq_name text;
    id_tables text[] := ARRAY['media_asset', 'media_attachment', 'data_import_batch'];
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
