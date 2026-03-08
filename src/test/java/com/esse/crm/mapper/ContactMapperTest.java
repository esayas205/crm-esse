package com.esse.crm.mapper;

import com.esse.crm.dto.ContactDTO;
import com.esse.crm.entity.Account;
import com.esse.crm.entity.Contact;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ContactMapperTest {

    private final ContactMapper contactMapper = Mappers.getMapper(ContactMapper.class);

    @Test
    void shouldMapContactToDTO() {
        Account account = Account.builder().id(10L).build();
        Contact contact = Contact.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .accounts(List.of(account))
                .build();

        ContactDTO dto = contactMapper.toDTO(contact);

        assertNotNull(dto);
        assertEquals(contact.getId(), dto.getId());
        assertEquals(contact.getFirstName(), dto.getFirstName());
        assertEquals(contact.getLastName(), dto.getLastName());
        assertEquals(contact.getEmail(), dto.getEmail());
        assertEquals(1, dto.getAccountIds().size());
        assertEquals(10L, dto.getAccountIds().get(0));
    }

    @Test
    void shouldMapDTOToContact() {
        ContactDTO dto = ContactDTO.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .build();

        Contact contact = contactMapper.toEntity(dto);

        assertNotNull(contact);
        assertNull(contact.getId());
        assertEquals(dto.getFirstName(), contact.getFirstName());
        assertEquals(dto.getLastName(), contact.getLastName());
        assertEquals(dto.getEmail(), contact.getEmail());
    }
}
