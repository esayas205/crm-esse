package com.esse.crm.controller;

import com.esse.crm.dto.activity.ActivityDTO;
import com.esse.crm.dto.activity.ActivityType;
import com.esse.crm.service.ActivityService;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/activities")
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityService activityService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ActivityDTO createActivity(@Valid @RequestBody ActivityDTO dto) {
        return activityService.createActivity(dto);
    }

    @GetMapping("/{id}")
    public ActivityDTO getActivity(@PathVariable Long id) {
        return activityService.getActivity(id);
    }

    @GetMapping
    public Page<ActivityDTO> searchActivities(
            @RequestParam(required = false) Boolean completed,
            @RequestParam(required = false) ActivityType type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) Long leadId,
            @RequestParam(required = false) Long opportunityId,
            @RequestParam(required = false) Long accountId,
            @RequestParam(required = false) Long contactId,
            @PageableDefault(size = 20) Pageable pageable) {
        return activityService.searchActivities(completed, type, startDate, endDate, leadId, opportunityId, accountId, contactId, pageable);
    }

    @PutMapping("/{id}")
    public ActivityDTO updateActivity(@PathVariable Long id, @Valid @RequestBody ActivityDTO dto) {
        return activityService.updateActivity(id, dto);
    }

    @PatchMapping("/{id}/complete")
    public ActivityDTO completeActivity(@PathVariable Long id, @RequestParam(required = false) String outcome) {
        return activityService.completeActivity(id, outcome);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteActivity(@PathVariable Long id) {
        activityService.deleteActivity(id);
    }
}
