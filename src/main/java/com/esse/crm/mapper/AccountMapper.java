package com.esse.crm.mapper;

import com.esse.crm.dto.AccountDTO;
import com.esse.crm.entity.Account;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {ContactMapper.class, ActivityMapper.class})
public interface AccountMapper {

    AccountDTO toDTO(Account account);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "contacts", ignore = true)
    @Mapping(target = "opportunities", ignore = true)
    @Mapping(target = "activities", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    Account toEntity(AccountDTO accountDTO);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "contacts", ignore = true)
    @Mapping(target = "opportunities", ignore = true)
    @Mapping(target = "activities", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    void updateAccountFromDto(AccountDTO accountDTO, @MappingTarget Account account);
}
