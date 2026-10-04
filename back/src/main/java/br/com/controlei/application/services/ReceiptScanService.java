package br.com.controlei.application.services;

import br.com.controlei.application.exceptions.BusinessException;
import br.com.controlei.application.exceptions.NotFoundException;
import br.com.controlei.application.services.receipt.LlmReceiptExtractor;
import br.com.controlei.application.services.receipt.ReceiptExtraction;
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
import org.springframework.transaction.annotation.Transactional;
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

    public ReceiptScanService(AttachmentRepositoryPort attachmentRepository,
                              ReceiptScanRepositoryPort receiptScanRepository,
                              CategoryRepositoryPort categoryRepository,
                              AuthorizationService authorizationService,
                              LlmReceiptExtractor llmExtractor,
                              RegexReceiptExtractor regexExtractor) {
        this.attachmentRepository = attachmentRepository;
        this.receiptScanRepository = receiptScanRepository;
        this.categoryRepository = categoryRepository;
        this.authorizationService = authorizationService;
        this.llmExtractor = llmExtractor;
        this.regexExtractor = regexExtractor;
    }

    @Transactional
    public ReceiptScanResponse scanReceipt(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("Arquivo do comprovante e obrigatorio");
        }

        String contentType = file.getContentType();
        if (contentType == null || (!contentType.equals("image/jpeg") &&
                !contentType.equals("image/png") &&
                !contentType.equals("application/pdf"))) {
            throw new BusinessException("Formato nao suportado. Use apenas JPEG, PNG ou PDF.");
        }

        long maxSize = 10 * 1024 * 1024; // 10MB
        if (file.getSize() > maxSize) {
            throw new BusinessException("Arquivo excede o tamanho maximo de 10MB");
        }

        UUID familyId = authorizationService.currentFamilyId();
        UUID userId = authorizationService.currentUserId();

        String fileName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "comprovante.jpg";
        String filePath = "/uploads/family_" + familyId + "/" + UUID.randomUUID() + "_" + fileName;

        Attachment attachment = new Attachment(
                UUID.randomUUID(),
                familyId,
                userId,
                fileName,
                filePath,
                file.getSize(),
                contentType,
                LocalDateTime.now(),
                null,
                null,
                null
        );
        Attachment savedAttachment = attachmentRepository.save(attachment);

        // Sem IA configurada (ou PDF), nao ha como ler a imagem: devolve "revisar" em vez de inventar dados.
        ReceiptExtraction extraction = null;
        boolean image = contentType.equals("image/jpeg") || contentType.equals("image/png");
        if (image && llmExtractor.available()) {
            try {
                extraction = llmExtractor.extract(null, new ReceiptImage(file.getBytes(), contentType),
                        categoryNames(familyId), LocalDate.now()).orElse(null);
            } catch (IOException e) {
                throw new BusinessException("Nao foi possivel ler o arquivo enviado");
            }
        }
        return processExtraction(savedAttachment, extraction);
    }

    @Transactional
    public ReceiptScanResponse simulateScan(SimulateScanRequest request) {
        UUID familyId = authorizationService.currentFamilyId();
        UUID userId = authorizationService.currentUserId();

        Attachment attachment = new Attachment(
                UUID.randomUUID(),
                familyId,
                userId,
                "texto_simulado.txt",
                "/simulated/text",
                request.receiptText().length(),
                "text/plain",
                LocalDateTime.now(),
                null,
                null,
                null
        );
        Attachment savedAttachment = attachmentRepository.save(attachment);

        String text = request.receiptText().length() > 8000
                ? request.receiptText().substring(0, 8000) : request.receiptText();
        Optional<ReceiptExtraction> ai = llmExtractor.extract(text, null, categoryNames(familyId), LocalDate.now());
        // Plano B deterministico: sem IA, ou quando a resposta dela nao passou na validacao.
        return processExtraction(savedAttachment, ai.orElseGet(() -> regexExtractor.extract(text, LocalDate.now())));
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
