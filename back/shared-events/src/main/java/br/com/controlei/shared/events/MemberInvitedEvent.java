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
public class MemberInvitedEvent implements DomainEvent {
    @Builder.Default
    private UUID eventId = UUID.randomUUID();

    @Builder.Default
    private Instant occurredOn = Instant.now();

    @Builder.Default
    private String eventType = "MEMBER_INVITED";

    private UUID familyId;
    private String familyName;
    private String memberEmail;
    private String memberName;
    private String role;
    private String inviteToken;
    private UUID invitedByUserId;
    private String invitedByUserName;
}
