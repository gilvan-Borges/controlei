package br.com.controlei.application.services;

import br.com.controlei.application.exceptions.BusinessException;
import br.com.controlei.domain.contracts.repositories.SubscriptionRepositoryPort;
import br.com.controlei.domain.models.dtos.subscription.PlanDetailsResponse;
import br.com.controlei.domain.models.dtos.subscription.SubscriptionResponse;
import br.com.controlei.domain.models.dtos.subscription.UpgradeSubscriptionRequest;
import br.com.controlei.domain.models.entities.Subscription;
import br.com.controlei.domain.models.enums.PlanType;
import br.com.controlei.domain.models.enums.SubscriptionStatus;
import br.com.controlei.infrastructure.kafka.EventPublisher;
import br.com.controlei.shared.events.KafkaTopics;
import br.com.controlei.shared.events.MemberQuotaUpdatedEvent;
import br.com.controlei.shared.events.SubscriptionActivatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class SubscriptionService {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionService.class);

    public static final BigDecimal PRICE_INDIVIDUAL = new BigDecimal("24.90");
    public static final BigDecimal PRICE_FAMILIAR = new BigDecimal("49.90");
    public static final BigDecimal PRICE_EXTRA_MEMBER = new BigDecimal("9.90");

    private final SubscriptionRepositoryPort subscriptionRepository;
    private final AuthorizationService authorizationService;
    private final EventPublisher eventPublisher;

    public SubscriptionService(SubscriptionRepositoryPort subscriptionRepository,
                               AuthorizationService authorizationService,
                               EventPublisher eventPublisher) {
        this.subscriptionRepository = subscriptionRepository;
        this.authorizationService = authorizationService;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public SubscriptionResponse getFamilySubscription() {
        UUID familyId = authorizationService.currentFamilyId();
        UUID userId = authorizationService.currentUserId();

        Subscription subscription = subscriptionRepository.findByFamilyIdAndDeletedAtIsNull(familyId)
                .orElseGet(() -> createDefaultTrialSubscription(familyId, userId));

        return toResponse(subscription);
    }

    @Transactional
    public SubscriptionResponse upgradeSubscription(UpgradeSubscriptionRequest request) {
        UUID familyId = authorizationService.currentFamilyId();
        UUID userId = authorizationService.currentUserId();

        if (!authorizationService.isResponsible()) {
            throw new BusinessException("Apenas o responsavel pela familia pode alterar o plano de assinatura.");
        }

        Subscription subscription = subscriptionRepository.findByFamilyIdAndDeletedAtIsNull(familyId)
                .orElseGet(() -> createDefaultTrialSubscription(familyId, userId));

        int baseMembers = switch (request.planType()) {
            case INDIVIDUAL -> 1; // apenas o titular
            case FAMILIAR, TRIAL -> 3; // titular + 2 dependentes inclusos
        };

        int extraMembers = request.planType() == PlanType.FAMILIAR ? Math.max(0, request.extraMembers()) : 0;

        BigDecimal basePrice = switch (request.planType()) {
            case INDIVIDUAL -> PRICE_INDIVIDUAL;
            case FAMILIAR -> PRICE_FAMILIAR;
            case TRIAL -> BigDecimal.ZERO;
        };

        BigDecimal extraPrice = PRICE_EXTRA_MEMBER.multiply(BigDecimal.valueOf(extraMembers));
        BigDecimal totalPrice = basePrice.add(extraPrice);

        subscription.setPlanType(request.planType());
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setBaseMembersAllowed(baseMembers);
        subscription.setExtraMembersPaid(extraMembers);
        subscription.setPriceMonthly(totalPrice);
        subscription.setCurrentPeriodStartsAt(LocalDateTime.now());
        subscription.setCurrentPeriodEndsAt(LocalDateTime.now().plusMonths(1));
        subscription.setPaymentMethod(request.paymentMethod() != null ? request.paymentMethod() : "PIX");
        subscription.setUpdatedAt(LocalDateTime.now());

        Subscription saved = subscriptionRepository.save(subscription);

        // Emite eventos Kafka
        try {
            eventPublisher.publish(KafkaTopics.BILLING,
                    SubscriptionActivatedEvent.builder()
                            .subscriptionId(saved.getId())
                            .familyId(saved.getFamilyId())
                            .ownerId(saved.getOwnerId())
                            .planType(saved.getPlanType().name())
                            .baseMembersAllowed(saved.getBaseMembersAllowed())
                            .extraMembersAllowed(saved.getExtraMembersPaid())
                            .totalMembersAllowed(saved.getTotalMembersAllowed())
                            .isTrial(saved.getPlanType() == PlanType.TRIAL)
                            .build()
            );

            eventPublisher.publish(KafkaTopics.BILLING,
                    MemberQuotaUpdatedEvent.builder()
                            .familyId(saved.getFamilyId())
                            .baseMembersAllowed(saved.getBaseMembersAllowed())
                            .extraMembersPaid(saved.getExtraMembersPaid())
                            .totalAllowedMembers(saved.getTotalMembersAllowed())
                            .build()
            );
        } catch (Exception ex) {
            log.error("Erro ao emitir eventos de assinatura no Kafka: {}", ex.getMessage());
        }

        return toResponse(saved);
    }

    public List<PlanDetailsResponse> getAvailablePlans() {
        return List.of(
                new PlanDetailsResponse(
                        PlanType.TRIAL,
                        "Trial 30 Dias",
                        "Experimente gratuitamente todos os recursos do Controlei por 30 dias.",
                        BigDecimal.ZERO,
                        3, // 1 titular + 2 dependentes inclusos
                        PRICE_EXTRA_MEMBER,
                        false,
                        List.of(
                                "Acesso completo a todos os recursos",
                                "1 Titular + até 2 Dependentes",
                                "Divisão inteligente de despesas familiares",
                                "Metas e limites orçamentários com alertas",
                                "Relatórios e conciliação bancária"
                        )
                ),
                new PlanDetailsResponse(
                        PlanType.INDIVIDUAL,
                        "Plano Individual",
                        "Ideal para controle financeiro pessoal completo com alta performance.",
                        PRICE_INDIVIDUAL,
                        1, // apenas o titular
                        BigDecimal.ZERO,
                        false,
                        List.of(
                                "Controle de receitas, despesas e cartões",
                                "Dashboard pessoal com gráficos dinâmicos",
                                "Alertas de vencimento de fatura e orçamentos",
                                "Importação de extratos e comprovantes",
                                "Suporte prioritário"
                        )
                ),
                new PlanDetailsResponse(
                        PlanType.FAMILIAR,
                        "Plano Familiar",
                        "Gestão financeira colaborativa para casais e famílias com dependentes.",
                        PRICE_FAMILIAR,
                        3, // 1 titular + 2 dependentes inclusos
                        PRICE_EXTRA_MEMBER,
                        true,
                        List.of(
                                "1 Titular + 2 Dependentes inclusos no plano",
                                "Adicione dependentes extras por apenas R$ 9,90/mês cada",
                                "Divisão inteligente de contas da casa com PIX facilitado",
                                "Cartões de crédito individuais e consolidados",
                                "Limites orçamentários por membro da família",
                                "Notificações em tempo real no app e e-mail"
                        )
                )
        );
    }

    private Subscription createDefaultTrialSubscription(UUID familyId, UUID ownerId) {
        LocalDateTime now = LocalDateTime.now();
        Subscription trial = new Subscription(
                UUID.randomUUID(),
                familyId,
                ownerId,
                PlanType.TRIAL,
                SubscriptionStatus.TRIAL,
                3, // Titular + 2 dependentes
                0,
                BigDecimal.ZERO,
                now.plusDays(30),
                now,
                now.plusDays(30),
                5,
                true,
                "FREE_TRIAL",
                now,
                now,
                null,
                null
        );
        return subscriptionRepository.save(trial);
    }

    private SubscriptionResponse toResponse(Subscription s) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expires = s.getTrialEndsAt() != null ? s.getTrialEndsAt() : s.getCurrentPeriodEndsAt();
        long daysRemaining = expires != null && expires.isAfter(now) 
                ? Duration.between(now, expires).toDays() + 1 
                : 0;

        boolean isReadOnly = s.getStatus() == SubscriptionStatus.EXPIRED 
                || (s.getStatus() == SubscriptionStatus.TRIAL && daysRemaining <= 0);

        return new SubscriptionResponse(
                s.getId(),
                s.getFamilyId(),
                s.getOwnerId(),
                s.getPlanType(),
                s.getStatus(),
                s.getBaseMembersAllowed(),
                s.getExtraMembersPaid(),
                s.getTotalMembersAllowed(),
                s.getPriceMonthly(),
                s.getTrialEndsAt(),
                s.getCurrentPeriodStartsAt(),
                s.getCurrentPeriodEndsAt(),
                daysRemaining,
                s.getPlanType() == PlanType.TRIAL,
                isReadOnly,
                s.isAutoRenew(),
                s.getPaymentMethod()
        );
    }
}
