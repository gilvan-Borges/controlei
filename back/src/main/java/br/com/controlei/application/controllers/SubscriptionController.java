package br.com.controlei.application.controllers;

import br.com.controlei.application.services.SubscriptionService;
import br.com.controlei.domain.models.dtos.subscription.PlanDetailsResponse;
import br.com.controlei.domain.models.dtos.subscription.SubscriptionResponse;
import br.com.controlei.domain.models.dtos.subscription.UpgradeSubscriptionRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/subscriptions")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    public SubscriptionController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @GetMapping("/current")
    public ResponseEntity<SubscriptionResponse> getCurrentSubscription() {
        return ResponseEntity.ok(subscriptionService.getFamilySubscription());
    }

    @GetMapping("/plans")
    public ResponseEntity<List<PlanDetailsResponse>> getAvailablePlans() {
        return ResponseEntity.ok(subscriptionService.getAvailablePlans());
    }

    @PostMapping("/upgrade")
    public ResponseEntity<SubscriptionResponse> upgrade(@Valid @RequestBody UpgradeSubscriptionRequest request) {
        return ResponseEntity.ok(subscriptionService.upgradeSubscription(request));
    }
}
