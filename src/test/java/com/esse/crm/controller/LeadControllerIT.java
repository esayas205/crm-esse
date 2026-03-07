package com.esse.crm.controller;

import com.esse.crm.dto.lead.LeadDTO;
import com.esse.crm.dto.lead.LeadSource;
import com.esse.crm.dto.lead.LeadStatus;
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
@WithMockUser(roles = "ADMIN", authorities = {"LEAD_WRITE", "LEAD_READ"})
public class LeadControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LeadRepository leadRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ContactRepository contactRepository;

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
    @WithMockUser(authorities = "LEAD_WRITE")
    void shouldCreateLead() throws Exception {
        LeadDTO leadDTO = LeadDTO.builder()
                .source(LeadSource.WEB)
                .company("Test Company")
                .contactName("John Doe")
                .email("john@example.com")
                .status(LeadStatus.NEW)
                .build();

        mockMvc.perform(post("/api/leads")
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("admin").authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("LEAD_WRITE")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(leadDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.company", is("Test Company")))
                .andExpect(jsonPath("$.contactName", is("John Doe")))
                .andExpect(jsonPath("$.email", is("john@example.com")));
    }

    @Test
    @WithMockUser(authorities = {"LEAD_READ", "LEAD_WRITE"})
    void shouldGetLead() throws Exception {
        LeadDTO leadDTO = LeadDTO.builder()
                .source(LeadSource.WEB)
                .company("Test Company")
                .contactName("John Doe")
                .email("john@example.com")
                .status(LeadStatus.NEW)
                .build();

        String response = mockMvc.perform(post("/api/leads")
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("admin").authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("LEAD_WRITE")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(leadDTO)))
                .andReturn().getResponse().getContentAsString();
        
        Long id = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(get("/api/leads/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(id.intValue())))
                .andExpect(jsonPath("$.email", is("john@example.com")));
    }

    @Test
    @WithMockUser(authorities = {"LEAD_READ", "LEAD_WRITE"})
    void shouldUpdateLead() throws Exception {
        LeadDTO leadDTO = LeadDTO.builder()
                .source(LeadSource.WEB)
                .company("Test Company")
                .contactName("John Doe")
                .email("john@example.com")
                .status(LeadStatus.NEW)
                .build();

        String response = mockMvc.perform(post("/api/leads")
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("admin").authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("LEAD_WRITE")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(leadDTO)))
                .andReturn().getResponse().getContentAsString();
        
        Long id = objectMapper.readTree(response).get("id").asLong();

        leadDTO.setContactName("Jane Doe");
        leadDTO.setStatus(LeadStatus.QUALIFIED);

        mockMvc.perform(put("/api/leads/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(leadDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contactName", is("Jane Doe")))
                .andExpect(jsonPath("$.status", is("QUALIFIED")));
    }

    @Test
    @WithMockUser(authorities = "LEAD_WRITE")
    void shouldConvertLead() throws Exception {
        // 1. Create a lead
        LeadDTO leadDTO = LeadDTO.builder()
                .source(LeadSource.WEB)
                .company("Acme Corp")
                .contactName("Alice Smith")
                .email("alice@acme.com")
                .status(LeadStatus.QUALIFIED)
                .build();

        String response = mockMvc.perform(post("/api/leads")
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("admin").authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("LEAD_WRITE")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(leadDTO)))
                .andReturn().getResponse().getContentAsString();
        
        Long id = objectMapper.readTree(response).get("id").asLong();

        // 2. Convert the lead
        mockMvc.perform(post("/api/leads/{id}/convert", id)
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("admin").authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("LEAD_WRITE"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.leadId", is(id.intValue())))
                .andExpect(jsonPath("$.accountId", notNullValue()))
                .andExpect(jsonPath("$.contactId", notNullValue()))
                .andExpect(jsonPath("$.opportunityId", notNullValue()));

        // 3. Verify lead status is CONVERTED
        mockMvc.perform(get("/api/leads/{id}", id)
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("admin").authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("LEAD_READ"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("CONVERTED")));
    }

    @Test
    @WithMockUser(authorities = "LEAD_WRITE")
    void shouldFailConvertingIfNotQualified() throws Exception {
        LeadDTO leadDTO = LeadDTO.builder()
                .source(LeadSource.WEB)
                .company("Acme Corp")
                .contactName("Alice Smith")
                .email("alice2@acme.com")
                .status(LeadStatus.NEW)
                .build();

        String response = mockMvc.perform(post("/api/leads")
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("admin").authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("LEAD_WRITE")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(leadDTO)))
                .andReturn().getResponse().getContentAsString();
        
        Long id = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(post("/api/leads/{id}/convert", id)
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("admin").authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("LEAD_WRITE"))))
                .andExpect(status().isBadRequest());
    }
}
