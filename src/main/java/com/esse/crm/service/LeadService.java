package com.esse.crm.service;

import com.esse.crm.dto.AccountDTO;
import com.esse.crm.dto.AccountStatus;
import com.esse.crm.dto.ContactDTO;
import com.esse.crm.dto.lead.*;
import com.esse.crm.dto.opportunity.OpportunityDTO;
import com.esse.crm.dto.opportunity.OpportunityStage;
import com.esse.crm.entity.Account;
import com.esse.crm.entity.Contact;
import com.esse.crm.entity.Lead;
import com.esse.crm.entity.Activity;
import com.esse.crm.dto.activity.ActivityType;
import com.esse.crm.repository.ActivityRepository;
import com.esse.crm.exception.ConflictException;
import com.esse.crm.exception.ResourceNotFoundException;
import com.esse.crm.repository.AccountRepository;
import com.esse.crm.repository.ContactRepository;
import com.esse.crm.repository.LeadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class LeadService {

    private final LeadRepository leadRepository;
    private final AccountRepository accountRepository;
    private final ContactRepository contactRepository;
    private final OpportunityService opportunityService;
    private final ActivityRepository activityRepository;

    @Transactional
    public LeadDTO createLead(LeadDTO leadDTO) {
        if (leadRepository.findByEmail(leadDTO.getEmail()).isPresent()) {
            throw new ConflictException("Lead with this email already exists");
        }
        Lead lead = convertToEntity(leadDTO);
        if (lead.getStatus() == null) {
            lead.setStatus(LeadStatus.NEW);
        }
        Lead savedLead = leadRepository.save(lead);

        Activity autoActivity = Activity.builder()
                .type(ActivityType.NOTE)
                .subject("Lead Created")
                .description("A new lead is created with a name " + savedLead.getContactName())
                .leadId(savedLead.getId())
                .completed(true)
                .build();
        activityRepository.save(autoActivity);

        return convertToDTO(savedLead);
    }

    public LeadDTO getLead(Long id) {
        return convertToDTO(leadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lead not found with id: " + id)));
    }

    public Page<LeadDTO> searchLeads(LeadStatus status, String ownerUser, LeadSource source, String searchTerm, Pageable pageable) {
        return leadRepository.search(status, ownerUser, source, searchTerm, pageable)
                .map(this::convertToDTO);
    }

    @Transactional
    public LeadDTO updateLead(Long id, LeadDTO leadDTO) {
        Lead lead = leadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lead not found with id: " + id));
        
        lead.setSource(leadDTO.getSource());
        lead.setCompany(leadDTO.getCompany());
        lead.setContactName(leadDTO.getContactName());
        lead.setEmail(leadDTO.getEmail());
        lead.setPhone(leadDTO.getPhone());
        lead.setStatus(leadDTO.getStatus());
        lead.setOwnerUser(leadDTO.getOwnerUser());
        
        Lead updatedLead = leadRepository.save(lead);

        Activity autoActivity = Activity.builder()
                .type(ActivityType.NOTE)
                .subject("Lead Updated")
                .description("Lead " + updatedLead.getContactName() + " was updated")
                .leadId(updatedLead.getId())
                .completed(true)
                .build();
        activityRepository.save(autoActivity);

        return convertToDTO(updatedLead);
    }

    @Transactional
    public void deleteLead(Long id) {
        Lead lead = leadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lead not found with id: " + id));
        
        Activity autoActivity = Activity.builder()
                .type(ActivityType.NOTE)
                .subject("Lead Deleted")
                .description("Lead " + lead.getContactName() + " was deleted")
                .leadId(lead.getId())
                .completed(true)
                .build();
        activityRepository.save(autoActivity);

        leadRepository.delete(lead);
    }

    @Transactional
    public LeadConversionResponseDTO convertLead(Long id) {
        Lead lead = leadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lead not found with id: " + id));

        if (lead.getStatus() != LeadStatus.QUALIFIED) {
            throw new IllegalStateException("Only QUALIFIED leads can be converted");
        }

        // 1. Find or create Account
        Account account = accountRepository.findByAccountName(lead.getCompany())
                .orElseGet(() -> {
                    Account newAccount = Account.builder()
                            .accountName(lead.getCompany())
                            .phone(lead.getPhone())
                            .status(AccountStatus.ACTIVE)
                            .build();
                    Account saved = accountRepository.save(newAccount);

                    Activity autoActivity = Activity.builder()
                            .type(ActivityType.NOTE)
                            .subject("Account Created from Lead")
                            .description("Account " + saved.getAccountName() + " created during lead conversion")
                            .accountId(saved.getId())
                            .completed(true)
                            .build();
                    activityRepository.save(autoActivity);
                    return saved;
                });

        // 2. Find or create Contact
        Contact contact = contactRepository.findByEmail(lead.getEmail())
                .orElseGet(() -> {
                    String[] nameParts = lead.getContactName().trim().split("\\s+", 2);
                    String firstName = nameParts[0];
                    String lastName = nameParts.length > 1 ? nameParts[1] : "N/A";

                    Contact newContact = Contact.builder()
                            .firstName(firstName)
                            .lastName(lastName)
                            .email(lead.getEmail())
                            .phone(lead.getPhone())
                            .isPrimaryContact(true)
                            .account(account)
                            .build();
                    Contact saved = contactRepository.save(newContact);

                    Activity autoActivity = Activity.builder()
                            .type(ActivityType.NOTE)
                            .subject("Contact Created from Lead")
                            .description("Contact " + saved.getFirstName() + " " + saved.getLastName() + " created during lead conversion")
                            .contactId(saved.getId())
                            .completed(true)
                            .build();
                    activityRepository.save(autoActivity);
                    return saved;
                });

        // 3. Create Opportunity
        OpportunityDTO opportunityDTO = OpportunityDTO.builder()
                .name(lead.getCompany() + " - Opportunity")
                .stage(OpportunityStage.PROSPECTING)
                .amount(BigDecimal.ZERO)
                .accountId(account.getId())
                .primaryLeadId(lead.getId())
                .build();
        
        OpportunityDTO savedOpportunity = opportunityService.createOpportunity(opportunityDTO);

        lead.setStatus(LeadStatus.CONVERTED);
        leadRepository.save(lead);

        return LeadConversionResponseDTO.builder()
                .leadId(lead.getId())
                .accountId(account.getId())
                .contactId(contact.getId())
                .opportunityId(savedOpportunity.getId())
                .build();
    }

    private Lead convertToEntity(LeadDTO dto) {
        return Lead.builder()
                .id(dto.getId())
                .source(dto.getSource())
                .company(dto.getCompany())
                .contactName(dto.getContactName())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .status(dto.getStatus())
                .ownerUser(dto.getOwnerUser())
                .build();
    }

    private LeadDTO convertToDTO(Lead entity) {
        return LeadDTO.builder()
                .id(entity.getId())
                .source(entity.getSource())
                .company(entity.getCompany())
                .contactName(entity.getContactName())
                .email(entity.getEmail())
                .phone(entity.getPhone())
                .status(entity.getStatus())
                .ownerUser(entity.getOwnerUser())
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
