package br.com.controlei.application.controllers;

import br.com.controlei.application.services.CreditCardService;
import br.com.controlei.domain.models.dtos.card.CreateCardExpenseRequest;
import br.com.controlei.domain.models.dtos.card.CreateCreditCardRequest;
import br.com.controlei.domain.models.dtos.card.CreditCardResponse;
import br.com.controlei.domain.models.dtos.card.CreditCardTransactionResponse;
import br.com.controlei.domain.models.dtos.card.InvoiceResponse;
import br.com.controlei.domain.models.dtos.card.PayInvoiceRequest;
import br.com.controlei.domain.models.dtos.card.UpdateCreditCardRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/credit-cards")
public class CreditCardController {

    private final CreditCardService creditCardService;

    public CreditCardController(CreditCardService creditCardService) {
        this.creditCardService = creditCardService;
    }

    @PostMapping
    public ResponseEntity<CreditCardResponse> create(@Valid @RequestBody CreateCreditCardRequest request) {
        return ResponseEntity.ok(creditCardService.createCreditCard(request));
    }

    @GetMapping
    public ResponseEntity<List<CreditCardResponse>> list() {
        return ResponseEntity.ok(creditCardService.listCreditCards());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CreditCardResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(creditCardService.getCreditCard(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CreditCardResponse> update(@PathVariable UUID id, @Valid @RequestBody UpdateCreditCardRequest request) {
        return ResponseEntity.ok(creditCardService.updateCreditCard(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        creditCardService.deleteCreditCard(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/expenses")
    public ResponseEntity<List<CreditCardTransactionResponse>> addExpense(@PathVariable UUID id, @Valid @RequestBody CreateCardExpenseRequest request) {
        return ResponseEntity.ok(creditCardService.addExpense(id, request));
    }

    @GetMapping("/{id}/invoices")
    public ResponseEntity<List<InvoiceResponse>> listInvoices(@PathVariable UUID id) {
        return ResponseEntity.ok(creditCardService.listInvoices(id));
    }

    @GetMapping("/invoices/{invoiceId}")
    public ResponseEntity<InvoiceResponse> getInvoice(@PathVariable UUID invoiceId) {
        return ResponseEntity.ok(creditCardService.getInvoice(invoiceId));
    }

    @PostMapping("/invoices/{invoiceId}/pay")
    public ResponseEntity<InvoiceResponse> payInvoice(@PathVariable UUID invoiceId, @Valid @RequestBody PayInvoiceRequest request) {
        return ResponseEntity.ok(creditCardService.payInvoice(invoiceId, request));
    }
}
