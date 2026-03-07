package com.esse.crm.service;

import com.esse.crm.dto.lead.LeadConversionResponseDTO;
import com.esse.crm.dto.lead.LeadDTO;
import com.esse.crm.dto.lead.LeadStatus;
import com.esse.crm.dto.opportunity.OpportunityDTO;
import com.esse.crm.entity.Account;
import com.esse.crm.entity.Contact;
import com.esse.crm.entity.Lead;
import com.esse.crm.exception.ConflictException;
import com.esse.crm.exception.ResourceNotFoundException;
import com.esse.crm.repository.AccountRepository;
import com.esse.crm.repository.ContactRepository;
import com.esse.crm.repository.LeadRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LeadServiceTest {

    @Mock
    private LeadRepository leadRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private ContactRepository contactRepository;

    @Mock
    private OpportunityService opportunityService;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private LeadService leadService;

    private Lead lead;
    private LeadDTO leadDTO;

    @BeforeEach
    void setUp() {
        lead = Lead.builder()
                .id(1L)
                .email("test@example.com")
                .company("Test Co")
                .contactName("John Doe")
                .status(LeadStatus.NEW)
                .build();

        leadDTO = new LeadDTO();
        leadDTO.setEmail("test@example.com");
        leadDTO.setCompany("Test Co");
        leadDTO.setContactName("John Doe");
    }

    @Test
    void createLead_ShouldReturnDTO_WhenEmailUnique() {
        when(leadRepository.findByEmail(any())).thenReturn(Optional.empty());
        when(modelMapper.map(any(LeadDTO.class), eq(Lead.class))).thenReturn(lead);
        when(leadRepository.save(any(Lead.class))).thenReturn(lead);
        when(modelMapper.map(any(Lead.class), eq(LeadDTO.class))).thenReturn(leadDTO);

        LeadDTO result = leadService.createLead(leadDTO);

        assertNotNull(result);
        verify(leadRepository).save(any(Lead.class));
    }

    @Test
    void createLead_ShouldThrowConflict_WhenEmailExists() {
        when(leadRepository.findByEmail(any())).thenReturn(Optional.of(lead));

        assertThrows(ConflictException.class, () -> leadService.createLead(leadDTO));
    }

    @Test
    void convertLead_ShouldReturnConversionResponse() {
        lead.setStatus(LeadStatus.QUALIFIED);
        when(leadRepository.findById(1L)).thenReturn(Optional.of(lead));
        when(accountRepository.findByAccountName(any())).thenReturn(Optional.empty());
        when(accountRepository.save(any(Account.class))).thenReturn(Account.builder().id(10L).build());
        when(contactRepository.findByEmail(any())).thenReturn(Optional.empty());
        when(contactRepository.save(any(Contact.class))).thenReturn(Contact.builder().id(20L).build());
        when(opportunityService.createOpportunity(any(OpportunityDTO.class))).thenReturn(OpportunityDTO.builder().id(30L).build());

        LeadConversionResponseDTO result = leadService.convertLead(1L);

        assertNotNull(result);
        assertEquals(10L, result.getAccountId());
        assertEquals(20L, result.getContactId());
        assertEquals(30L, result.getOpportunityId());
        assertEquals(LeadStatus.CONVERTED, lead.getStatus());
    }

    @Test
    void convertLead_ShouldThrowException_WhenNotQualified() {
        lead.setStatus(LeadStatus.NEW);
        when(leadRepository.findById(1L)).thenReturn(Optional.of(lead));

        assertThrows(IllegalStateException.class, () -> leadService.convertLead(1L));
    }

    @Test
    void getLead_ShouldThrowNotFound_WhenIdInvalid() {
        when(leadRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> leadService.getLead(1L));
    }
}
