package com.esse.crm.service;

import com.esse.crm.dto.AccountDTO;
import com.esse.crm.dto.AccountStatus;
import com.esse.crm.dto.ContactDTO;
import com.esse.crm.dto.activity.ActivityDTO;
import com.esse.crm.dto.activity.ActivityType;
import com.esse.crm.entity.Account;
import com.esse.crm.entity.Activity;
import com.esse.crm.entity.Contact;
import com.esse.crm.exception.ResourceNotFoundException;
import com.esse.crm.repository.AccountRepository;
import com.esse.crm.repository.ActivityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final ActivityRepository activityRepository;

    public Page<AccountDTO> getAllAccounts(String searchTerm, Pageable pageable) {
        return accountRepository.searchAccounts(searchTerm, pageable)
                .map(this::convertToDTO);
    }

    public AccountDTO getAccountById(Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with id: " + id));
        return convertToDTO(account);
    }

    @Transactional
    public AccountDTO createAccount(AccountDTO accountDTO) {
        Account account = convertToEntity(accountDTO);
        Account savedAccount = accountRepository.save(account);

        Activity autoActivity = Activity.builder()
                .type(ActivityType.NOTE)
                .subject("Account Created")
                .description("A new account is created: " + savedAccount.getAccountName())
                .accountId(savedAccount.getId())
                .completed(true)
                .build();
        activityRepository.save(autoActivity);

        return convertToDTO(savedAccount);
    }

    @Transactional
    public AccountDTO updateAccount(Long id, AccountDTO accountDTO) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with id: " + id));
        
        account.setAccountName(accountDTO.getAccountName());
        account.setIndustry(accountDTO.getIndustry());
        account.setWebsite(accountDTO.getWebsite());
        account.setPhone(accountDTO.getPhone());
        account.setBillingAddress(accountDTO.getBillingAddress());
        account.setShippingAddress(accountDTO.getShippingAddress());
        account.setStatus(accountDTO.getStatus());
        
        Account updatedAccount = accountRepository.save(account);

        Activity autoActivity = Activity.builder()
                .type(ActivityType.NOTE)
                .subject("Account Updated")
                .description("Account " + updatedAccount.getAccountName() + " was updated")
                .accountId(updatedAccount.getId())
                .completed(true)
                .build();
        activityRepository.save(autoActivity);

        return convertToDTO(updatedAccount);
    }

    @Transactional
    public void deleteAccount(Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with id: " + id));

        accountRepository.delete(account);
    }

    private AccountDTO convertToDTO(Account account) {
        return AccountDTO.builder()
                .id(account.getId())
                .accountName(account.getAccountName())
                .industry(account.getIndustry())
                .website(account.getWebsite())
                .phone(account.getPhone())
                .billingAddress(account.getBillingAddress())
                .shippingAddress(account.getShippingAddress())
                .status(account.getStatus())
                .contacts(account.getContacts() != null ? account.getContacts().stream()
                        .map(this::convertContactToDTO)
                        .collect(Collectors.toList()) : null)
                .activities(account.getActivities() != null ? account.getActivities().stream()
                        .map(this::convertActivityToDTO)
                        .collect(Collectors.toList()) : null)
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .build();
    }

    private ContactDTO convertContactToDTO(Contact contact) {
        return ContactDTO.builder()
                .id(contact.getId())
                .firstName(contact.getFirstName())
                .lastName(contact.getLastName())
                .email(contact.getEmail())
                .phone(contact.getPhone())
                .jobTitle(contact.getJobTitle())
                .isPrimaryContact(contact.isPrimaryContact())
                .accountIds(contact.getAccounts() != null ? contact.getAccounts().stream()
                        .map(Account::getId)
                        .collect(Collectors.toList()) : null)
                .createdAt(contact.getCreatedAt())
                .updatedAt(contact.getUpdatedAt())
                .build();
    }

    private ActivityDTO convertActivityToDTO(Activity entity) {
        return ActivityDTO.builder()
                .id(entity.getId())
                .type(entity.getType())
                .subject(entity.getSubject())
                .description(entity.getDescription())
                .dueAt(entity.getDueAt())
                .completed(entity.isCompleted())
                .outcome(entity.getOutcome())
                .leadId(entity.getLeadId())
                .opportunityId(entity.getOpportunityId())
                .accountId(entity.getAccountId())
                .contactId(entity.getContactId())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private Account convertToEntity(AccountDTO dto) {
        return Account.builder()
                .id(dto.getId())
                .accountName(dto.getAccountName())
                .industry(dto.getIndustry())
                .website(dto.getWebsite())
                .phone(dto.getPhone())
                .billingAddress(dto.getBillingAddress())
                .shippingAddress(dto.getShippingAddress())
                .status(dto.getStatus())
                .build();
    }
}
