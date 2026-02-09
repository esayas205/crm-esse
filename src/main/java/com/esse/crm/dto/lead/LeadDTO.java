package com.esse.crm.dto.lead;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.esse.crm.dto.activity.ActivityDTO;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeadDTO {
    private Long id;

    @NotNull(message = "Source is required")
    private LeadSource source;

    @NotBlank(message = "Company is required")
    private String company;

    @NotBlank(message = "Contact name is required")
    private String contactName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    private String phone;

    @NotNull(message = "Status is required")
    private LeadStatus status;

    private String ownerUser;

    private List<ActivityDTO> activities;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
