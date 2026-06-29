-- V1__create_initial_schema.sql
-- Initial schema setup for Controlei
-- This migration sets up the foundational database extensions

-- Enable pgcrypto extension for UUID generation
create extension if not exists pgcrypto;
