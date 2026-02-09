package com.esse.crm.controller;

import com.esse.crm.dto.lead.LeadConversionResponseDTO;
import com.esse.crm.dto.lead.LeadDTO;
import com.esse.crm.dto.lead.LeadSource;
import com.esse.crm.dto.lead.LeadStatus;
import com.esse.crm.service.LeadService;
import com.esse.crm.service.ActivityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/leads")
@RequiredArgsConstructor
public class LeadController {

    private final LeadService leadService;
    private final ActivityService activityService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('LEAD_WRITE')")
    public LeadDTO createLead(@Valid @RequestBody LeadDTO leadDTO) {
        return leadService.createLead(leadDTO);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('LEAD_READ')")
    public LeadDTO getLead(@PathVariable Long id) {
        return leadService.getLead(id);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('LEAD_READ')")
    public Page<LeadDTO> searchLeads(
            @RequestParam(required = false) LeadStatus status,
            @RequestParam(required = false) String ownerUser,
            @RequestParam(required = false) LeadSource source,
            @RequestParam(required = false) String searchTerm,
            @PageableDefault(size = 20) Pageable pageable) {
        return leadService.searchLeads(status, ownerUser, source, searchTerm, pageable);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('LEAD_WRITE')")
    public LeadDTO updateLead(@PathVariable Long id, @Valid @RequestBody LeadDTO leadDTO) {
        return leadService.updateLead(id, leadDTO);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public void deleteLead(@PathVariable Long id) {
        leadService.deleteLead(id);
    }

    @PostMapping("/{id}/convert")
    public LeadConversionResponseDTO convertLead(@PathVariable Long id) {
        return leadService.convertLead(id);
    }

    @GetMapping("/{id}/activities")
    public Page<com.esse.crm.dto.activity.ActivityDTO> getLeadActivities(
            @PathVariable Long id,
            @RequestParam(required = false) com.esse.crm.dto.activity.ActivityType type,
            @RequestParam(required = false) Boolean completed,
            @PageableDefault(size = 20) Pageable pageable) {
        return activityService.searchActivities(completed, type, null, null, id, null, null, null, pageable);
    }
}
