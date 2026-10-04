package br.com.controlei.domain.models.entities;

import br.com.controlei.domain.models.enums.CategoryType;

import java.time.LocalDateTime;
import java.util.UUID;

public class Category {

    private final UUID id;
    private final UUID familyId;
    private String name;
    private CategoryType type;
    private String color;
    private String icon;
    private boolean active;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    private String deletedBy;

    public Category(UUID id, UUID familyId, String name, CategoryType type,
                    String color, String icon, boolean active,
                    LocalDateTime createdAt, LocalDateTime updatedAt,
                    LocalDateTime deletedAt, String deletedBy) {
        this.id = id;
        this.familyId = familyId;
        this.name = name;
        this.type = type;
        this.color = color;
        this.icon = icon;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
        this.deletedBy = deletedBy;
    }

    public UUID getId() {
        return id;
    }

    public UUID getFamilyId() {
        return familyId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CategoryType getType() {
        return type;
    }

    public void setType(CategoryType type) {
        this.type = type;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }

    public String getDeletedBy() {
        return deletedBy;
    }

    public void setDeletedBy(String deletedBy) {
        this.deletedBy = deletedBy;
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }
}
