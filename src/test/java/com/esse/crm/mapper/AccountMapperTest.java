package com.esse.crm.mapper;

import com.esse.crm.dto.AccountDTO;
import com.esse.crm.dto.AccountStatus;
import com.esse.crm.entity.Account;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.*;

class AccountMapperTest {

    private final AccountMapper accountMapper = Mappers.getMapper(AccountMapper.class);

    @Test
    void shouldMapAccountToDTO() {
        Account account = Account.builder()
                .id(1L)
                .accountName("Test Corp")
                .industry("Tech")
                .status(AccountStatus.ACTIVE)
                .build();

        AccountDTO dto = accountMapper.toDTO(account);

        assertNotNull(dto);
        assertEquals(account.getId(), dto.getId());
        assertEquals(account.getAccountName(), dto.getAccountName());
        assertEquals(account.getIndustry(), dto.getIndustry());
        assertEquals(account.getStatus(), dto.getStatus());
    }

    @Test
    void shouldMapDTOToAccount() {
        AccountDTO dto = AccountDTO.builder()
                .accountName("Test Corp")
                .industry("Tech")
                .status(AccountStatus.ACTIVE)
                .build();

        Account account = accountMapper.toEntity(dto);

        assertNotNull(account);
        assertNull(account.getId());
        assertEquals(dto.getAccountName(), account.getAccountName());
        assertEquals(dto.getIndustry(), account.getIndustry());
        assertEquals(dto.getStatus(), account.getStatus());
    }

    @Test
    void shouldUpdateAccountFromDto() {
        Account account = Account.builder()
                .id(1L)
                .accountName("Old Name")
                .status(AccountStatus.ACTIVE)
                .build();

        AccountDTO dto = AccountDTO.builder()
                .accountName("New Name")
                .status(AccountStatus.INACTIVE)
                .build();

        accountMapper.updateAccountFromDto(dto, account);

        assertEquals(1L, account.getId());
        assertEquals("New Name", account.getAccountName());
        assertEquals(AccountStatus.INACTIVE, account.getStatus());
    }
}
