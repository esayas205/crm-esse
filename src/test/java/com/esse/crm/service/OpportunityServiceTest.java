package com.esse.crm.service;

import com.esse.crm.dto.opportunity.OpportunityDTO;
import com.esse.crm.dto.opportunity.OpportunityStage;
import com.esse.crm.entity.Account;
import com.esse.crm.entity.Opportunity;
import com.esse.crm.exception.ResourceNotFoundException;
import com.esse.crm.repository.AccountRepository;
import com.esse.crm.repository.LeadRepository;
import com.esse.crm.repository.OpportunityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OpportunityServiceTest {

    @Mock
    private OpportunityRepository opportunityRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private LeadRepository leadRepository;

    @InjectMocks
    private OpportunityService opportunityService;

    private Opportunity opportunity;
    private OpportunityDTO opportunityDTO;
    private Account account;

    @BeforeEach
    void setUp() {
        account = Account.builder()
                .id(1L)
                .accountName("Test Account")
                .build();

        opportunity = Opportunity.builder()
                .id(1L)
                .name("Test Opportunity")
                .stage(OpportunityStage.PROSPECTING)
                .amount(BigDecimal.valueOf(1000))
                .account(account)
                .build();

        opportunityDTO = OpportunityDTO.builder()
                .id(1L)
                .name("Test Opportunity")
                .stage(OpportunityStage.PROSPECTING)
                .amount(BigDecimal.valueOf(1000))
                .accountId(1L)
                .build();
    }

    @Test
    void createOpportunity_ShouldReturnDTO() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(opportunityRepository.save(any(Opportunity.class))).thenReturn(opportunity);

        OpportunityDTO result = opportunityService.createOpportunity(opportunityDTO);

        assertNotNull(result);
        assertEquals("Test Opportunity", result.getName());
        verify(opportunityRepository).save(any(Opportunity.class));
    }

    @Test
    void advanceStage_ShouldUpdateStage_WhenNotFinal() {
        when(opportunityRepository.findById(1L)).thenReturn(Optional.of(opportunity));
        when(opportunityRepository.save(any(Opportunity.class))).thenReturn(opportunity);

        OpportunityDTO result = opportunityService.advanceStage(1L, OpportunityStage.QUALIFICATION);

        assertNotNull(result);
        assertEquals(OpportunityStage.QUALIFICATION, opportunity.getStage());
    }

    @Test
    void advanceStage_ShouldThrowException_WhenInFinalStage() {
        opportunity.setStage(OpportunityStage.WON);
        when(opportunityRepository.findById(1L)).thenReturn(Optional.of(opportunity));

        assertThrows(IllegalStateException.class, () -> opportunityService.advanceStage(1L, OpportunityStage.LOST));
    }

    @Test
    void getOpportunity_ShouldReturnDTO() {
        when(opportunityRepository.findById(1L)).thenReturn(Optional.of(opportunity));

        OpportunityDTO result = opportunityService.getOpportunity(1L);

        assertNotNull(result);
        assertEquals("Test Opportunity", result.getName());
    }

    @Test
    void searchOpportunities_ShouldReturnPage() {
        Pageable pageable = PageRequest.of(0, 10);
        when(opportunityRepository.search(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(Collections.singletonList(opportunity)));

        Page<OpportunityDTO> result = opportunityService.searchOpportunities(null, null, null, null, null, null, pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
    }
}
