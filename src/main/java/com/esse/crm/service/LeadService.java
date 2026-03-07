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
import com.esse.crm.exception.ConflictException;
import com.esse.crm.exception.ResourceNotFoundException;
import com.esse.crm.repository.AccountRepository;
import com.esse.crm.repository.ContactRepository;
import com.esse.crm.repository.LeadRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
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
    private final ModelMapper modelMapper;

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

        return convertToDTO(savedLead);
    }

    @Transactional(readOnly = true)
    public LeadDTO getLead(Long id) {
        return convertToDTO(leadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lead not found with id: " + id)));
    }

    @Transactional(readOnly = true)
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

        return convertToDTO(updatedLead);
    }

    @Transactional
    public void deleteLead(Long id) {
        Lead lead = leadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lead not found with id: " + id));
        
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
                    return accountRepository.save(newAccount);
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
                            .build();
                    newContact.getAccounts().add(account);
                    return contactRepository.save(newContact);
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
        return modelMapper.map(dto, Lead.class);
    }

    private LeadDTO convertToDTO(Lead entity) {
        return modelMapper.map(entity, LeadDTO.class);
    }

    private com.esse.crm.dto.activity.ActivityDTO convertActivityToDTO(com.esse.crm.entity.Activity entity) {
        return modelMapper.map(entity, com.esse.crm.dto.activity.ActivityDTO.class);
    }
}
