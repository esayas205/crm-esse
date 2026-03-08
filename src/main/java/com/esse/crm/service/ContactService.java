package com.esse.crm.service;

import com.esse.crm.dto.ContactDTO;
import com.esse.crm.dto.activity.ActivityDTO;
import com.esse.crm.mapper.ContactMapper;
import com.esse.crm.entity.Account;
import com.esse.crm.entity.Activity;
import com.esse.crm.entity.Contact;
import com.esse.crm.exception.ResourceNotFoundException;
import com.esse.crm.repository.AccountRepository;
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
    private final ContactMapper contactMapper;

    @Transactional(readOnly = true)
    public Page<ContactDTO> getAllContacts(String searchTerm, Pageable pageable) {
        return contactRepository.searchContacts(searchTerm, pageable)
                .map(contactMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<ContactDTO> getContactsByAccount(Long accountId, Pageable pageable) {
        return contactRepository.findByAccountId(accountId, pageable)
                .map(contactMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public ContactDTO getContactById(Long id) {
        Contact contact = contactRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contact not found with id: " + id));
        return contactMapper.toDTO(contact);
    }

    @Transactional
    public ContactDTO createContact(ContactDTO contactDTO) {
        List<Account> accounts = List.of();
        if (contactDTO.getAccountIds() != null && !contactDTO.getAccountIds().isEmpty()) {
            accounts = accountRepository.findAllById(contactDTO.getAccountIds());
            if (accounts.size() != contactDTO.getAccountIds().size()) {
                throw new ResourceNotFoundException("One or more accounts not found");
            }
        }

        Contact contact = contactMapper.toEntity(contactDTO);
        // In ManyToMany, we need to manage both sides if we want it to be reflected in the session
        // but typically saving the owning side (Account) works if configured, 
        // or just setting the collection on the side we're saving.
        // Since Account "owns" the JoinTable in our config:
        contact.setAccounts(accounts);
        for (Account account : accounts) {
            account.getContacts().add(contact);
        }
        
        Contact savedContact = contactRepository.save(contact);

        return contactMapper.toDTO(savedContact);
    }

    @Transactional
    public ContactDTO updateContact(Long id, ContactDTO contactDTO) {
        Contact contact = contactRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contact not found with id: " + id));
        
        List<Account> accounts = List.of();
        if (contactDTO.getAccountIds() != null && !contactDTO.getAccountIds().isEmpty()) {
            accounts = accountRepository.findAllById(contactDTO.getAccountIds());
            if (accounts.size() != contactDTO.getAccountIds().size()) {
                throw new ResourceNotFoundException("One or more accounts not found");
            }
        }

        contactMapper.updateContactFromDto(contactDTO, contact);
        
        // Update relationships
        // Remove from old accounts
        if (contact.getAccounts() != null) {
            for (Account account : contact.getAccounts()) {
                account.getContacts().remove(contact);
            }
        }
        
        contact.setAccounts(accounts);
        
        // Add to new accounts
        for (Account account : accounts) {
            if (!account.getContacts().contains(contact)) {
                account.getContacts().add(contact);
            }
        }
        
        Contact updatedContact = contactRepository.save(contact);

        return contactMapper.toDTO(updatedContact);
    }

    @Transactional
    public void deleteContact(Long id) {
        Contact contact = contactRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contact not found with id: " + id));

        contactRepository.delete(contact);
    }
}
