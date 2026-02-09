package com.esse.crm.controller;

import com.esse.crm.dto.AccountDTO;
import com.esse.crm.dto.AccountStatus;
import com.esse.crm.dto.ContactDTO;
import com.esse.crm.repository.AccountRepository;
import com.esse.crm.repository.ActivityRepository;
import com.esse.crm.repository.ContactRepository;
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

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(authorities = {"ACCOUNT_WRITE", "CONTACT_WRITE", "CONTACT_READ"})
public class ContactControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ContactRepository contactRepository;

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private OpportunityRepository opportunityRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Long accountId1;
    private Long accountId2;

    @BeforeEach
    @WithMockUser(authorities = "ACCOUNT_WRITE")
    void setUp() throws Exception {
        activityRepository.deleteAll();
        opportunityRepository.deleteAll();
        
        // Manual cleanup of join table if necessary, though deleteAll should work if ordered correctly
        // and relationships are mapped.
        // In ManyToMany, deleting the "owner" side (Account) should delete join table entries.
        // But we need to delete contacts too.
        
        accountRepository.deleteAll();
        contactRepository.deleteAll();

        // Create two accounts
        AccountDTO a1 = AccountDTO.builder().accountName("Account 1").status(AccountStatus.ACTIVE).build();
        AccountDTO a2 = AccountDTO.builder().accountName("Account 2").status(AccountStatus.ACTIVE).build();

        String r1 = mockMvc.perform(post("/api/accounts")
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("admin").authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("ACCOUNT_WRITE")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(a1)))
                .andReturn().getResponse().getContentAsString();
        accountId1 = objectMapper.readTree(r1).get("id").asLong();

        String r2 = mockMvc.perform(post("/api/accounts")
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("admin").authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("ACCOUNT_WRITE")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(a2)))
                .andReturn().getResponse().getContentAsString();
        accountId2 = objectMapper.readTree(r2).get("id").asLong();
    }

    @Test
    @WithMockUser(authorities = "CONTACT_WRITE")
    void shouldCreateContactWithMultipleAccounts() throws Exception {
        ContactDTO contactDTO = ContactDTO.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .accountIds(Arrays.asList(accountId1, accountId2))
                .isPrimaryContact(true)
                .build();

        mockMvc.perform(post("/api/contacts")
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("admin").authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("CONTACT_WRITE")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(contactDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.accountIds", hasSize(2)))
                .andExpect(jsonPath("$.accountIds", containsInAnyOrder(accountId1.intValue(), accountId2.intValue())));
    }

    @Test
    @WithMockUser(authorities = {"CONTACT_WRITE", "CONTACT_READ"})
    void shouldUpdateContactAccounts() throws Exception {
        // 1. Create with one account
        ContactDTO contactDTO = ContactDTO.builder()
                .firstName("Jane")
                .lastName("Doe")
                .email("jane.doe@example.com")
                .accountIds(Arrays.asList(accountId1))
                .build();

        String response = mockMvc.perform(post("/api/contacts")
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("admin").authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("CONTACT_WRITE")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(contactDTO)))
                .andReturn().getResponse().getContentAsString();
        Long id = objectMapper.readTree(response).get("id").asLong();

        // 2. Update to two accounts
        contactDTO.setAccountIds(Arrays.asList(accountId1, accountId2));

        mockMvc.perform(put("/api/contacts/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(contactDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountIds", hasSize(2)))
                .andExpect(jsonPath("$.accountIds", containsInAnyOrder(accountId1.intValue(), accountId2.intValue())));
        
        // 3. Update to other account
        contactDTO.setAccountIds(Arrays.asList(accountId2));
        mockMvc.perform(put("/api/contacts/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(contactDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountIds", hasSize(1)))
                .andExpect(jsonPath("$.accountIds[0]", is(accountId2.intValue())));
    }
}
