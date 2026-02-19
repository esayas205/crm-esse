package com.esse.crm.controller;

import com.esse.crm.dto.AccountDTO;
import com.esse.crm.dto.AccountStatus;
import com.esse.crm.dto.opportunity.OpportunityDTO;
import com.esse.crm.dto.opportunity.OpportunityStage;
import com.esse.crm.repository.ActivityRepository;
import com.esse.crm.repository.AccountRepository;
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

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(roles = "ADMIN", authorities = {"DEAL_WRITE", "DEAL_READ", "ACCOUNT_WRITE"})
public class OpportunityControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OpportunityRepository opportunityRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ContactRepository contactRepository;

    @Autowired
    private LeadRepository leadRepository;

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Long accountId;

    @BeforeEach
    @Transactional
    @WithMockUser(authorities = "ACCOUNT_WRITE")
    void setUp() throws Exception {
        activityRepository.deleteAll();
        opportunityRepository.deleteAll();
        accountRepository.deleteAll();
        contactRepository.deleteAll();
        leadRepository.deleteAll();

        // Create an account for opportunities
        AccountDTO accountDTO = AccountDTO.builder()
                .accountName("Test Account")
                .status(AccountStatus.ACTIVE)
                .build();

        String response = mockMvc.perform(post("/api/accounts")
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("admin").authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("ACCOUNT_WRITE")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(accountDTO)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        
        accountId = objectMapper.readTree(response).get("id").asLong();
    }

    @Test
    @WithMockUser(authorities = "DEAL_WRITE")
    void shouldCreateOpportunity() throws Exception {
        OpportunityDTO opportunityDTO = OpportunityDTO.builder()
                .name("New Project")
                .stage(OpportunityStage.QUALIFICATION)
                .amount(new BigDecimal("10000.00"))
                .closeDate(LocalDate.now().plusMonths(1))
                .accountId(accountId)
                .build();

        mockMvc.perform(post("/api/opportunities")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(opportunityDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name", is("New Project")))
                .andExpect(jsonPath("$.amount", is(10000.0)))
                .andExpect(jsonPath("$.stage", is("QUALIFICATION")));
    }

    @Test
    @WithMockUser(authorities = {"DEAL_READ", "DEAL_WRITE"})
    void shouldGetOpportunity() throws Exception {
        OpportunityDTO opportunityDTO = OpportunityDTO.builder()
                .name("Existing Project")
                .stage(OpportunityStage.PROPOSAL)
                .amount(new BigDecimal("5000.00"))
                .accountId(accountId)
                .build();

        String response = mockMvc.perform(post("/api/opportunities")
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("admin").authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("DEAL_WRITE")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(opportunityDTO)))
                .andReturn().getResponse().getContentAsString();
        
        Long id = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(get("/api/opportunities/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(id.intValue())))
                .andExpect(jsonPath("$.name", is("Existing Project")));
    }

    @Test
    @WithMockUser(authorities = {"DEAL_READ", "DEAL_WRITE"})
    void shouldUpdateOpportunity() throws Exception {
        OpportunityDTO opportunityDTO = OpportunityDTO.builder()
                .name("Project Alpha")
                .stage(OpportunityStage.QUALIFICATION)
                .amount(new BigDecimal("2000.00"))
                .accountId(accountId)
                .build();

        String response = mockMvc.perform(post("/api/opportunities")
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("admin").authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("DEAL_WRITE")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(opportunityDTO)))
                .andReturn().getResponse().getContentAsString();
        
        Long id = objectMapper.readTree(response).get("id").asLong();

        opportunityDTO.setName("Project Beta");
        opportunityDTO.setStage(OpportunityStage.NEGOTIATION);

        mockMvc.perform(put("/api/opportunities/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(opportunityDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Project Beta")))
                .andExpect(jsonPath("$.stage", is("NEGOTIATION")));
    }

    @Test
    @WithMockUser(authorities = {"DEAL_READ", "DEAL_WRITE"})
    void shouldAdvanceStage() throws Exception {
        OpportunityDTO opportunityDTO = OpportunityDTO.builder()
                .name("Stage Test")
                .stage(OpportunityStage.QUALIFICATION)
                .amount(new BigDecimal("1000.00"))
                .accountId(accountId)
                .build();

        String response = mockMvc.perform(post("/api/opportunities")
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("admin").authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("DEAL_WRITE")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(opportunityDTO)))
                .andReturn().getResponse().getContentAsString();
        
        Long id = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(patch("/api/opportunities/{id}/stage", id)
                .param("stage", "WON"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stage", is("WON")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldDeleteOpportunity() throws Exception {
        OpportunityDTO opportunityDTO = OpportunityDTO.builder()
                .name("To Delete")
                .stage(OpportunityStage.QUALIFICATION)
                .amount(new BigDecimal("100.00"))
                .accountId(accountId)
                .build();

        String response = mockMvc.perform(post("/api/opportunities")
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("admin").authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("DEAL_WRITE")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(opportunityDTO)))
                .andReturn().getResponse().getContentAsString();
        
        Long id = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(delete("/api/opportunities/{id}", id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/opportunities/{id}", id)
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("admin").authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("DEAL_READ"))))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = {"DEAL_READ", "DEAL_WRITE"})
    void shouldSearchOpportunities() throws Exception {
        OpportunityDTO o1 = OpportunityDTO.builder().name("Big Deal").stage(OpportunityStage.QUALIFICATION).amount(new BigDecimal("100000.00")).accountId(accountId).build();
        OpportunityDTO o2 = OpportunityDTO.builder().name("Small Deal").stage(OpportunityStage.LOST).amount(new BigDecimal("1000.00")).accountId(accountId).build();

        mockMvc.perform(post("/api/opportunities").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(o1)));
        mockMvc.perform(post("/api/opportunities").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(o2)));

        mockMvc.perform(get("/api/opportunities")
                .param("stage", "QUALIFICATION"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name", is("Big Deal")));

        mockMvc.perform(get("/api/opportunities")
                .param("minAmount", "50000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name", is("Big Deal")));
    }
}
