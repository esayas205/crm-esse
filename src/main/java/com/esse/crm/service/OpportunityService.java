package com.esse.crm.service;

import com.esse.crm.dto.opportunity.OpportunityDTO;
import com.esse.crm.dto.opportunity.OpportunityStage;
import com.esse.crm.entity.Account;
import com.esse.crm.entity.Lead;
import com.esse.crm.entity.Opportunity;
import com.esse.crm.entity.Activity;
import com.esse.crm.dto.activity.ActivityType;
import com.esse.crm.repository.ActivityRepository;
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
    private final ActivityRepository activityRepository;

    private static final Set<OpportunityStage> FINAL_STAGES = EnumSet.of(OpportunityStage.WON, OpportunityStage.LOST);

    @Transactional
    public OpportunityDTO createOpportunity(OpportunityDTO dto) {
        Opportunity opportunity = convertToEntity(dto);
        Opportunity savedOpportunity = opportunityRepository.save(opportunity);

        Activity autoActivity = Activity.builder()
                .type(ActivityType.NOTE)
                .subject("Opportunity Created")
                .description("A new opportunity is created with a name " + savedOpportunity.getName())
                .opportunityId(savedOpportunity.getId())
                .completed(true)
                .build();
        activityRepository.save(autoActivity);

        return convertToDTO(savedOpportunity);
    }

    public OpportunityDTO getOpportunity(Long id) {
        return convertToDTO(opportunityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Opportunity not found with id: " + id)));
    }

    public Page<OpportunityDTO> searchOpportunities(OpportunityStage stage, Long accountId, LocalDate startDate, LocalDate endDate, BigDecimal minAmount, BigDecimal maxAmount, Pageable pageable) {
        return opportunityRepository.search(stage, accountId, startDate, endDate, minAmount, maxAmount, pageable)
                .map(this::convertToDTO);
    }

    @Transactional
    public OpportunityDTO updateOpportunity(Long id, OpportunityDTO dto) {
        Opportunity opportunity = opportunityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Opportunity not found with id: " + id));

        opportunity.setName(dto.getName());
        opportunity.setStage(dto.getStage());
        opportunity.setAmount(dto.getAmount());
        opportunity.setCloseDate(dto.getCloseDate());
        opportunity.setProbability(dto.getProbability());

        Opportunity updatedOpportunity = opportunityRepository.save(opportunity);

        Activity autoActivity = Activity.builder()
                .type(ActivityType.NOTE)
                .subject("Opportunity Updated")
                .description("Opportunity " + updatedOpportunity.getName() + " was updated")
                .opportunityId(updatedOpportunity.getId())
                .completed(true)
                .build();
        activityRepository.save(autoActivity);

        return convertToDTO(updatedOpportunity);
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

        Activity autoActivity = Activity.builder()
                .type(ActivityType.NOTE)
                .subject("Opportunity Stage Changed")
                .description("Opportunity stage changed from " + currentStage + " to " + newStage)
                .opportunityId(updatedOpportunity.getId())
                .completed(true)
                .build();
        activityRepository.save(autoActivity);

        return convertToDTO(updatedOpportunity);
    }

    @Transactional
    public void deleteOpportunity(Long id) {
        Opportunity opportunity = opportunityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Opportunity not found with id: " + id));
        
        Activity autoActivity = Activity.builder()
                .type(ActivityType.NOTE)
                .subject("Opportunity Deleted")
                .description("Opportunity " + opportunity.getName() + " was deleted")
                .opportunityId(opportunity.getId())
                .completed(true)
                .build();
        activityRepository.save(autoActivity);

        opportunityRepository.delete(opportunity);
    }

    private Opportunity convertToEntity(OpportunityDTO dto) {
        Account account = accountRepository.findById(dto.getAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with id: " + dto.getAccountId()));

        Lead primaryLead = null;
        if (dto.getPrimaryLeadId() != null) {
            primaryLead = leadRepository.findById(dto.getPrimaryLeadId())
                    .orElseThrow(() -> new ResourceNotFoundException("Lead not found with id: " + dto.getPrimaryLeadId()));
        }

        return Opportunity.builder()
                .id(dto.getId())
                .name(dto.getName())
                .stage(dto.getStage())
                .amount(dto.getAmount())
                .closeDate(dto.getCloseDate())
                .probability(dto.getProbability())
                .account(account)
                .primaryLead(primaryLead)
                .build();
    }

    private OpportunityDTO convertToDTO(Opportunity entity) {
        return OpportunityDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .stage(entity.getStage())
                .amount(entity.getAmount())
                .closeDate(entity.getCloseDate())
                .probability(entity.getProbability())
                .accountId(entity.getAccount().getId())
                .primaryLeadId(entity.getPrimaryLead() != null ? entity.getPrimaryLead().getId() : null)
                .activities(entity.getActivities() != null ? entity.getActivities().stream()
                        .map(this::convertActivityToDTO)
                        .collect(java.util.stream.Collectors.toList()) : null)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private com.esse.crm.dto.activity.ActivityDTO convertActivityToDTO(com.esse.crm.entity.Activity entity) {
        return com.esse.crm.dto.activity.ActivityDTO.builder()
                .id(entity.getId())
                .type(entity.getType())
                .subject(entity.getSubject())
                .description(entity.getDescription())
                .dueAt(entity.getDueAt())
                .completed(entity.isCompleted())
                .outcome(entity.getOutcome())
                .leadId(entity.getLeadId())
                .opportunityId(entity.getOpportunityId())
                .accountId(entity.getAccountId())
                .contactId(entity.getContactId())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
