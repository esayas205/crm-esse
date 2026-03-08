package com.esse.crm.mapper;

import com.esse.crm.dto.activity.ActivityDTO;
import com.esse.crm.entity.Activity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ActivityMapper {

    ActivityDTO toDTO(Activity activity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "lead", ignore = true)
    @Mapping(target = "opportunity", ignore = true)
    @Mapping(target = "account", ignore = true)
    @Mapping(target = "contact", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    Activity toEntity(ActivityDTO activityDTO);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "lead", ignore = true)
    @Mapping(target = "opportunity", ignore = true)
    @Mapping(target = "account", ignore = true)
    @Mapping(target = "contact", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    void updateActivityFromDto(ActivityDTO activityDTO, @MappingTarget Activity activity);
}
