package com.esse.crm.controller;

import com.esse.crm.dto.AccountDTO;
import com.esse.crm.dto.ContactDTO;
import com.esse.crm.dto.activity.ActivityDTO;
import com.esse.crm.dto.activity.ActivityType;
import com.esse.crm.service.AccountService;
import com.esse.crm.service.ActivityService;
import com.esse.crm.service.ContactService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;
    private final ContactService contactService;
    private final ActivityService activityService;

    @GetMapping
    @PreAuthorize("hasAuthority('ACCOUNT_READ')")
    public Page<AccountDTO> getAllAccounts(
            @RequestParam(required = false) String search,
            Pageable pageable) {
        return accountService.getAllAccounts(search, pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ACCOUNT_READ')")
    public ResponseEntity<AccountDTO> getAccountById(@PathVariable Long id) {
        return ResponseEntity.ok(accountService.getAccountById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ACCOUNT_WRITE')")
    public ResponseEntity<AccountDTO> createAccount(@Valid @RequestBody AccountDTO accountDTO) {
        return new ResponseEntity<>(accountService.createAccount(accountDTO), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ACCOUNT_WRITE')")
    public ResponseEntity<AccountDTO> updateAccount(@PathVariable Long id, @Valid @RequestBody AccountDTO accountDTO) {
        return ResponseEntity.ok(accountService.updateAccount(id, accountDTO));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteAccount(@PathVariable Long id) {
        accountService.deleteAccount(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/contacts")
    public Page<ContactDTO> getAccountContacts(
            @PathVariable Long id,
            Pageable pageable) {
        return contactService.getContactsByAccount(id, pageable);
    }

    @GetMapping("/{id}/activities")
    public Page<ActivityDTO> getAccountActivities(
            @PathVariable Long id,
            @RequestParam(required = false) ActivityType type,
            @RequestParam(required = false) Boolean completed,
            Pageable pageable) {
        return activityService.searchActivities(completed, type, null, null, null, null, id, null, pageable);
    }
}
