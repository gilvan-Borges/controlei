package br.com.controlei.application.controllers;

import br.com.controlei.application.services.AccountService;
import br.com.controlei.domain.models.dtos.account.AccountQueryFilter;
import br.com.controlei.domain.models.dtos.account.AccountResponse;
import br.com.controlei.domain.models.dtos.account.CreateAccountRequest;
import br.com.controlei.domain.models.dtos.account.UpdateAccountRequest;
import br.com.controlei.domain.models.enums.AccountType;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping
    public ResponseEntity<List<AccountResponse>> listAccounts(
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) AccountType type,
            @RequestParam(required = false) Boolean shared,
            @RequestParam(required = false) UUID userId) {
        AccountQueryFilter filter = new AccountQueryFilter(active, type, shared, userId);
        return ResponseEntity.ok(accountService.listAccounts(filter));
    }

    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(@Valid @RequestBody CreateAccountRequest request) {
        return ResponseEntity.ok(accountService.createAccount(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountResponse> getAccount(@PathVariable UUID id) {
        return ResponseEntity.ok(accountService.getAccount(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AccountResponse> updateAccount(@PathVariable UUID id,
                                                         @Valid @RequestBody UpdateAccountRequest request) {
        return ResponseEntity.ok(accountService.updateAccount(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAccount(@PathVariable UUID id) {
        accountService.deleteAccount(id);
        return ResponseEntity.noContent().build();
    }
}
