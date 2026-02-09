package com.esse.crm.controller;

import com.esse.crm.dto.ContactDTO;
import com.esse.crm.dto.activity.ActivityDTO;
import com.esse.crm.dto.activity.ActivityType;
import com.esse.crm.service.ActivityService;
import com.esse.crm.service.ContactService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/contacts")
@RequiredArgsConstructor
public class ContactController {

    private final ContactService contactService;
    private final ActivityService activityService;

    @GetMapping
    @PreAuthorize("hasAuthority('CONTACT_READ')")
    public Page<ContactDTO> getAllContacts(
            @RequestParam(required = false) String search,
            Pageable pageable) {
        return contactService.getAllContacts(search, pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('CONTACT_READ')")
    public ResponseEntity<ContactDTO> getContactById(@PathVariable Long id) {
        return ResponseEntity.ok(contactService.getContactById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('CONTACT_WRITE')")
    public ResponseEntity<ContactDTO> createContact(@Valid @RequestBody ContactDTO contactDTO) {
        return new ResponseEntity<>(contactService.createContact(contactDTO), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('CONTACT_WRITE')")
    public ResponseEntity<ContactDTO> updateContact(@PathVariable Long id, @Valid @RequestBody ContactDTO contactDTO) {
        return ResponseEntity.ok(contactService.updateContact(id, contactDTO));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteContact(@PathVariable Long id) {
        contactService.deleteContact(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/activities")
    public Page<ActivityDTO> getContactActivities(
            @PathVariable Long id,
            @RequestParam(required = false) ActivityType type,
            @RequestParam(required = false) Boolean completed,
            Pageable pageable) {
        return activityService.searchActivities(completed, type, null, null, null, null, null, id, pageable);
    }
}
