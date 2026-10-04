-- V7__fix_category_unique_for_soft_delete.sql
-- Remove unique constraint that conflicts with soft delete
-- H2 does not support partial indexes, so we just drop the constraint
-- Service-level validation handles the uniqueness check

ALTER TABLE categories DROP CONSTRAINT IF EXISTS uq_categories_family_type_name;
