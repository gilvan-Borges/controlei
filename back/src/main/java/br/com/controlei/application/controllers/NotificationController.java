package br.com.controlei.application.controllers;

import br.com.controlei.application.services.NotificationService;
import br.com.controlei.domain.models.dtos.notification.CreateNotificationRequest;
import br.com.controlei.domain.models.dtos.notification.NotificationResponse;
import br.com.controlei.domain.models.dtos.notification.UnreadNotificationsCountResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping
    public ResponseEntity<NotificationResponse> createNotification(@Valid @RequestBody CreateNotificationRequest request) {
        return ResponseEntity.ok(notificationService.createNotification(request));
    }

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> listNotifications(
            @RequestParam(required = false, defaultValue = "false") boolean unreadOnly) {
        return ResponseEntity.ok(notificationService.listNotifications(unreadOnly));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<UnreadNotificationsCountResponse> getUnreadCount() {
        return ResponseEntity.ok(notificationService.getUnreadCount());
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable UUID id) {
        notificationService.markAsRead(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/mark-all-read")
    public ResponseEntity<Void> markAllAsRead() {
        notificationService.markAllAsRead();
        return ResponseEntity.noContent().build();
    }
}
