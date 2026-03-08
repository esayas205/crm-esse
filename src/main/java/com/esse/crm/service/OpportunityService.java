package com.esse.crm.service;

import com.esse.crm.dto.opportunity.OpportunityDTO;
import com.esse.crm.dto.opportunity.OpportunityStage;
import com.esse.crm.mapper.OpportunityMapper;
import com.esse.crm.entity.Account;
import com.esse.crm.entity.Lead;
import com.esse.crm.entity.Opportunity;
import com.esse.crm.entity.Activity;
import com.esse.crm.exception.ResourceNotFoundException;
import com.esse.crm.repository.AccountRepository;
import com.esse.crm.repository.LeadRepository;
import com.esse.crm.repository.OpportunityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class OpportunityService {

    private final OpportunityRepository opportunityRepository;
    private final AccountRepository accountRepository;
    private final LeadRepository leadRepository;
    private final OpportunityMapper opportunityMapper;

    private static final Set<OpportunityStage> FINAL_STAGES = EnumSet.of(OpportunityStage.WON, OpportunityStage.LOST);

    @Transactional
    public OpportunityDTO createOpportunity(OpportunityDTO dto) {
        Account account = accountRepository.findById(dto.getAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with id: " + dto.getAccountId()));

        Lead primaryLead = null;
        if (dto.getPrimaryLeadId() != null) {
            primaryLead = leadRepository.findById(dto.getPrimaryLeadId())
                    .orElseThrow(() -> new ResourceNotFoundException("Lead not found with id: " + dto.getPrimaryLeadId()));
        }

        Opportunity opportunity = opportunityMapper.toEntity(dto);
        opportunity.setAccount(account);
        opportunity.setPrimaryLead(primaryLead);

        Opportunity savedOpportunity = opportunityRepository.save(opportunity);

        return opportunityMapper.toDTO(savedOpportunity);
    }

    @Transactional(readOnly = true)
    public OpportunityDTO getOpportunity(Long id) {
        return opportunityMapper.toDTO(opportunityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Opportunity not found with id: " + id)));
    }

    @Transactional(readOnly = true)
    public Page<OpportunityDTO> searchOpportunities(OpportunityStage stage, Long accountId, LocalDate startDate, LocalDate endDate, BigDecimal minAmount, BigDecimal maxAmount, Pageable pageable) {
        return opportunityRepository.search(stage, accountId, startDate, endDate, minAmount, maxAmount, pageable)
                .map(opportunityMapper::toDTO);
    }

    @Transactional
    public OpportunityDTO updateOpportunity(Long id, OpportunityDTO dto) {
        Opportunity opportunity = opportunityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Opportunity not found with id: " + id));

        opportunityMapper.updateOpportunityFromDto(dto, opportunity);

        Opportunity updatedOpportunity = opportunityRepository.save(opportunity);

        return opportunityMapper.toDTO(updatedOpportunity);
    }

    @Transactional
    public OpportunityDTO advanceStage(Long id, OpportunityStage newStage) {
        Opportunity opportunity = opportunityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Opportunity not found with id: " + id));

        OpportunityStage currentStage = opportunity.getStage();

        if (FINAL_STAGES.contains(currentStage) && currentStage != newStage) {
            throw new IllegalStateException("Cannot change stage once it is in a final state (WON/LOST)");
        }

        opportunity.setStage(newStage);
        Opportunity updatedOpportunity = opportunityRepository.save(opportunity);

        return opportunityMapper.toDTO(updatedOpportunity);
    }

    @Transactional
    public void deleteOpportunity(Long id) {
        Opportunity opportunity = opportunityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Opportunity not found with id: " + id));
        
        opportunityRepository.delete(opportunity);
    }
}
