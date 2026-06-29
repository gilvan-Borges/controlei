package br.com.controlei.application.mappers;

import br.com.controlei.domain.models.dtos.transaction.CreateTransactionRequest;
import br.com.controlei.domain.models.dtos.transaction.TransactionResponse;
import br.com.controlei.domain.models.dtos.transaction.UpdateTransactionRequest;
import br.com.controlei.domain.models.entities.Transaction;
import br.com.controlei.domain.models.enums.TransactionStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class TransactionMapper {

    public Transaction toEntity(CreateTransactionRequest request, UUID familyId) {
        return new Transaction(
                UUID.randomUUID(),
                familyId,
                request.userId(),
                request.accountId(),
                request.categoryId(),
                request.type(),
                request.description(),
                request.amount(),
                request.transactionDate(),
                request.dueDate(),
                null,
                TransactionStatus.PENDING,
                request.notes(),
                LocalDateTime.now(),
                LocalDateTime.now(),
                null,
                null
        );
    }

    public TransactionResponse toResponse(Transaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getFamilyId(),
                transaction.getUserId(),
                transaction.getAccountId(),
                transaction.getCategoryId(),
                transaction.getType(),
                transaction.getDescription(),
                transaction.getAmount(),
                transaction.getTransactionDate(),
                transaction.getDueDate(),
                transaction.getPaidAt(),
                transaction.getStatus(),
                transaction.getNotes(),
                transaction.getCreatedAt(),
                transaction.getUpdatedAt()
        );
    }

    public void updateEntity(Transaction transaction, UpdateTransactionRequest request) {
        transaction.setUserId(request.userId());
        transaction.setAccountId(request.accountId());
        transaction.setCategoryId(request.categoryId());
        transaction.setType(request.type());
        transaction.setDescription(request.description());
        transaction.setAmount(request.amount());
        transaction.setTransactionDate(request.transactionDate());
        transaction.setDueDate(request.dueDate());
        transaction.setNotes(request.notes());
        transaction.setUpdatedAt(LocalDateTime.now());
    }
}
