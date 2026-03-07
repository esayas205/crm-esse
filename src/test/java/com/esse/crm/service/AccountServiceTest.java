package com.esse.crm.service;

import com.esse.crm.dto.AccountDTO;
import com.esse.crm.dto.AccountStatus;
import com.esse.crm.entity.Account;
import com.esse.crm.exception.ResourceNotFoundException;
import com.esse.crm.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountService accountService;

    private Account account;
    private AccountDTO accountDTO;

    @BeforeEach
    void setUp() {
        account = Account.builder()
                .id(1L)
                .accountName("Test Account")
                .industry("Tech")
                .status(AccountStatus.ACTIVE)
                .build();

        accountDTO = AccountDTO.builder()
                .id(1L)
                .accountName("Test Account")
                .industry("Tech")
                .status(AccountStatus.ACTIVE)
                .build();
    }

    @Test
    void getAllAccounts_ShouldReturnPageOfDTOs() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Account> accountPage = new PageImpl<>(Collections.singletonList(account));

        when(accountRepository.searchAccounts(any(), any())).thenReturn(accountPage);

        Page<AccountDTO> result = accountService.getAllAccounts("Test", pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(account.getAccountName(), result.getContent().get(0).getAccountName());
        verify(accountRepository).searchAccounts("Test", pageable);
    }

    @Test
    void getAccountById_ShouldReturnDTO_WhenIdExists() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        AccountDTO result = accountService.getAccountById(1L);

        assertNotNull(result);
        assertEquals(account.getAccountName(), result.getAccountName());
    }

    @Test
    void getAccountById_ShouldThrowException_WhenIdDoesNotExist() {
        when(accountRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> accountService.getAccountById(1L));
    }

    @Test
    void createAccount_ShouldReturnDTO() {
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        AccountDTO result = accountService.createAccount(accountDTO);

        assertNotNull(result);
        assertEquals(account.getAccountName(), result.getAccountName());
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void updateAccount_ShouldReturnUpdatedDTO() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        accountDTO.setAccountName("Updated Name");
        AccountDTO result = accountService.updateAccount(1L, accountDTO);

        assertNotNull(result);
        assertEquals("Updated Name", result.getAccountName());
        verify(accountRepository).save(account);
    }

    @Test
    void deleteAccount_ShouldCallRepository_WhenIdExists() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        doNothing().when(accountRepository).delete(account);

        accountService.deleteAccount(1L);

        verify(accountRepository).delete(account);
    }
}
