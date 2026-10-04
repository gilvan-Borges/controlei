-- V7__fix_category_unique_for_soft_delete.sql
-- Remove unique constraint that conflicts with soft delete and add partial unique index

ALTER TABLE categories DROP CONSTRAINT IF EXISTS uq_categories_family_type_name;

CREATE UNIQUE INDEX uq_categories_active_family_type_name
    ON categories(family_id, type, name)
    WHERE deleted_at IS NULL;
