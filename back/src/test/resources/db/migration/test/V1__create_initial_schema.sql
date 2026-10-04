-- V1__create_initial_schema.sql (H2 compatible version for tests)
-- Initial schema setup for Controlei - Test environment

-- H2 does not support pgcrypto extension, UUID generation uses built-in functions
-- This migration validates Flyway execution in test environment
