package com.esse.crm.controller;

import com.esse.crm.dto.AccountDTO;
import com.esse.crm.dto.AccountStatus;
import com.esse.crm.dto.ContactDTO;
import com.esse.crm.repository.AccountRepository;
import com.esse.crm.repository.ActivityRepository;
import com.esse.crm.repository.ContactRepository;
import com.esse.crm.repository.LeadRepository;
import com.esse.crm.repository.OpportunityRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(roles = "ADMIN", authorities = {"ACCOUNT_READ", "ACCOUNT_WRITE"})
public class AccountControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ContactRepository contactRepository;

    @Autowired
    private LeadRepository leadRepository;

    @Autowired
    private OpportunityRepository opportunityRepository;

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    @Transactional
    void setUp() {
        activityRepository.deleteAll();
        opportunityRepository.deleteAll();
        accountRepository.deleteAll();
        contactRepository.deleteAll();
        leadRepository.deleteAll();
    }

    @Test
    @WithMockUser(authorities = "ACCOUNT_WRITE")
    void shouldCreateAccount() throws Exception {
        AccountDTO accountDTO = AccountDTO.builder()
                .accountName("Acme Corp")
                .industry("Technology")
                .status(AccountStatus.ACTIVE)
                .build();

        mockMvc.perform(post("/api/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(accountDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.accountName", is("Acme Corp")));
    }

    @Test
    @WithMockUser(authorities = {"ACCOUNT_READ", "ACCOUNT_WRITE"})
    void shouldGetAccount() throws Exception {
        AccountDTO accountDTO = AccountDTO.builder()
                .accountName("Globex")
                .status(AccountStatus.ACTIVE)
                .build();

        String response = mockMvc.perform(post("/api/accounts")
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("admin").authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("ACCOUNT_WRITE")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(accountDTO)))
                .andReturn().getResponse().getContentAsString();
        
        Long id = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(get("/api/accounts/{id}", id)
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("admin").authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("ACCOUNT_READ"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(id.intValue())))
                .andExpect(jsonPath("$.accountName", is("Globex")));
    }

    @Test
    @WithMockUser(authorities = {"ACCOUNT_READ", "ACCOUNT_WRITE"})
    void shouldUpdateAccount() throws Exception {
        AccountDTO accountDTO = AccountDTO.builder()
                .accountName("Initech")
                .status(AccountStatus.ACTIVE)
                .build();

        String response = mockMvc.perform(post("/api/accounts")
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("admin").authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("ACCOUNT_WRITE")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(accountDTO)))
                .andReturn().getResponse().getContentAsString();
        
        Long id = objectMapper.readTree(response).get("id").asLong();

        accountDTO.setAccountName("Initech Updated");
        accountDTO.setStatus(AccountStatus.INACTIVE);

        mockMvc.perform(put("/api/accounts/{id}", id)
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("admin").authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("ACCOUNT_WRITE")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(accountDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountName", is("Initech Updated")))
                .andExpect(jsonPath("$.status", is("INACTIVE")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldDeleteAccount() throws Exception {
        AccountDTO accountDTO = AccountDTO.builder()
                .accountName("Soylent Corp")
                .status(AccountStatus.ACTIVE)
                .build();

        String response = mockMvc.perform(post("/api/accounts")
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("admin").authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("ACCOUNT_WRITE")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(accountDTO)))
                .andReturn().getResponse().getContentAsString();
        
        Long id = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(delete("/api/accounts/{id}", id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/accounts/{id}", id)
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("admin").authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("ACCOUNT_READ"))))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = {"ACCOUNT_READ", "ACCOUNT_WRITE"})
    void shouldSearchAccounts() throws Exception {
        AccountDTO a1 = AccountDTO.builder().accountName("Apple").status(AccountStatus.ACTIVE).industry("Hardware").build();
        AccountDTO a2 = AccountDTO.builder().accountName("Microsoft").status(AccountStatus.ACTIVE).industry("Software").build();

        mockMvc.perform(post("/api/accounts")
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("admin").authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("ACCOUNT_WRITE")))
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(a1)));
        mockMvc.perform(post("/api/accounts")
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("admin").authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("ACCOUNT_WRITE")))
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(a2)));

        mockMvc.perform(get("/api/accounts")
                .param("search", "Apple"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].accountName", is("Apple")));

        mockMvc.perform(get("/api/accounts")
                .param("search", "Software"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].accountName", is("Microsoft")));
    }
}
