package br.com.controlei.application.controllers;

import br.com.controlei.application.services.ReceiptScanService;
import br.com.controlei.domain.models.dtos.receipt.ReceiptScanResponse;
import br.com.controlei.domain.models.dtos.receipt.SimulateScanRequest;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/receipts")
public class ReceiptController {

    private final ReceiptScanService receiptScanService;

    public ReceiptController(ReceiptScanService receiptScanService) {
        this.receiptScanService = receiptScanService;
    }

    @PostMapping(value = "/scan", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ReceiptScanResponse> scanReceipt(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(receiptScanService.scanReceipt(file));
    }

    @PostMapping("/simulate-scan")
    public ResponseEntity<ReceiptScanResponse> simulateScan(@Valid @RequestBody SimulateScanRequest request) {
        return ResponseEntity.ok(receiptScanService.simulateScan(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReceiptScanResponse> getScan(@PathVariable UUID id) {
        return ResponseEntity.ok(receiptScanService.getScan(id));
    }

    @GetMapping
    public ResponseEntity<List<ReceiptScanResponse>> listScans() {
        return ResponseEntity.ok(receiptScanService.listScans());
    }
}
