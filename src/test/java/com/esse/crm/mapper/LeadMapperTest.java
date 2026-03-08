package com.esse.crm.mapper;

import com.esse.crm.dto.lead.LeadDTO;
import com.esse.crm.dto.lead.LeadSource;
import com.esse.crm.dto.lead.LeadStatus;
import com.esse.crm.entity.Lead;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.*;

class LeadMapperTest {

    private final LeadMapper leadMapper = Mappers.getMapper(LeadMapper.class);

    @Test
    void shouldMapLeadToDTO() {
        Lead lead = Lead.builder()
                .id(1L)
                .company("Test Corp")
                .contactName("John Doe")
                .email("john@example.com")
                .phone("123456789")
                .source(LeadSource.WEB)
                .status(LeadStatus.NEW)
                .ownerUser("admin")
                .build();

        LeadDTO dto = leadMapper.toDTO(lead);

        assertNotNull(dto);
        assertEquals(lead.getId(), dto.getId());
        assertEquals(lead.getCompany(), dto.getCompany());
        assertEquals(lead.getContactName(), dto.getContactName());
        assertEquals(lead.getEmail(), dto.getEmail());
        assertEquals(lead.getPhone(), dto.getPhone());
        assertEquals(lead.getSource(), dto.getSource());
        assertEquals(lead.getStatus(), dto.getStatus());
        assertEquals(lead.getOwnerUser(), dto.getOwnerUser());
    }

    @Test
    void shouldMapDTOToLead() {
        LeadDTO dto = LeadDTO.builder()
                .company("Test Corp")
                .contactName("John Doe")
                .email("john@example.com")
                .phone("123456789")
                .source(LeadSource.WEB)
                .status(LeadStatus.NEW)
                .ownerUser("admin")
                .build();

        Lead lead = leadMapper.toEntity(dto);

        assertNotNull(lead);
        assertNull(lead.getId()); // Should be ignored
        assertEquals(dto.getCompany(), lead.getCompany());
        assertEquals(dto.getContactName(), lead.getContactName());
        assertEquals(dto.getEmail(), lead.getEmail());
        assertEquals(dto.getPhone(), lead.getPhone());
        assertEquals(dto.getSource(), lead.getSource());
        assertEquals(dto.getStatus(), lead.getStatus());
        assertEquals(dto.getOwnerUser(), lead.getOwnerUser());
    }

    @Test
    void shouldUpdateLeadFromDto() {
        Lead lead = Lead.builder()
                .id(1L)
                .company("Old Corp")
                .status(LeadStatus.NEW)
                .build();

        LeadDTO dto = LeadDTO.builder()
                .company("New Corp")
                .status(LeadStatus.QUALIFIED)
                .build();

        leadMapper.updateLeadFromDto(dto, lead);

        assertEquals(1L, lead.getId()); // Should not be changed
        assertEquals("New Corp", lead.getCompany());
        assertEquals(LeadStatus.QUALIFIED, lead.getStatus());
    }
}
