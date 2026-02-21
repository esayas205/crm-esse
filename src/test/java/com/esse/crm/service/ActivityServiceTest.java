package com.esse.crm.service;

import com.esse.crm.dto.activity.ActivityDTO;
import com.esse.crm.dto.activity.ActivityType;
import com.esse.crm.entity.Activity;
import com.esse.crm.exception.ResourceNotFoundException;
import com.esse.crm.repository.ActivityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ActivityServiceTest {

    @Mock
    private ActivityRepository activityRepository;

    @InjectMocks
    private ActivityService activityService;

    private Activity activity;
    private ActivityDTO activityDTO;

    @BeforeEach
    void setUp() {
        activity = Activity.builder()
                .id(1L)
                .subject("Test Subject")
                .type(ActivityType.TASK)
                .accountId(1L)
                .build();

        activityDTO = ActivityDTO.builder()
                .id(1L)
                .subject("Test Subject")
                .type(ActivityType.TASK)
                .accountId(1L)
                .build();
    }

    @Test
    void createActivity_ShouldReturnDTO_WhenValid() {
        when(activityRepository.save(any(Activity.class))).thenReturn(activity);

        ActivityDTO result = activityService.createActivity(activityDTO);

        assertNotNull(result);
        assertEquals("Test Subject", result.getSubject());
        verify(activityRepository).save(any(Activity.class));
    }

    @Test
    void createActivity_ShouldThrowException_WhenNoParent() {
        activityDTO.setAccountId(null);
        assertThrows(IllegalArgumentException.class, () -> activityService.createActivity(activityDTO));
    }

    @Test
    void createActivity_ShouldThrowException_WhenMultipleParents() {
        activityDTO.setLeadId(1L);
        assertThrows(IllegalArgumentException.class, () -> activityService.createActivity(activityDTO));
    }

    @Test
    void completeActivity_ShouldUpdateStatus() {
        when(activityRepository.findById(1L)).thenReturn(Optional.of(activity));
        when(activityRepository.save(any(Activity.class))).thenReturn(activity);

        ActivityDTO result = activityService.completeActivity(1L, "Done");

        assertNotNull(result);
        assertTrue(activity.isCompleted());
        assertEquals("Done", activity.getOutcome());
    }

    @Test
    void getActivity_ShouldThrowNotFound_WhenInvalidId() {
        when(activityRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> activityService.getActivity(1L));
    }
}
