package com.esse.crm.service;

import com.esse.crm.dto.activity.ActivityDTO;
import com.esse.crm.dto.activity.ActivityType;
import com.esse.crm.entity.Activity;
import com.esse.crm.exception.ResourceNotFoundException;
import com.esse.crm.repository.ActivityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ActivityService {

    private final ActivityRepository activityRepository;

    public ActivityDTO createActivity(ActivityDTO dto) {
        validateParent(dto);
        Activity activity = convertToEntity(dto);
        return convertToDTO(activityRepository.save(activity));
    }

    public ActivityDTO getActivity(Long id) {
        return convertToDTO(activityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Activity not found with id: " + id)));
    }

    public Page<ActivityDTO> searchActivities(Boolean completed, ActivityType type, LocalDateTime startDate, LocalDateTime endDate, Long leadId, Long opportunityId, Long accountId, Long contactId, Pageable pageable) {
        return activityRepository.search(completed, type, startDate, endDate, leadId, opportunityId, accountId, contactId, pageable)
                .map(this::convertToDTO);
    }

    public ActivityDTO updateActivity(Long id, ActivityDTO dto) {
        Activity activity = activityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Activity not found with id: " + id));

        validateParent(dto);

        activity.setType(dto.getType());
        activity.setSubject(dto.getSubject());
        activity.setDescription(dto.getDescription());
        activity.setDueAt(dto.getDueAt());
        activity.setCompleted(dto.isCompleted());
        activity.setOutcome(dto.getOutcome());
        activity.setLeadId(dto.getLeadId());
        activity.setOpportunityId(dto.getOpportunityId());
        activity.setAccountId(dto.getAccountId());
        activity.setContactId(dto.getContactId());

        return convertToDTO(activityRepository.save(activity));
    }

    public ActivityDTO completeActivity(Long id, String outcome) {
        Activity activity = activityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Activity not found with id: " + id));

        activity.setCompleted(true);
        activity.setOutcome(outcome);

        return convertToDTO(activityRepository.save(activity));
    }

    public void deleteActivity(Long id) {
        if (!activityRepository.existsById(id)) {
            throw new ResourceNotFoundException("Activity not found with id: " + id);
        }
        activityRepository.deleteById(id);
    }

    private void validateParent(ActivityDTO dto) {
        int parents = 0;
        if (dto.getLeadId() != null) parents++;
        if (dto.getOpportunityId() != null) parents++;
        if (dto.getAccountId() != null) parents++;
        if (dto.getContactId() != null) parents++;

        if (parents != 1) {
            throw new IllegalArgumentException("Activity must be linked to exactly one parent: lead, opportunity, account, or contact");
        }
    }

    private Activity convertToEntity(ActivityDTO dto) {
        return Activity.builder()
                .id(dto.getId())
                .type(dto.getType())
                .subject(dto.getSubject())
                .description(dto.getDescription())
                .dueAt(dto.getDueAt())
                .completed(dto.isCompleted())
                .outcome(dto.getOutcome())
                .leadId(dto.getLeadId())
                .opportunityId(dto.getOpportunityId())
                .accountId(dto.getAccountId())
                .contactId(dto.getContactId())
                .build();
    }

    private ActivityDTO convertToDTO(Activity entity) {
        return ActivityDTO.builder()
                .id(entity.getId())
                .type(entity.getType())
                .subject(entity.getSubject())
                .description(entity.getDescription())
                .dueAt(entity.getDueAt())
                .completed(entity.isCompleted())
                .outcome(entity.getOutcome())
                .leadId(entity.getLeadId())
                .opportunityId(entity.getOpportunityId())
                .accountId(entity.getAccountId())
                .contactId(entity.getContactId())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
