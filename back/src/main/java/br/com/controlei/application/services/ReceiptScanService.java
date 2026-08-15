package br.com.controlei.application.services;

import br.com.controlei.application.exceptions.BusinessException;
import br.com.controlei.application.exceptions.NotFoundException;
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
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ReceiptScanService {

    private final AttachmentRepositoryPort attachmentRepository;
    private final ReceiptScanRepositoryPort receiptScanRepository;
    private final CategoryRepositoryPort categoryRepository;
    private final AuthorizationService authorizationService;

    public ReceiptScanService(AttachmentRepositoryPort attachmentRepository,
                              ReceiptScanRepositoryPort receiptScanRepository,
                              CategoryRepositoryPort categoryRepository,
                              AuthorizationService authorizationService) {
        this.attachmentRepository = attachmentRepository;
        this.receiptScanRepository = receiptScanRepository;
        this.categoryRepository = categoryRepository;
        this.authorizationService = authorizationService;
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

        // Simulação OCR / Extração de Texto Inteligente a partir do arquivo
        String mockText = "COMPROVANTE DE PAGAMENTO PIX\n" +
                "Estabelecimento: Pao de Acucar Supermercados\n" +
                "Valor: R$ 145,80\n" +
                "Data: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + "\n" +
                "Autenticacao: 8F7E.992B.112A";

        return processExtraction(savedAttachment, mockText);
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

        return processExtraction(savedAttachment, request.receiptText());
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

    private ReceiptScanResponse processExtraction(Attachment attachment, String rawText) {
        UUID familyId = attachment.getFamilyId();
        UUID userId = attachment.getUserId();

        ExtractedData data = extractEntities(rawText, familyId);

        ReceiptScan scan = new ReceiptScan(
                UUID.randomUUID(),
                attachment.getId(),
                familyId,
                userId,
                rawText,
                data.amount,
                data.date,
                data.merchant,
                data.suggestedCategoryId,
                ScanStatus.COMPLETED,
                BigDecimal.valueOf(0.95),
                LocalDateTime.now(),
                null,
                null,
                null
        );
        ReceiptScan savedScan = receiptScanRepository.save(scan);

        return buildResponse(savedScan);
    }

    private ExtractedData extractEntities(String text, UUID familyId) {
        BigDecimal amount = null;
        LocalDate date = LocalDate.now();
        String merchant = "Estabelecimento";
        UUID suggestedCategoryId = null;

        // 1. Extração de Valor com Regex (ex: R$ 145,80 ou 145.80)
        Pattern amountPattern = Pattern.compile("(?:R\\$\\s*|Valor:\\s*R?\\$?\\s*)(\\d+[.,]\\d{2})", Pattern.CASE_INSENSITIVE);
        Matcher amountMatcher = amountPattern.matcher(text);
        if (amountMatcher.find()) {
            String valStr = amountMatcher.group(1).replace(".", "").replace(",", ".");
            try {
                amount = new BigDecimal(valStr);
            } catch (Exception ignored) {}
        }

        // 2. Extração de Estabelecimento
        Pattern merchantPattern = Pattern.compile("(?:Estabelecimento|Favorecido|Destinatario|Loja|Beneficiario):\\s*([^\\n]+)", Pattern.CASE_INSENSITIVE);
        Matcher merchantMatcher = merchantPattern.matcher(text);
        if (merchantMatcher.find()) {
            merchant = merchantMatcher.group(1).trim();
        } else {
            String[] lines = text.split("\n");
            for (String line : lines) {
                if (line.toLowerCase().contains("supermercado") || line.toLowerCase().contains("mercado") ||
                        line.toLowerCase().contains("posto") || line.toLowerCase().contains("farmacia") ||
                        line.toLowerCase().contains("restaurante")) {
                    merchant = line.trim();
                    break;
                }
            }
        }

        // 3. Sugestão de categoria baseada nas categorias da família
        List<Category> categories = categoryRepository.findAllByFamilyIdAndDeletedAtIsNull(familyId);
        String merchantLower = merchant.toLowerCase();
        for (Category cat : categories) {
            String catLower = cat.getName().toLowerCase();
            if (merchantLower.contains(catLower) ||
                    (merchantLower.contains("supermercado") && catLower.contains("aliment")) ||
                    (merchantLower.contains("posto") && catLower.contains("transp")) ||
                    (merchantLower.contains("farmacia") && catLower.contains("saud"))) {
                suggestedCategoryId = cat.getId();
                break;
            }
        }

        return new ExtractedData(amount, date, merchant, suggestedCategoryId);
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

    private record ExtractedData(BigDecimal amount, LocalDate date, String merchant, UUID suggestedCategoryId) {}
}
