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
    @Test
    @WithMockUser(authorities = "CONTACT_READ")
    void shouldGetAllContacts() throws Exception {
        // 1. Create a contact
        ContactDTO contactDTO = ContactDTO.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .accountIds(Arrays.asList(accountId1))
                .build();

        mockMvc.perform(post("/api/contacts")
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("admin").authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("CONTACT_WRITE")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(contactDTO)))
                .andExpect(status().isCreated());

        // 2. Get all contacts
        mockMvc.perform(get("/api/contacts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.content[0].email", is("john.doe@example.com")));
    }

    @Test
    @WithMockUser(authorities = "CONTACT_READ")
    void shouldSearchContacts() throws Exception {
        // 1. Create two contacts
        ContactDTO c1 = ContactDTO.builder().firstName("Alice").lastName("Smith").email("alice@example.com").build();
        ContactDTO c2 = ContactDTO.builder().firstName("Bob").lastName("Jones").email("bob@example.com").build();

        mockMvc.perform(post("/api/contacts")
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("admin").authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("CONTACT_WRITE")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(c1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/contacts")
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("admin").authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("CONTACT_WRITE")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(c2)))
                .andExpect(status().isCreated());

        // 2. Search for "Alice"
        mockMvc.perform(get("/api/contacts").param("search", "Alice"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].firstName", is("Alice")));

        // 3. Search for "Jones"
        mockMvc.perform(get("/api/contacts").param("search", "Jones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].lastName", is("Jones")));
    }

    @Test
    @WithMockUser(authorities = "CONTACT_READ")
    void shouldGetContactById() throws Exception {
        // 1. Create a contact
        ContactDTO contactDTO = ContactDTO.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe.id@example.com")
                .build();

        String response = mockMvc.perform(post("/api/contacts")
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("admin").authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("CONTACT_WRITE")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(contactDTO)))
                .andReturn().getResponse().getContentAsString();
        Long id = objectMapper.readTree(response).get("id").asLong();

        // 2. Get by id
        mockMvc.perform(get("/api/contacts/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(id.intValue())))
                .andExpect(jsonPath("$.firstName", is("John")));
    }

    @Test
    @WithMockUser(authorities = "CONTACT_READ")
    void shouldReturn404WhenContactNotFound() throws Exception {
        mockMvc.perform(get("/api/contacts/{id}", 9999))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void shouldDeleteContact() throws Exception {
        // 1. Create a contact
        ContactDTO contactDTO = ContactDTO.builder()
                .firstName("Delete")
                .lastName("Me")
                .email("delete.me@example.com")
                .build();

        String response = mockMvc.perform(post("/api/contacts")
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("admin").authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("CONTACT_WRITE")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(contactDTO)))
                .andReturn().getResponse().getContentAsString();
        Long id = objectMapper.readTree(response).get("id").asLong();

        // 2. Delete it
        mockMvc.perform(delete("/api/contacts/{id}", id))
                .andExpect(status().isNoContent());

        // 3. Verify it's gone
        mockMvc.perform(get("/api/contacts/{id}", id)
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("admin").authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("CONTACT_READ"))))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = "CONTACT_WRITE")
    void shouldReturn403WhenDeletingWithoutAdminRole() throws Exception {
        mockMvc.perform(delete("/api/contacts/{id}", 1L))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "CONTACT_WRITE")
    void shouldReturn400WhenCreatingInvalidContact() throws Exception {
        ContactDTO contactDTO = ContactDTO.builder()
                .firstName("") // Invalid
                .lastName("Doe")
                .email("invalid-email") // Invalid
                .build();

        mockMvc.perform(post("/api/contacts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(contactDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn401WhenUnauthorized() throws Exception {
        // No @WithMockUser for this test case (or override with empty)
        mockMvc.perform(get("/api/contacts")
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = "CONTACT_READ")
    void shouldGetContactActivities() throws Exception {
        // 1. Create a contact
        ContactDTO contactDTO = ContactDTO.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.activities@example.com")
                .build();

        String contactResponse = mockMvc.perform(post("/api/contacts")
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("admin").authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("CONTACT_WRITE")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(contactDTO)))
                .andReturn().getResponse().getContentAsString();
        Long contactId = objectMapper.readTree(contactResponse).get("id").asLong();

        // 2. Create an activity for this contact
        // Assuming there is an activity endpoint. If not, we might need to use repository to insert directly.
        // Looking at ActivityController, it should have a POST /api/activities
        
        com.esse.crm.dto.activity.ActivityDTO activityDTO = com.esse.crm.dto.activity.ActivityDTO.builder()
                .subject("Call John")
                .type(com.esse.crm.dto.activity.ActivityType.CALL)
                .contactId(contactId)
                .build();

        mockMvc.perform(post("/api/activities")
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("admin").authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("ACTIVITY_WRITE")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(activityDTO)))
                .andExpect(status().isCreated());

        // 3. Get activities for the contact
        mockMvc.perform(get("/api/contacts/{id}/activities", contactId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].subject", is("Call John")));
    }
}
