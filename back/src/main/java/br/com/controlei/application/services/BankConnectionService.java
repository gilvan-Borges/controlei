package br.com.controlei.application.services;

import br.com.controlei.application.exceptions.BusinessException;
import br.com.controlei.application.exceptions.NotFoundException;
import br.com.controlei.domain.contracts.repositories.AccountRepositoryPort;
import br.com.controlei.domain.contracts.repositories.BankConnectionRepositoryPort;
import br.com.controlei.domain.contracts.repositories.BankSyncMappingRepositoryPort;
import br.com.controlei.domain.contracts.repositories.TransactionRepositoryPort;
import br.com.controlei.domain.contracts.repositories.UserRepositoryPort;
import br.com.controlei.domain.models.dtos.openfinance.BankConnectionResponse;
import br.com.controlei.domain.models.dtos.openfinance.ConnectBankRequest;
import br.com.controlei.domain.models.dtos.openfinance.OpenFinanceWebhookPayload;
import br.com.controlei.domain.models.dtos.openfinance.SyncTransactionsResponse;
import br.com.controlei.domain.models.entities.Account;
import br.com.controlei.domain.models.entities.BankConnection;
import br.com.controlei.domain.models.entities.BankSyncMapping;
import br.com.controlei.domain.models.entities.Transaction;
import br.com.controlei.domain.models.entities.User;
import br.com.controlei.domain.models.enums.BankConnectionStatus;
import br.com.controlei.domain.models.enums.TransactionStatus;
import br.com.controlei.domain.models.enums.TransactionType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class BankConnectionService {

    private final BankConnectionRepositoryPort bankConnectionRepository;
    private final BankSyncMappingRepositoryPort bankSyncMappingRepository;
    private final AccountRepositoryPort accountRepository;
    private final TransactionRepositoryPort transactionRepository;
    private final UserRepositoryPort userRepository;
    private final AuthorizationService authorizationService;

    public BankConnectionService(BankConnectionRepositoryPort bankConnectionRepository,
                                 BankSyncMappingRepositoryPort bankSyncMappingRepository,
                                 AccountRepositoryPort accountRepository,
                                 TransactionRepositoryPort transactionRepository,
                                 UserRepositoryPort userRepository,
                                 AuthorizationService authorizationService) {
        this.bankConnectionRepository = bankConnectionRepository;
        this.bankSyncMappingRepository = bankSyncMappingRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
        this.authorizationService = authorizationService;
    }

    @Transactional
    public BankConnectionResponse connectBank(ConnectBankRequest request) {
        UUID familyId = authorizationService.currentFamilyId();
        UUID userId = authorizationService.currentUserId();

        BankConnection connection = new BankConnection(
                UUID.randomUUID(),
                familyId,
                userId,
                request.institutionId().trim(),
                request.institutionName().trim(),
                request.externalItemId().trim(),
                BankConnectionStatus.CONNECTED,
                null,
                LocalDateTime.now(),
                null,
                null,
                null
        );
        BankConnection saved = bankConnectionRepository.save(connection);

        if (request.targetAccountId() != null || request.targetCreditCardId() != null) {
            BankSyncMapping mapping = new BankSyncMapping(
                    UUID.randomUUID(),
                    saved.getId(),
                    familyId,
                    request.targetAccountId(),
                    request.targetCreditCardId(),
                    "ext_acc_" + UUID.randomUUID().toString().substring(0, 8),
                    LocalDate.now(),
                    LocalDateTime.now(),
                    null,
                    null,
                    null
            );
            bankSyncMappingRepository.save(mapping);
        }

        return buildResponse(saved);
    }

    public List<BankConnectionResponse> listConnections() {
        UUID familyId = authorizationService.currentFamilyId();
        return bankConnectionRepository.findAllByFamilyIdAndDeletedAtIsNullOrderByCreatedAtDesc(familyId)
                .stream()
                .map(this::buildResponse)
                .toList();
    }

    @Transactional
    public void disconnect(UUID connectionId) {
        BankConnection connection = bankConnectionRepository.findByIdAndDeletedAtIsNull(connectionId)
                .orElseThrow(() -> new NotFoundException("Conexao bancaria nao encontrada"));
        authorizationService.requireCanWrite(connection.getFamilyId(), connection.getUserId());

        connection.setStatus(BankConnectionStatus.DISCONNECTED);
        connection.setUpdatedAt(LocalDateTime.now());
        bankConnectionRepository.save(connection);
    }

    @Transactional
    public SyncTransactionsResponse syncConnection(UUID connectionId) {
        BankConnection connection = bankConnectionRepository.findByIdAndDeletedAtIsNull(connectionId)
                .orElseThrow(() -> new NotFoundException("Conexao bancaria nao encontrada"));
        authorizationService.requireSameFamily(connection.getFamilyId());

        if (connection.getStatus() == BankConnectionStatus.DISCONNECTED) {
            throw new BusinessException("Conexao bancaria desconectada");
        }

        List<BankSyncMapping> mappings = bankSyncMappingRepository.findAllByBankConnectionIdAndDeletedAtIsNull(connectionId);
        int imported = 0;
        int skipped = 0;

        LocalDate today = LocalDate.now();

        for (BankSyncMapping mapping : mappings) {
            if (mapping.getAccountId() != null) {
                Account account = accountRepository.findByIdAndDeletedAtIsNull(mapping.getAccountId()).orElse(null);
                if (account != null) {
                    // Transação simulada vinda do Open Finance
                    BigDecimal mockAmount = BigDecimal.valueOf(52.50);
                    String mockDesc = "Padaria Central (Open Finance)";

                    // Deduplicação: verifica se já existe transação igual no mesmo dia com mesmo valor e conta
                    List<Transaction> existing = transactionRepository.findAllByFamilyIdAndPeriod(
                            connection.getFamilyId(),
                            today,
                            today
                    );

                    boolean isDuplicate = existing.stream()
                            .anyMatch(t -> t.getAmount().compareTo(mockAmount) == 0 && t.getDescription().contains("Padaria Central"));

                    if (!isDuplicate) {
                        Transaction tx = new Transaction(
                                UUID.randomUUID(),
                                connection.getFamilyId(),
                                connection.getUserId(),
                                account.getId(),
                                null,
                                TransactionType.EXPENSE,
                                mockDesc,
                                mockAmount,
                                today,
                                today,
                                LocalDateTime.now(),
                                TransactionStatus.PAID,
                                "Importado via Open Finance",
                                LocalDateTime.now(),
                                null,
                                null,
                                null
                        );
                        transactionRepository.save(tx);
                        imported++;
                    } else {
                        skipped++;
                    }
                }
            }
        }

        connection.setLastSyncedAt(LocalDateTime.now());
        connection.setUpdatedAt(LocalDateTime.now());
        bankConnectionRepository.save(connection);

        return new SyncTransactionsResponse(
                connectionId,
                imported,
                skipped,
                "Sincronizacao concluida com sucesso"
        );
    }

    public void handleWebhook(String signature, OpenFinanceWebhookPayload payload) {
        if (payload != null && payload.itemId() != null) {
            bankConnectionRepository.findByExternalItemIdAndDeletedAtIsNull(payload.itemId())
                    .ifPresent(conn -> {
                        conn.setLastSyncedAt(LocalDateTime.now());
                        bankConnectionRepository.save(conn);
                    });
        }
    }

    private BankConnectionResponse buildResponse(BankConnection conn) {
        User user = userRepository.findByIdAndDeletedAtIsNull(conn.getUserId()).orElse(null);

        return new BankConnectionResponse(
                conn.getId(),
                conn.getFamilyId(),
                conn.getUserId(),
                user != null ? user.getName() : null,
                conn.getInstitutionId(),
                conn.getInstitutionName(),
                conn.getExternalItemId(),
                conn.getStatus(),
                conn.getLastSyncedAt(),
                conn.getCreatedAt()
        );
    }
}
