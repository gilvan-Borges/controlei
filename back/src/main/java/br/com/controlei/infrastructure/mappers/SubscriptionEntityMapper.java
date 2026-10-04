package br.com.controlei.infrastructure.mappers;

import br.com.controlei.domain.models.entities.Subscription;
import br.com.controlei.infrastructure.persistence.entities.SubscriptionEntity;
import org.springframework.stereotype.Component;

@Component
public class SubscriptionEntityMapper {

    public Subscription toDomain(SubscriptionEntity entity) {
        if (entity == null) {
            return null;
        }

        return new Subscription(
                entity.getId(),
                entity.getFamilyId(),
                entity.getOwnerId(),
                entity.getPlanType(),
                entity.getStatus(),
                entity.getBaseMembersAllowed(),
                entity.getExtraMembersPaid(),
                entity.getPriceMonthly(),
                entity.getTrialEndsAt(),
                entity.getCurrentPeriodStartsAt(),
                entity.getCurrentPeriodEndsAt(),
                entity.getGraceDaysRemaining(),
                entity.isAutoRenew(),
                entity.getPaymentMethod(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt(),
                entity.getDeletedBy()
        );
    }

    public SubscriptionEntity toEntity(Subscription domain) {
        if (domain == null) {
            return null;
        }

        SubscriptionEntity entity = new SubscriptionEntity();
        entity.setId(domain.getId());
        entity.setFamilyId(domain.getFamilyId());
        entity.setOwnerId(domain.getOwnerId());
        entity.setPlanType(domain.getPlanType());
        entity.setStatus(domain.getStatus());
        entity.setBaseMembersAllowed(domain.getBaseMembersAllowed());
        entity.setExtraMembersPaid(domain.getExtraMembersPaid());
        entity.setPriceMonthly(domain.getPriceMonthly());
        entity.setTrialEndsAt(domain.getTrialEndsAt());
        entity.setCurrentPeriodStartsAt(domain.getCurrentPeriodStartsAt());
        entity.setCurrentPeriodEndsAt(domain.getCurrentPeriodEndsAt());
        entity.setGraceDaysRemaining(domain.getGraceDaysRemaining());
        entity.setAutoRenew(domain.isAutoRenew());
        entity.setPaymentMethod(domain.getPaymentMethod());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        entity.setDeletedAt(domain.getDeletedAt());
        entity.setDeletedBy(domain.getDeletedBy());
        return entity;
    }
}
