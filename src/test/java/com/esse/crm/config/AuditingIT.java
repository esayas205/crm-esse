package com.esse.crm.config;

import com.esse.crm.dto.AccountDTO;
import com.esse.crm.dto.AccountStatus;
import com.esse.crm.repository.AccountRepository;
import com.esse.crm.security.dto.UserPrincipal;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class AuditingIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldPopulateAuditFieldsWhenAuthenticated() throws Exception {
        UserPrincipal principal = UserPrincipal.builder()
                .id(99L)
                .username("test.auditor")
                .authorities(List.of(new SimpleGrantedAuthority("ACCOUNT_WRITE")))
                .build();

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

        AccountDTO accountDTO = AccountDTO.builder()
                .accountName("Audited Account")
                .status(AccountStatus.ACTIVE)
                .build();

        mockMvc.perform(post("/api/accounts")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication(auth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(accountDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.createdBy", is("99-test.auditor")))
                .andExpect(jsonPath("$.updatedBy", is("99-test.auditor")));
    }

    @Test
    void shouldPopulateSystemWhenInternalUpdate() {
        // Clear security context to simulate internal/unauthenticated call
        SecurityContextHolder.clearContext();

        com.esse.crm.entity.Account account = com.esse.crm.entity.Account.builder()
                .accountName("System Account")
                .status(AccountStatus.ACTIVE)
                .build();

        com.esse.crm.entity.Account saved = accountRepository.saveAndFlush(account);

        assert saved.getCreatedBy().equals("system");
        assert saved.getUpdatedBy().equals("system");
    }
}
