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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
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
    void setUp() {
        activityRepository.deleteAll();
        opportunityRepository.deleteAll();
        contactRepository.deleteAll();
        leadRepository.deleteAll();
        accountRepository.deleteAll();
    }

    @Test
    void shouldCreateLead() throws Exception {
        LeadDTO leadDTO = LeadDTO.builder()
                .source(LeadSource.WEB)
                .company("Test Company")
                .contactName("John Doe")
                .email("john@example.com")
                .status(LeadStatus.NEW)
                .build();

        mockMvc.perform(post("/api/leads")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(leadDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.company", is("Test Company")))
                .andExpect(jsonPath("$.contactName", is("John Doe")))
                .andExpect(jsonPath("$.email", is("john@example.com")));
    }

    @Test
    void shouldGetLead() throws Exception {
        LeadDTO leadDTO = LeadDTO.builder()
                .source(LeadSource.WEB)
                .company("Test Company")
                .contactName("John Doe")
                .email("john@example.com")
                .status(LeadStatus.NEW)
                .build();

        String response = mockMvc.perform(post("/api/leads")
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
    void shouldUpdateLead() throws Exception {
        LeadDTO leadDTO = LeadDTO.builder()
                .source(LeadSource.WEB)
                .company("Test Company")
                .contactName("John Doe")
                .email("john@example.com")
                .status(LeadStatus.NEW)
                .build();

        String response = mockMvc.perform(post("/api/leads")
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
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(leadDTO)))
                .andReturn().getResponse().getContentAsString();
        
        Long id = objectMapper.readTree(response).get("id").asLong();

        // 2. Convert the lead
        mockMvc.perform(post("/api/leads/{id}/convert", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.leadId", is(id.intValue())))
                .andExpect(jsonPath("$.accountId", notNullValue()))
                .andExpect(jsonPath("$.contactId", notNullValue()))
                .andExpect(jsonPath("$.opportunityId", notNullValue()));

        // 3. Verify lead status is CONVERTED
        mockMvc.perform(get("/api/leads/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("CONVERTED")));
    }

    @Test
    void shouldFailConvertingIfNotQualified() throws Exception {
        LeadDTO leadDTO = LeadDTO.builder()
                .source(LeadSource.WEB)
                .company("Acme Corp")
                .contactName("Alice Smith")
                .email("alice2@acme.com")
                .status(LeadStatus.NEW)
                .build();

        String response = mockMvc.perform(post("/api/leads")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(leadDTO)))
                .andReturn().getResponse().getContentAsString();
        
        Long id = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(post("/api/leads/{id}/convert", id))
                .andExpect(status().isBadRequest());
    }
}
