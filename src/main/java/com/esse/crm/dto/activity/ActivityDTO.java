package com.esse.crm.dto.activity;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityDTO {
    private Long id;

    @NotNull(message = "Type is required")
    private ActivityType type;

    @NotBlank(message = "Subject is required")
    private String subject;

    private String description;

    private LocalDateTime dueAt;

    private boolean completed;

    private String outcome;

    private Long leadId;
    private Long opportunityId;
    private Long accountId;
    private Long contactId;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
