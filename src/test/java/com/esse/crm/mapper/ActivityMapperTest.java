package com.esse.crm.mapper;

import com.esse.crm.dto.activity.ActivityDTO;
import com.esse.crm.dto.activity.ActivityType;
import com.esse.crm.entity.Activity;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.*;

class ActivityMapperTest {

    private final ActivityMapper activityMapper = Mappers.getMapper(ActivityMapper.class);

    @Test
    void shouldMapActivityToDTO() {
        Activity activity = Activity.builder()
                .id(1L)
                .type(ActivityType.CALL)
                .subject("Intro Call")
                .accountId(10L)
                .build();

        ActivityDTO dto = activityMapper.toDTO(activity);

        assertNotNull(dto);
        assertEquals(activity.getId(), dto.getId());
        assertEquals(activity.getType(), dto.getType());
        assertEquals(activity.getSubject(), dto.getSubject());
        assertEquals(10L, dto.getAccountId());
    }
}
