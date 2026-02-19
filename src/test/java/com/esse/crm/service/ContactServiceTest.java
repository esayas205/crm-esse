package com.esse.crm.service;

import com.esse.crm.dto.ContactDTO;
import com.esse.crm.entity.Account;
import com.esse.crm.entity.Contact;
import com.esse.crm.exception.ResourceNotFoundException;
import com.esse.crm.repository.AccountRepository;
import com.esse.crm.repository.ContactRepository;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ContactServiceTest {

    @Mock
    private ContactRepository contactRepository;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private ContactService contactService;

    private Contact contact;
    private ContactDTO contactDTO;
    private Account account;

    @BeforeEach
    void setUp() {
        account = Account.builder()
                .id(1L)
                .accountName("Test Account")
                .contacts(new ArrayList<>())
                .build();

        contact = Contact.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .accounts(new ArrayList<>(Collections.singletonList(account)))
                .build();

        contactDTO = ContactDTO.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .accountIds(Collections.singletonList(1L))
                .build();
    }

    @Test
    void getAllContacts_ShouldReturnPage() {
        Pageable pageable = PageRequest.of(0, 10);
        when(contactRepository.searchContacts(any(), any())).thenReturn(new PageImpl<>(Collections.singletonList(contact)));

        Page<ContactDTO> result = contactService.getAllContacts("John", pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(contactRepository).searchContacts("John", pageable);
    }

    @Test
    void createContact_ShouldReturnDTO_WhenAccountsExist() {
        when(accountRepository.findAllById(any())).thenReturn(Collections.singletonList(account));
        when(contactRepository.save(any(Contact.class))).thenReturn(contact);

        ContactDTO result = contactService.createContact(contactDTO);

        assertNotNull(result);
        assertEquals("John", result.getFirstName());
        assertTrue(account.getContacts().contains(contact));
        verify(contactRepository).save(any(Contact.class));
    }

    @Test
    void createContact_ShouldThrowException_WhenAccountNotFound() {
        when(accountRepository.findAllById(any())).thenReturn(Collections.emptyList());

        assertThrows(ResourceNotFoundException.class, () -> contactService.createContact(contactDTO));
    }

    @Test
    void updateContact_ShouldReturnUpdatedDTO() {
        when(contactRepository.findById(1L)).thenReturn(Optional.of(contact));
        when(accountRepository.findAllById(any())).thenReturn(Collections.singletonList(account));
        when(contactRepository.save(any(Contact.class))).thenReturn(contact);

        contactDTO.setFirstName("Jane");
        ContactDTO result = contactService.updateContact(1L, contactDTO);

        assertNotNull(result);
        assertEquals("Jane", result.getFirstName());
    }

    @Test
    void deleteContact_ShouldCallRepository() {
        when(contactRepository.findById(1L)).thenReturn(Optional.of(contact));
        doNothing().when(contactRepository).delete(contact);

        contactService.deleteContact(1L);

        verify(contactRepository).delete(contact);
    }
}
