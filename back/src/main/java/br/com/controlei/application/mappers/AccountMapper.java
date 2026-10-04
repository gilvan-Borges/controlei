package br.com.controlei.application.mappers;

import br.com.controlei.domain.models.dtos.account.AccountResponse;
import br.com.controlei.domain.models.dtos.account.CreateAccountRequest;
import br.com.controlei.domain.models.dtos.account.UpdateAccountRequest;
import br.com.controlei.domain.models.entities.Account;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class AccountMapper {

    public Account toEntity(CreateAccountRequest request, UUID familyId) {
        return new Account(
                UUID.randomUUID(),
                familyId,
                request.userId(),
                request.name(),
                request.type(),
                request.shared(),
                request.initialBalance(),
                true,
                LocalDateTime.now(),
                LocalDateTime.now(),
                null,
                null
        );
    }

    public AccountResponse toResponse(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getFamilyId(),
                account.getUserId(),
                account.getName(),
                account.getType(),
                account.isShared(),
                account.getInitialBalance(),
                account.isActive(),
                account.getCreatedAt(),
                account.getUpdatedAt()
        );
    }

    public void updateEntity(Account account, UpdateAccountRequest request) {
        account.setName(request.name());
        account.setType(request.type());
        account.setShared(request.shared());
        account.setUserId(request.userId());
        account.setInitialBalance(request.initialBalance());
        if (request.active() != null) {
            account.setActive(request.active());
        }
        account.setUpdatedAt(LocalDateTime.now());
    }
}
