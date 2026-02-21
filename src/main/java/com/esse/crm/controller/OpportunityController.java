package com.esse.crm.controller;

import com.esse.crm.dto.opportunity.OpportunityDTO;
import com.esse.crm.dto.opportunity.OpportunityStage;
import com.esse.crm.service.OpportunityService;
import com.esse.crm.service.ActivityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/opportunities")
@RequiredArgsConstructor
public class OpportunityController {

    private final OpportunityService opportunityService;
    private final ActivityService activityService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('DEAL_WRITE')")
    public OpportunityDTO createOpportunity(@Valid @RequestBody OpportunityDTO dto) {
        return opportunityService.createOpportunity(dto);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('DEAL_READ')")
    public OpportunityDTO getOpportunity(@PathVariable Long id) {
        return opportunityService.getOpportunity(id);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('DEAL_READ')")
    public Page<OpportunityDTO> searchOpportunities(
            @RequestParam(required = false) OpportunityStage stage,
            @RequestParam(required = false) Long accountId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount,
            @PageableDefault(size = 20) Pageable pageable) {
        return opportunityService.searchOpportunities(stage, accountId, startDate, endDate, minAmount, maxAmount, pageable);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('DEAL_WRITE')")
    public OpportunityDTO updateOpportunity(@PathVariable Long id, @Valid @RequestBody OpportunityDTO dto) {
        return opportunityService.updateOpportunity(id, dto);
    }

    @PatchMapping("/{id}/stage")
    @PreAuthorize("hasAuthority('DEAL_WRITE')")
    public OpportunityDTO advanceStage(@PathVariable Long id, @RequestParam OpportunityStage stage) {
        return opportunityService.advanceStage(id, stage);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public void deleteOpportunity(@PathVariable Long id) {
        opportunityService.deleteOpportunity(id);
    }

    @GetMapping("/{id}/activities")
    public Page<com.esse.crm.dto.activity.ActivityDTO> getOpportunityActivities(
            @PathVariable Long id,
            @RequestParam(required = false) com.esse.crm.dto.activity.ActivityType type,
            @RequestParam(required = false) Boolean completed,
            @PageableDefault(size = 20) Pageable pageable) {
        return activityService.searchActivities(completed, type, null, null, null, id, null, null, pageable);
    }
}
