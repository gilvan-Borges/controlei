package br.com.controlei.application.services;

import br.com.controlei.application.exceptions.NotFoundException;
import br.com.controlei.domain.contracts.repositories.NotificationRepositoryPort;
import br.com.controlei.domain.models.dtos.notification.CreateNotificationRequest;
import br.com.controlei.domain.models.dtos.notification.NotificationResponse;
import br.com.controlei.domain.models.dtos.notification.UnreadNotificationsCountResponse;
import br.com.controlei.domain.models.entities.Notification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class NotificationService {

    private final NotificationRepositoryPort notificationRepository;
    private final AuthorizationService authorizationService;

    public NotificationService(NotificationRepositoryPort notificationRepository,
                               AuthorizationService authorizationService) {
        this.notificationRepository = notificationRepository;
        this.authorizationService = authorizationService;
    }

    @Transactional
    public NotificationResponse createNotification(CreateNotificationRequest request) {
        UUID familyId = authorizationService.currentFamilyId();
        UUID targetUserId = request.userId() != null ? request.userId() : authorizationService.currentUserId();

        authorizationService.requireUserBelongsToFamily(targetUserId, familyId);

        Notification notification = new Notification(
                UUID.randomUUID(),
                familyId,
                targetUserId,
                request.title().trim(),
                request.message().trim(),
                request.type(),
                request.linkUrl(),
                false,
                null,
                LocalDateTime.now(),
                null,
                null,
                null
        );

        Notification saved = notificationRepository.save(notification);
        return buildResponse(saved);
    }

    public List<NotificationResponse> listNotifications(boolean unreadOnly) {
        UUID familyId = authorizationService.currentFamilyId();
        UUID userId = authorizationService.currentUserId();

        List<Notification> list = unreadOnly
                ? notificationRepository.findAllByFamilyIdAndUserIdAndReadIsFalseAndDeletedAtIsNullOrderByCreatedAtDesc(familyId, userId)
                : notificationRepository.findAllByFamilyIdAndUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(familyId, userId);

        return list.stream().map(this::buildResponse).toList();
    }

    public UnreadNotificationsCountResponse getUnreadCount() {
        UUID familyId = authorizationService.currentFamilyId();
        UUID userId = authorizationService.currentUserId();
        long count = notificationRepository.countByFamilyIdAndUserIdAndReadIsFalseAndDeletedAtIsNull(familyId, userId);
        return new UnreadNotificationsCountResponse(count);
    }

    @Transactional
    public void markAsRead(UUID id) {
        Notification notification = notificationRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("Notificacao nao encontrada"));
        authorizationService.requireCanWrite(notification.getFamilyId(), notification.getUserId());

        notification.setRead(true);
        notification.setReadAt(LocalDateTime.now());
        notification.setUpdatedAt(LocalDateTime.now());
        notificationRepository.save(notification);
    }

    @Transactional
    public void markAllAsRead() {
        UUID familyId = authorizationService.currentFamilyId();
        UUID userId = authorizationService.currentUserId();

        List<Notification> unread = notificationRepository
                .findAllByFamilyIdAndUserIdAndReadIsFalseAndDeletedAtIsNullOrderByCreatedAtDesc(familyId, userId);

        for (Notification n : unread) {
            n.setRead(true);
            n.setReadAt(LocalDateTime.now());
            n.setUpdatedAt(LocalDateTime.now());
            notificationRepository.save(n);
        }
    }

    private NotificationResponse buildResponse(Notification n) {
        return new NotificationResponse(
                n.getId(),
                n.getFamilyId(),
                n.getUserId(),
                n.getTitle(),
                n.getMessage(),
                n.getType(),
                n.getLinkUrl(),
                n.isRead(),
                n.getReadAt(),
                n.getCreatedAt()
        );
    }
}
