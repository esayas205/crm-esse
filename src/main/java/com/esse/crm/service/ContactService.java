package com.esse.crm.service;

import com.esse.crm.dto.ContactDTO;
import com.esse.crm.dto.activity.ActivityDTO;
import com.esse.crm.dto.activity.ActivityType;
import com.esse.crm.entity.Account;
import com.esse.crm.entity.Activity;
import com.esse.crm.entity.Contact;
import com.esse.crm.exception.ResourceNotFoundException;
import com.esse.crm.repository.AccountRepository;
import com.esse.crm.repository.ActivityRepository;
import com.esse.crm.repository.ContactRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContactService {

    private final ContactRepository contactRepository;
    private final AccountRepository accountRepository;
    private final ActivityRepository activityRepository;

    public Page<ContactDTO> getAllContacts(String searchTerm, Pageable pageable) {
        return contactRepository.searchContacts(searchTerm, pageable)
                .map(this::convertToDTO);
    }

    public Page<ContactDTO> getContactsByAccount(Long accountId, Pageable pageable) {
        return contactRepository.findByAccountId(accountId, pageable)
                .map(this::convertToDTO);
    }

    public ContactDTO getContactById(Long id) {
        Contact contact = contactRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contact not found with id: " + id));
        return convertToDTO(contact);
    }

    @Transactional
    public ContactDTO createContact(ContactDTO contactDTO) {
        List<Account> accounts = accountRepository.findAllById(contactDTO.getAccountIds());
        if (accounts.size() != contactDTO.getAccountIds().size()) {
            throw new ResourceNotFoundException("One or more accounts not found");
        }

        Contact contact = convertToEntity(contactDTO);
        // In ManyToMany, we need to manage both sides if we want it to be reflected in the session
        // but typically saving the owning side (Account) works if configured, 
        // or just setting the collection on the side we're saving.
        // Since Account "owns" the JoinTable in our config:
        contact.setAccounts(accounts);
        for (Account account : accounts) {
            account.getContacts().add(contact);
        }
        
        Contact savedContact = contactRepository.save(contact);

        Activity autoActivity = Activity.builder()
                .type(ActivityType.NOTE)
                .subject("Contact Created")
                .description("A new contact is created: " + savedContact.getFirstName() + " " + savedContact.getLastName())
                .contactId(savedContact.getId())
                .completed(true)
                .build();
        activityRepository.save(autoActivity);

        return convertToDTO(savedContact);
    }

    @Transactional
    public ContactDTO updateContact(Long id, ContactDTO contactDTO) {
        Contact contact = contactRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contact not found with id: " + id));
        
        List<Account> accounts = accountRepository.findAllById(contactDTO.getAccountIds());
        if (accounts.size() != contactDTO.getAccountIds().size()) {
            throw new ResourceNotFoundException("One or more accounts not found");
        }

        contact.setFirstName(contactDTO.getFirstName());
        contact.setLastName(contactDTO.getLastName());
        contact.setEmail(contactDTO.getEmail());
        contact.setPhone(contactDTO.getPhone());
        contact.setJobTitle(contactDTO.getJobTitle());
        contact.setPrimaryContact(contactDTO.isPrimaryContact());
        
        // Update relationships
        // Remove from old accounts
        for (Account account : contact.getAccounts()) {
            account.getContacts().remove(contact);
        }
        
        contact.setAccounts(accounts);
        
        // Add to new accounts
        for (Account account : accounts) {
            if (!account.getContacts().contains(contact)) {
                account.getContacts().add(contact);
            }
        }
        
        Contact updatedContact = contactRepository.save(contact);

        Activity autoActivity = Activity.builder()
                .type(ActivityType.NOTE)
                .subject("Contact Updated")
                .description("Contact " + updatedContact.getFirstName() + " " + updatedContact.getLastName() + " was updated")
                .contactId(updatedContact.getId())
                .completed(true)
                .build();
        activityRepository.save(autoActivity);

        return convertToDTO(updatedContact);
    }

    @Transactional
    public void deleteContact(Long id) {
        Contact contact = contactRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contact not found with id: " + id));

        contactRepository.delete(contact);
    }

    private ContactDTO convertToDTO(Contact contact) {
        return ContactDTO.builder()
                .id(contact.getId())
                .firstName(contact.getFirstName())
                .lastName(contact.getLastName())
                .email(contact.getEmail())
                .phone(contact.getPhone())
                .jobTitle(contact.getJobTitle())
                .isPrimaryContact(contact.isPrimaryContact())
                .accountIds(contact.getAccounts() != null ? contact.getAccounts().stream()
                        .map(Account::getId)
                        .collect(Collectors.toList()) : null)
                .activities(contact.getActivities() != null ? contact.getActivities().stream()
                        .map(this::convertActivityToDTO)
                        .collect(Collectors.toList()) : null)
                .createdAt(contact.getCreatedAt())
                .updatedAt(contact.getUpdatedAt())
                .build();
    }

    private ActivityDTO convertActivityToDTO(Activity entity) {
        return ActivityDTO.builder()
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

    private Contact convertToEntity(ContactDTO dto) {
        return Contact.builder()
                .id(dto.getId())
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .jobTitle(dto.getJobTitle())
                .isPrimaryContact(dto.isPrimaryContact())
                .build();
    }
}
