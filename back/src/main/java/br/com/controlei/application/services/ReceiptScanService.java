package br.com.controlei.application.services;

import br.com.controlei.application.exceptions.BusinessException;
import br.com.controlei.application.exceptions.NotFoundException;
import br.com.controlei.application.services.receipt.AiQuota;
import br.com.controlei.application.services.receipt.LlmReceiptExtractor;
import br.com.controlei.application.services.receipt.ReceiptExtraction;
import br.com.controlei.application.services.receipt.ReceiptFileType;
import br.com.controlei.application.services.receipt.RegexReceiptExtractor;
import br.com.controlei.domain.contracts.ai.ReceiptAiClient.ReceiptImage;
import br.com.controlei.domain.contracts.repositories.AttachmentRepositoryPort;
import br.com.controlei.domain.contracts.repositories.CategoryRepositoryPort;
import br.com.controlei.domain.contracts.repositories.ReceiptScanRepositoryPort;
import br.com.controlei.domain.models.dtos.receipt.ReceiptScanResponse;
import br.com.controlei.domain.models.dtos.receipt.SimulateScanRequest;
import br.com.controlei.domain.models.entities.Attachment;
import br.com.controlei.domain.models.entities.Category;
import br.com.controlei.domain.models.entities.ReceiptScan;
import br.com.controlei.domain.models.enums.ScanStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.io.IOException;
import java.util.Locale;
import java.util.Optional;

@Service
public class ReceiptScanService {

    private final AttachmentRepositoryPort attachmentRepository;
    private final ReceiptScanRepositoryPort receiptScanRepository;
    private final CategoryRepositoryPort categoryRepository;
    private final AuthorizationService authorizationService;
    private final LlmReceiptExtractor llmExtractor;
    private final RegexReceiptExtractor regexExtractor;
    private final AiQuota aiQuota;
    private final TransactionTemplate transactions;

    private static final long MAX_UPLOAD_BYTES = 10L * 1024 * 1024;
    /** Imagem maior que isto nao vai ao modelo: custo, latencia e a memoria do base64. */
    private static final int AI_MAX_IMAGE_BYTES = 4 * 1024 * 1024;

    public ReceiptScanService(AttachmentRepositoryPort attachmentRepository,
                              ReceiptScanRepositoryPort receiptScanRepository,
                              CategoryRepositoryPort categoryRepository,
                              AuthorizationService authorizationService,
                              LlmReceiptExtractor llmExtractor,
                              RegexReceiptExtractor regexExtractor,
                              AiQuota aiQuota,
                              TransactionTemplate transactions) {
        this.attachmentRepository = attachmentRepository;
        this.receiptScanRepository = receiptScanRepository;
        this.categoryRepository = categoryRepository;
        this.authorizationService = authorizationService;
        this.llmExtractor = llmExtractor;
        this.regexExtractor = regexExtractor;
        this.aiQuota = aiQuota;
        this.transactions = transactions;
    }

    /**
     * A chamada ao modelo pode levar ate 30 s, entao ela roda FORA da transacao: segurar uma conexao do banco
     * durante uma chamada externa esgota o pool com poucos usuarios. So o salvamento e transacional.
     */
    public ReceiptScanResponse scanReceipt(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("Arquivo do comprovante e obrigatorio");
        }
        if (file.getSize() > MAX_UPLOAD_BYTES) {
            throw new BusinessException("Arquivo excede o tamanho maximo de 10MB");
        }
        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            throw new BusinessException("Nao foi possivel ler o arquivo enviado");
        }
        // O tipo declarado pelo cliente nao prova nada: vale o que os bytes dizem.
        String type = ReceiptFileType.detect(bytes);
        if (type == null) {
            throw new BusinessException("Formato nao suportado. Use apenas JPEG, PNG ou PDF.");
        }

        UUID familyId = authorizationService.currentFamilyId();
        UUID userId = authorizationService.currentUserId();

        // PDF e imagem acima do teto nao vao ao modelo: sem dado inventado, voltam como "revisar".
        ReceiptExtraction extraction = null;
        if (ReceiptFileType.isImage(type) && bytes.length <= AI_MAX_IMAGE_BYTES && canUseAi(familyId)) {
            extraction = llmExtractor.extract(null, new ReceiptImage(bytes, type),
                    categoryNames(familyId), LocalDate.now()).orElse(null);
        }
        return persist(familyId, userId, ReceiptFileType.safeName(file.getOriginalFilename()),
                "/uploads/family_" + familyId + "/" + UUID.randomUUID(), bytes.length, type, extraction);
    }

    public ReceiptScanResponse simulateScan(SimulateScanRequest request) {
        UUID familyId = authorizationService.currentFamilyId();
        UUID userId = authorizationService.currentUserId();

        String text = request.receiptText().length() > 8000
                ? request.receiptText().substring(0, 8000) : request.receiptText();
        Optional<ReceiptExtraction> ai = canUseAi(familyId)
                ? llmExtractor.extract(text, null, categoryNames(familyId), LocalDate.now())
                : Optional.empty();
        // Plano B deterministico: sem IA, sem cota, com o disjuntor aberto ou quando a resposta nao passou na validacao.
        ReceiptExtraction extraction = ai.orElseGet(() -> regexExtractor.extract(text, LocalDate.now()));
        return persist(familyId, userId, "texto_simulado.txt", "/simulated/text", text.length(), "text/plain", extraction);
    }

    /** Ha IA, o disjuntor esta fechado e a familia ainda tem cota hoje. A cota so e gasta quando a IA vai ser chamada. */
    private boolean canUseAi(UUID familyId) {
        return llmExtractor.available() && !llmExtractor.circuitOpen() && aiQuota.tryAcquire(familyId);
    }

    private ReceiptScanResponse persist(UUID familyId, UUID userId, String fileName, String filePath, long size,
                                        String contentType, ReceiptExtraction extraction) {
        return transactions.execute(status -> {
            Attachment attachment = new Attachment(
                    UUID.randomUUID(), familyId, userId, fileName, filePath, size, contentType,
                    LocalDateTime.now(), null, null, null);
            return processExtraction(attachmentRepository.save(attachment), extraction);
        });
    }

    public ReceiptScanResponse getScan(UUID id) {
        ReceiptScan scan = receiptScanRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("Leitura de comprovante nao encontrada"));
        authorizationService.requireSameFamily(scan.getFamilyId());
        return buildResponse(scan);
    }

    public List<ReceiptScanResponse> listScans() {
        UUID familyId = authorizationService.currentFamilyId();
        return receiptScanRepository.findAllByFamilyIdAndDeletedAtIsNullOrderByCreatedAtDesc(familyId)
                .stream()
                .map(this::buildResponse)
                .toList();
    }

    private static final BigDecimal COMPLETE_THRESHOLD = new BigDecimal("0.70");

    private List<String> categoryNames(UUID familyId) {
        return categoryRepository.findAllByFamilyIdAndDeletedAtIsNull(familyId).stream()
                .map(Category::getName).toList();
    }

    private ReceiptScanResponse processExtraction(Attachment attachment, ReceiptExtraction extraction) {
        UUID familyId = attachment.getFamilyId();
        UUID userId = attachment.getUserId();

        BigDecimal amount = extraction != null ? extraction.amount() : null;
        LocalDate date = extraction != null && extraction.date() != null ? extraction.date() : LocalDate.now();
        String merchant = extraction != null ? extraction.merchant() : null;
        BigDecimal confidence = extraction != null ? extraction.confidence() : BigDecimal.ZERO.setScale(2);
        String rawText = extraction != null ? extraction.rawText() : null;

        UUID categoryId = suggestCategory(familyId, extraction != null ? extraction.categoryName() : null, merchant);
        ScanStatus status = amount != null && confidence.compareTo(COMPLETE_THRESHOLD) >= 0
                ? ScanStatus.COMPLETED : ScanStatus.NEEDS_REVIEW;

        ReceiptScan scan = new ReceiptScan(
                UUID.randomUUID(),
                attachment.getId(),
                familyId,
                userId,
                rawText,
                amount,
                date,
                merchant,
                categoryId,
                status,
                confidence,
                LocalDateTime.now(),
                null,
                null,
                null
        );
        return buildResponse(receiptScanRepository.save(scan));
    }

    /** Categoria pelo nome que a IA escolheu; sem ela, pela palavra-chave do estabelecimento. */
    private UUID suggestCategory(UUID familyId, String aiCategory, String merchant) {
        List<Category> categories = categoryRepository.findAllByFamilyIdAndDeletedAtIsNull(familyId);
        if (aiCategory != null) {
            for (Category cat : categories) {
                if (cat.getName().equalsIgnoreCase(aiCategory)) {
                    return cat.getId();
                }
            }
        }
        if (merchant == null) {
            return null;
        }
        String m = merchant.toLowerCase(Locale.ROOT);
        for (Category cat : categories) {
            String c = cat.getName().toLowerCase(Locale.ROOT);
            if (m.contains(c)
                    || (m.contains("supermercado") && c.contains("aliment"))
                    || (m.contains("posto") && c.contains("transp"))
                    || (m.contains("farmacia") && c.contains("saud"))) {
                return cat.getId();
            }
        }
        return null;
    }

    private ReceiptScanResponse buildResponse(ReceiptScan scan) {
        Attachment attachment = attachmentRepository.findByIdAndDeletedAtIsNull(scan.getAttachmentId()).orElse(null);
        Category category = scan.getSuggestedCategoryId() != null ? categoryRepository.findByIdAndDeletedAtIsNull(scan.getSuggestedCategoryId()).orElse(null) : null;

        return new ReceiptScanResponse(
                scan.getId(),
                scan.getAttachmentId(),
                attachment != null ? attachment.getFileName() : null,
                scan.getRawText(),
                scan.getExtractedAmount(),
                scan.getExtractedDate(),
                scan.getExtractedMerchant(),
                scan.getSuggestedCategoryId(),
                category != null ? category.getName() : null,
                scan.getStatus(),
                scan.getConfidenceScore(),
                scan.getCreatedAt()
        );
    }
}
