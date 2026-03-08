package com.esse.crm.service;

import com.esse.crm.dto.AccountDTO;
import com.esse.crm.dto.AccountStatus;
import com.esse.crm.mapper.AccountMapper;
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

    @Mock
    private AccountMapper accountMapper;

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
        when(accountMapper.toDTO(any(Account.class))).thenReturn(accountDTO);

        Page<AccountDTO> result = accountService.getAllAccounts("Test", pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(accountDTO.getAccountName(), result.getContent().get(0).getAccountName());
        verify(accountRepository).searchAccounts("Test", pageable);
    }

    @Test
    void getAccountById_ShouldReturnDTO_WhenIdExists() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(accountMapper.toDTO(any(Account.class))).thenReturn(accountDTO);

        AccountDTO result = accountService.getAccountById(1L);

        assertNotNull(result);
        assertEquals(accountDTO.getAccountName(), result.getAccountName());
    }

    @Test
    void getAccountById_ShouldThrowException_WhenIdDoesNotExist() {
        when(accountRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> accountService.getAccountById(1L));
    }

    @Test
    void createAccount_ShouldReturnDTO() {
        when(accountMapper.toEntity(any(AccountDTO.class))).thenReturn(account);
        when(accountRepository.save(any(Account.class))).thenReturn(account);
        when(accountMapper.toDTO(any(Account.class))).thenReturn(accountDTO);

        AccountDTO result = accountService.createAccount(accountDTO);

        assertNotNull(result);
        assertEquals(accountDTO.getAccountName(), result.getAccountName());
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void updateAccount_ShouldReturnUpdatedDTO() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        doNothing().when(accountMapper).updateAccountFromDto(any(AccountDTO.class), any(Account.class));
        when(accountRepository.save(any(Account.class))).thenReturn(account);
        when(accountMapper.toDTO(any(Account.class))).thenReturn(accountDTO);

        accountDTO.setAccountName("Updated Name");
        AccountDTO result = accountService.updateAccount(1L, accountDTO);

        assertNotNull(result);
        assertEquals(accountDTO.getAccountName(), result.getAccountName());
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
