package com.esse.crm.controller;

import com.esse.crm.dto.AccountDTO;
import com.esse.crm.dto.AccountStatus;
import com.esse.crm.dto.activity.ActivityDTO;
import com.esse.crm.dto.activity.ActivityType;
import com.esse.crm.repository.ActivityRepository;
import com.esse.crm.repository.AccountRepository;
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

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(authorities = {"ACCOUNT_WRITE", "ACTIVITY_WRITE", "ACTIVITY_READ", "ROLE_ADMIN"})
public class ActivityControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private LeadRepository leadRepository;

    @Autowired
    private OpportunityRepository opportunityRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Long accountId;

    @BeforeEach
    @WithMockUser(authorities = "ACCOUNT_WRITE")
    void setUp() throws Exception {
        activityRepository.deleteAll();
        opportunityRepository.deleteAll();
        leadRepository.deleteAll();
        accountRepository.deleteAll();

        // Create an account for activities
        AccountDTO accountDTO = AccountDTO.builder()
                .accountName("Activity Account")
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
    @WithMockUser(authorities = "ACTIVITY_WRITE")
    void shouldCreateActivity() throws Exception {
        ActivityDTO activityDTO = ActivityDTO.builder()
                .subject("Follow up call")
                .type(ActivityType.CALL)
                .description("Call to discuss requirements")
                .dueAt(LocalDateTime.now().plusDays(1))
                .accountId(accountId)
                .build();

        mockMvc.perform(post("/api/activities")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(activityDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.subject", is("Follow up call")))
                .andExpect(jsonPath("$.type", is("CALL")));
    }

    @Test
    @WithMockUser(authorities = {"ACTIVITY_READ", "ACTIVITY_WRITE"})
    void shouldGetActivity() throws Exception {
        ActivityDTO activityDTO = ActivityDTO.builder()
                .subject("Meeting")
                .type(ActivityType.MEETING)
                .accountId(accountId)
                .build();

        String response = mockMvc.perform(post("/api/activities")
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("admin").authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("ACTIVITY_WRITE")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(activityDTO)))
                .andReturn().getResponse().getContentAsString();
        
        Long id = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(get("/api/activities/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(id.intValue())))
                .andExpect(jsonPath("$.subject", is("Meeting")));
    }

    @Test
    @WithMockUser(authorities = {"ACTIVITY_READ", "ACTIVITY_WRITE"})
    void shouldUpdateActivity() throws Exception {
        ActivityDTO activityDTO = ActivityDTO.builder()
                .subject("Email")
                .type(ActivityType.EMAIL)
                .accountId(accountId)
                .build();

        String response = mockMvc.perform(post("/api/activities")
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("admin").authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("ACTIVITY_WRITE")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(activityDTO)))
                .andReturn().getResponse().getContentAsString();
        
        Long id = objectMapper.readTree(response).get("id").asLong();

        activityDTO.setSubject("Revised Email");
        activityDTO.setDescription("Sent revised quote");

        mockMvc.perform(put("/api/activities/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(activityDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subject", is("Revised Email")))
                .andExpect(jsonPath("$.description", is("Sent revised quote")));
    }

    @Test
    @WithMockUser(authorities = {"ACTIVITY_READ", "ACTIVITY_WRITE"})
    void shouldCompleteActivity() throws Exception {
        ActivityDTO activityDTO = ActivityDTO.builder()
                .subject("Task")
                .type(ActivityType.TASK)
                .accountId(accountId)
                .completed(false)
                .build();

        String response = mockMvc.perform(post("/api/activities")
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("admin").authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("ACTIVITY_WRITE")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(activityDTO)))
                .andReturn().getResponse().getContentAsString();
        
        Long id = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(patch("/api/activities/{id}/complete", id)
                .param("outcome", "Done"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completed", is(true)))
                .andExpect(jsonPath("$.outcome", is("Done")));
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void shouldDeleteActivity() throws Exception {
        ActivityDTO activityDTO = ActivityDTO.builder()
                .subject("Delete me")
                .type(ActivityType.NOTE)
                .accountId(accountId)
                .build();

        String response = mockMvc.perform(post("/api/activities")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(activityDTO)))
                .andReturn().getResponse().getContentAsString();
        
        Long id = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(delete("/api/activities/{id}", id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/activities/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = {"ACTIVITY_READ", "ACTIVITY_WRITE"})
    void shouldSearchActivities() throws Exception {
        ActivityDTO a1 = ActivityDTO.builder().subject("A1").type(ActivityType.CALL).accountId(accountId).completed(true).build();
        ActivityDTO a2 = ActivityDTO.builder().subject("A2").type(ActivityType.EMAIL).accountId(accountId).completed(false).build();

        mockMvc.perform(post("/api/activities").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(a1)));
        mockMvc.perform(post("/api/activities").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(a2)));

        // Test filter by type
        mockMvc.perform(get("/api/activities")
                .param("type", "EMAIL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].subject", is("A2")))
                .andExpect(jsonPath("$.content[0].type", is("EMAIL")));
    }
}
