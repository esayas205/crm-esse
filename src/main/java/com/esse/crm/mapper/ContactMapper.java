package com.esse.crm.mapper;

import com.esse.crm.dto.ContactDTO;
import com.esse.crm.entity.Account;
import com.esse.crm.entity.Contact;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {ActivityMapper.class})
public interface ContactMapper {

    @Mapping(target = "accountIds", source = "accounts", qualifiedByName = "mapAccountsToIds")
    ContactDTO toDTO(Contact contact);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "accounts", ignore = true)
    @Mapping(target = "activities", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    Contact toEntity(ContactDTO contactDTO);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "accounts", ignore = true)
    @Mapping(target = "activities", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    void updateContactFromDto(ContactDTO contactDTO, @MappingTarget Contact contact);

    @Named("mapAccountsToIds")
    default List<Long> mapAccountsToIds(List<Account> accounts) {
        if (accounts == null) return null;
        return accounts.stream().map(Account::getId).collect(Collectors.toList());
    }
}
