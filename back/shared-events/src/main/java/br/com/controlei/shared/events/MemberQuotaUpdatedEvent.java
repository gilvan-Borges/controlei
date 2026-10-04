package br.com.controlei.shared.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberQuotaUpdatedEvent implements DomainEvent {
    @Builder.Default
    private UUID eventId = UUID.randomUUID();

    @Builder.Default
    private Instant occurredOn = Instant.now();

    @Builder.Default
    private String eventType = "MEMBER_QUOTA_UPDATED";

    private UUID familyId;
    private int baseMembersAllowed;
    private int extraMembersPaid;
    private int totalAllowedMembers;
}
