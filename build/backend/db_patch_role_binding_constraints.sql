ALTER TABLE user_role_binding
    DROP CONSTRAINT IF EXISTS chk_user_role_binding_role;

ALTER TABLE user_role_binding
    ADD CONSTRAINT chk_user_role_binding_role
    CHECK (role IN ('USER', 'COACH', 'STUDIO_ADMIN', 'PLATFORM_ADMIN'));
