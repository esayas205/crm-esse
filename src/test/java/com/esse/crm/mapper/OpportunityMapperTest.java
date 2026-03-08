package com.esse.crm.mapper;

import com.esse.crm.dto.opportunity.OpportunityDTO;
import com.esse.crm.dto.opportunity.OpportunityStage;
import com.esse.crm.entity.Account;
import com.esse.crm.entity.Opportunity;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class OpportunityMapperTest {

    private final OpportunityMapper opportunityMapper = Mappers.getMapper(OpportunityMapper.class);

    @Test
    void shouldMapOpportunityToDTO() {
        Account account = Account.builder().id(10L).build();
        Opportunity opportunity = Opportunity.builder()
                .id(1L)
                .name("Big Deal")
                .stage(OpportunityStage.PROSPECTING)
                .amount(new BigDecimal("1000.00"))
                .account(account)
                .build();

        OpportunityDTO dto = opportunityMapper.toDTO(opportunity);

        assertNotNull(dto);
        assertEquals(opportunity.getId(), dto.getId());
        assertEquals(opportunity.getName(), dto.getName());
        assertEquals(opportunity.getStage(), dto.getStage());
        assertEquals(opportunity.getAmount(), dto.getAmount());
        assertEquals(10L, dto.getAccountId());
    }
}
