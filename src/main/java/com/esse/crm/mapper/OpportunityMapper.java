package com.esse.crm.mapper;

import com.esse.crm.dto.opportunity.OpportunityDTO;
import com.esse.crm.entity.Opportunity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {ActivityMapper.class})
public interface OpportunityMapper {

    @Mapping(target = "accountId", source = "account.id")
    @Mapping(target = "primaryLeadId", source = "primaryLead.id")
    OpportunityDTO toDTO(Opportunity opportunity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "account", ignore = true)
    @Mapping(target = "primaryLead", ignore = true)
    @Mapping(target = "activities", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    Opportunity toEntity(OpportunityDTO opportunityDTO);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "account", ignore = true)
    @Mapping(target = "primaryLead", ignore = true)
    @Mapping(target = "activities", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    void updateOpportunityFromDto(OpportunityDTO opportunityDTO, @MappingTarget Opportunity opportunity);
}
