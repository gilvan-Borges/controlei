package br.com.controlei.application.services;

import br.com.controlei.application.exceptions.BusinessException;
import br.com.controlei.application.exceptions.NotFoundException;
import br.com.controlei.domain.contracts.repositories.AccountRepositoryPort;
import br.com.controlei.domain.contracts.repositories.CategoryRepositoryPort;
import br.com.controlei.domain.contracts.repositories.CreditCardRepositoryPort;
import br.com.controlei.domain.contracts.repositories.CreditCardTransactionRepositoryPort;
import br.com.controlei.domain.contracts.repositories.InvoiceRepositoryPort;
import br.com.controlei.domain.contracts.repositories.TransactionRepositoryPort;
import br.com.controlei.domain.contracts.repositories.UserRepositoryPort;
import br.com.controlei.domain.models.dtos.card.CreateCardExpenseRequest;
import br.com.controlei.domain.models.dtos.card.CreateCreditCardRequest;
import br.com.controlei.domain.models.dtos.card.CreditCardResponse;
import br.com.controlei.domain.models.dtos.card.CreditCardTransactionResponse;
import br.com.controlei.domain.models.dtos.card.InvoiceResponse;
import br.com.controlei.domain.models.dtos.card.PayInvoiceRequest;
import br.com.controlei.domain.models.dtos.card.UpdateCreditCardRequest;
import br.com.controlei.domain.models.entities.Account;
import br.com.controlei.domain.models.entities.Category;
import br.com.controlei.domain.models.entities.CreditCard;
import br.com.controlei.domain.models.entities.CreditCardTransaction;
import br.com.controlei.domain.models.entities.Invoice;
import br.com.controlei.domain.models.entities.Transaction;
import br.com.controlei.domain.models.entities.User;
import br.com.controlei.domain.models.enums.CategoryType;
import br.com.controlei.domain.models.enums.InvoiceStatus;
import br.com.controlei.domain.models.enums.TransactionStatus;
import br.com.controlei.domain.models.enums.TransactionType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CreditCardService {

    private final CreditCardRepositoryPort creditCardRepository;
    private final InvoiceRepositoryPort invoiceRepository;
    private final CreditCardTransactionRepositoryPort creditCardTransactionRepository;
    private final UserRepositoryPort userRepository;
    private final CategoryRepositoryPort categoryRepository;
    private final AccountRepositoryPort accountRepository;
    private final TransactionRepositoryPort transactionRepository;
    private final AuthorizationService authorizationService;

    public CreditCardService(CreditCardRepositoryPort creditCardRepository,
                             InvoiceRepositoryPort invoiceRepository,
                             CreditCardTransactionRepositoryPort creditCardTransactionRepository,
                             UserRepositoryPort userRepository,
                             CategoryRepositoryPort categoryRepository,
                             AccountRepositoryPort accountRepository,
                             TransactionRepositoryPort transactionRepository,
                             AuthorizationService authorizationService) {
        this.creditCardRepository = creditCardRepository;
        this.invoiceRepository = invoiceRepository;
        this.creditCardTransactionRepository = creditCardTransactionRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.authorizationService = authorizationService;
    }

    @Transactional
    public CreditCardResponse createCreditCard(CreateCreditCardRequest request) {
        UUID familyId = authorizationService.currentFamilyId();
        UUID targetUserId = request.userId() != null ? request.userId() : authorizationService.currentUserId();

        authorizationService.requireCanWrite(familyId, targetUserId);

        CreditCard card = new CreditCard(
                UUID.randomUUID(),
                familyId,
                targetUserId,
                request.name().trim(),
                request.lastDigits() != null ? request.lastDigits().trim() : null,
                request.brand() != null ? request.brand().trim() : null,
                request.closingDay(),
                request.dueDay(),
                request.creditLimit(),
                true,
                LocalDateTime.now(),
                null,
                null,
                null
        );

        CreditCard saved = creditCardRepository.save(card);
        User user = userRepository.findByIdAndDeletedAtIsNull(targetUserId).orElse(null);
        return toResponse(saved, user != null ? user.getName() : null, BigDecimal.ZERO, saved.getCreditLimit());
    }

    @Transactional
    public CreditCardResponse updateCreditCard(UUID id, UpdateCreditCardRequest request) {
        CreditCard card = findCardOrThrow(id);
        authorizationService.requireCanWrite(card.getFamilyId(), card.getUserId());

        card.setName(request.name().trim());
        card.setLastDigits(request.lastDigits() != null ? request.lastDigits().trim() : null);
        card.setBrand(request.brand() != null ? request.brand().trim() : null);
        card.setClosingDay(request.closingDay());
        card.setDueDay(request.dueDay());
        card.setCreditLimit(request.creditLimit());
        if (request.active() != null) {
            card.setActive(request.active());
        }
        card.setUpdatedAt(LocalDateTime.now());

        CreditCard updated = creditCardRepository.save(card);
        return buildCreditCardResponse(updated);
    }

    public CreditCardResponse getCreditCard(UUID id) {
        CreditCard card = findCardOrThrow(id);
        authorizationService.requireSameFamily(card.getFamilyId());
        return buildCreditCardResponse(card);
    }

    public List<CreditCardResponse> listCreditCards() {
        UUID familyId = authorizationService.currentFamilyId();
        List<CreditCard> cards;

        if (authorizationService.isResponsible()) {
            cards = creditCardRepository.findAllByFamilyIdAndDeletedAtIsNullOrderByCreatedAtDesc(familyId);
        } else {
            cards = creditCardRepository.findAllByFamilyIdAndUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(familyId, authorizationService.currentUserId());
        }

        return cards.stream()
                .map(this::buildCreditCardResponse)
                .toList();
    }

    @Transactional
    public void deleteCreditCard(UUID id) {
        CreditCard card = findCardOrThrow(id);
        authorizationService.requireCanWrite(card.getFamilyId(), card.getUserId());

        card.setDeletedAt(LocalDateTime.now());
        card.setDeletedBy(authorizationService.currentUserId().toString());
        creditCardRepository.save(card);
    }

    @Transactional
    public List<CreditCardTransactionResponse> addExpense(UUID cardId, CreateCardExpenseRequest request) {
        CreditCard card = findCardOrThrow(cardId);
        authorizationService.requireCanWrite(card.getFamilyId(), card.getUserId());

        if (request.categoryId() != null) {
            Category category = categoryRepository.findByIdAndDeletedAtIsNull(request.categoryId())
                    .orElseThrow(() -> new NotFoundException("Categoria nao encontrada"));
            if (!Objects.equals(category.getFamilyId(), card.getFamilyId())) {
                throw new NotFoundException("Categoria nao encontrada");
            }
            if (category.getType() != CategoryType.EXPENSE) {
                throw new BusinessException("Categoria deve ser do tipo DESPESA");
            }
        }

        int installments = request.totalInstallments() != null && request.totalInstallments() > 0 ? request.totalInstallments() : 1;
        BigDecimal totalAmount = request.amount();
        BigDecimal installmentAmount = totalAmount.divide(BigDecimal.valueOf(installments), 2, RoundingMode.HALF_EVEN);
        BigDecimal remainder = totalAmount.subtract(installmentAmount.multiply(BigDecimal.valueOf(installments)));

        // Determina o mês de referência da 1ª parcela
        LocalDate transDate = request.transactionDate();
        YearMonth startMonth = YearMonth.from(transDate);
        if (transDate.getDayOfMonth() > card.getClosingDay()) {
            startMonth = startMonth.plusMonths(1);
        }

        List<CreditCardTransactionResponse> createdTransactions = new ArrayList<>();

        for (int i = 1; i <= installments; i++) {
            YearMonth refMonth = startMonth.plusMonths(i - 1);
            LocalDate refDate = refMonth.atDay(1);

            Invoice invoice = getOrCreateInvoice(card, refDate);

            BigDecimal currentInstallmentAmount = (i == 1) ? installmentAmount.add(remainder) : installmentAmount;

            CreditCardTransaction transaction = new CreditCardTransaction(
                    UUID.randomUUID(),
                    card.getId(),
                    invoice.getId(),
                    card.getFamilyId(),
                    card.getUserId(),
                    request.categoryId(),
                    request.description().trim(),
                    currentInstallmentAmount,
                    request.transactionDate(),
                    i,
                    installments,
                    LocalDateTime.now(),
                    null,
                    null,
                    null
            );

            CreditCardTransaction saved = creditCardTransactionRepository.save(transaction);

            // Atualiza total da fatura
            invoice.setTotalAmount(invoice.getTotalAmount().add(currentInstallmentAmount));
            invoiceRepository.save(invoice);

            createdTransactions.add(toTransactionResponse(saved));
        }

        return createdTransactions;
    }

    public List<InvoiceResponse> listInvoices(UUID cardId) {
        CreditCard card = findCardOrThrow(cardId);
        authorizationService.requireSameFamily(card.getFamilyId());

        List<Invoice> invoices = invoiceRepository.findAllByCreditCardIdAndDeletedAtIsNullOrderByReferenceMonthDesc(cardId);
        return invoices.stream()
                .map(inv -> toInvoiceResponse(inv, card))
                .toList();
    }

    public InvoiceResponse getInvoice(UUID invoiceId) {
        Invoice invoice = invoiceRepository.findByIdAndDeletedAtIsNull(invoiceId)
                .orElseThrow(() -> new NotFoundException("Fatura nao encontrada"));
        authorizationService.requireSameFamily(invoice.getFamilyId());

        CreditCard card = creditCardRepository.findByIdAndDeletedAtIsNull(invoice.getCreditCardId())
                .orElseThrow(() -> new NotFoundException("Cartao nao encontrado"));

        return toInvoiceResponse(invoice, card);
    }

    @Transactional
    public InvoiceResponse payInvoice(UUID invoiceId, PayInvoiceRequest request) {
        Invoice invoice = invoiceRepository.findByIdAndDeletedAtIsNull(invoiceId)
                .orElseThrow(() -> new NotFoundException("Fatura nao encontrada"));
        authorizationService.requireCanWrite(invoice.getFamilyId(), invoice.getUserId());

        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw new BusinessException("Esta fatura ja foi paga");
        }

        Account account = accountRepository.findByIdAndDeletedAtIsNull(request.accountId())
                .orElseThrow(() -> new NotFoundException("Conta nao encontrada"));
        authorizationService.requireSameFamily(account.getFamilyId());

        BigDecimal amountToPay = request.amount() != null ? request.amount() : invoice.getTotalAmount();
        LocalDate payDate = request.paymentDate() != null ? request.paymentDate() : LocalDate.now();

        // 1. Cria transação de despesa na conta selecionada
        CreditCard card = creditCardRepository.findByIdAndDeletedAtIsNull(invoice.getCreditCardId()).orElse(null);
        String cardName = card != null ? card.getName() : "Cartao";

        Transaction paymentTx = new Transaction(
                UUID.randomUUID(),
                invoice.getFamilyId(),
                invoice.getUserId(),
                account.getId(),
                null,
                TransactionType.EXPENSE,
                "Pagamento Fatura " + cardName + " - " + invoice.getReferenceMonth().getMonthValue() + "/" + invoice.getReferenceMonth().getYear(),
                amountToPay,
                payDate,
                payDate,
                LocalDateTime.now(),
                TransactionStatus.PAID,
                null,
                LocalDateTime.now(),
                null,
                null,
                null
        );
        transactionRepository.save(paymentTx);

        // 2. Atualiza fatura para PAID
        invoice.setPaidAmount(amountToPay);
        invoice.setStatus(InvoiceStatus.PAID);
        invoice.setPaidAt(LocalDateTime.now());
        Invoice updated = invoiceRepository.save(invoice);

        return toInvoiceResponse(updated, card);
    }

    private Invoice getOrCreateInvoice(CreditCard card, LocalDate referenceMonth) {
        return invoiceRepository.findByCreditCardIdAndReferenceMonthAndDeletedAtIsNull(card.getId(), referenceMonth)
                .orElseGet(() -> {
                    YearMonth ym = YearMonth.from(referenceMonth);
                    LocalDate dueDate;
                    if (card.getDueDay() < card.getClosingDay()) {
                        YearMonth nextYm = ym.plusMonths(1);
                        dueDate = nextYm.atDay(Math.min(card.getDueDay(), nextYm.lengthOfMonth()));
                    } else {
                        dueDate = ym.atDay(Math.min(card.getDueDay(), ym.lengthOfMonth()));
                    }

                    Invoice newInvoice = new Invoice(
                            UUID.randomUUID(),
                            card.getId(),
                            card.getFamilyId(),
                            card.getUserId(),
                            referenceMonth,
                            BigDecimal.ZERO,
                            BigDecimal.ZERO,
                            InvoiceStatus.OPEN,
                            dueDate,
                            null,
                            LocalDateTime.now(),
                            null,
                            null,
                            null
                    );
                    return invoiceRepository.save(newInvoice);
                });
    }

    private CreditCard findCardOrThrow(UUID id) {
        return creditCardRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("Cartao nao encontrado"));
    }

    private CreditCardResponse buildCreditCardResponse(CreditCard card) {
        User user = userRepository.findByIdAndDeletedAtIsNull(card.getUserId()).orElse(null);
        List<Invoice> invoices = invoiceRepository.findAllByCreditCardIdAndDeletedAtIsNullOrderByReferenceMonthDesc(card.getId());

        BigDecimal totalUnpaid = invoices.stream()
                .filter(inv -> inv.getStatus() != InvoiceStatus.PAID)
                .map(Invoice::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal availableLimit = card.getCreditLimit().subtract(totalUnpaid);
        if (availableLimit.compareTo(BigDecimal.ZERO) < 0) {
            availableLimit = BigDecimal.ZERO;
        }

        LocalDate currentRefMonth = YearMonth.now().atDay(1);
        BigDecimal currentInvoiceAmount = invoices.stream()
                .filter(inv -> inv.getReferenceMonth().equals(currentRefMonth))
                .map(Invoice::getTotalAmount)
                .findFirst()
                .orElse(BigDecimal.ZERO);

        return toResponse(card, user != null ? user.getName() : null, currentInvoiceAmount, availableLimit);
    }

    private CreditCardResponse toResponse(CreditCard card, String userName, BigDecimal currentInvoiceAmount, BigDecimal availableLimit) {
        return new CreditCardResponse(
                card.getId(),
                card.getFamilyId(),
                card.getUserId(),
                userName,
                card.getName(),
                card.getLastDigits(),
                card.getBrand(),
                card.getClosingDay(),
                card.getDueDay(),
                card.getCreditLimit(),
                availableLimit,
                currentInvoiceAmount,
                card.isActive(),
                card.getCreatedAt(),
                card.getUpdatedAt()
        );
    }

    private InvoiceResponse toInvoiceResponse(Invoice invoice, CreditCard card) {
        User user = userRepository.findByIdAndDeletedAtIsNull(invoice.getUserId()).orElse(null);
        List<CreditCardTransaction> txs = creditCardTransactionRepository.findAllByInvoiceIdAndDeletedAtIsNullOrderByTransactionDateAsc(invoice.getId());

        List<CreditCardTransactionResponse> txResponses = txs.stream()
                .map(this::toTransactionResponse)
                .toList();

        return new InvoiceResponse(
                invoice.getId(),
                invoice.getCreditCardId(),
                card != null ? card.getName() : "Cartao",
                invoice.getFamilyId(),
                invoice.getUserId(),
                user != null ? user.getName() : null,
                invoice.getReferenceMonth(),
                invoice.getTotalAmount(),
                invoice.getPaidAmount(),
                invoice.getStatus(),
                invoice.getDueDate(),
                invoice.getPaidAt(),
                txResponses,
                invoice.getCreatedAt()
        );
    }

    private CreditCardTransactionResponse toTransactionResponse(CreditCardTransaction tx) {
        String categoryName = null;
        if (tx.getCategoryId() != null) {
            Category cat = categoryRepository.findByIdAndDeletedAtIsNull(tx.getCategoryId()).orElse(null);
            categoryName = cat != null ? cat.getName() : null;
        }

        return new CreditCardTransactionResponse(
                tx.getId(),
                tx.getCreditCardId(),
                tx.getInvoiceId(),
                tx.getFamilyId(),
                tx.getUserId(),
                tx.getCategoryId(),
                categoryName,
                tx.getDescription(),
                tx.getAmount(),
                tx.getTransactionDate(),
                tx.getInstallmentNumber(),
                tx.getTotalInstallments(),
                tx.getCreatedAt()
        );
    }
}
