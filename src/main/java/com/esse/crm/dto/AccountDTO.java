package com.esse.crm.dto;

import com.esse.crm.dto.activity.ActivityDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountDTO {
    private Long id;

    @NotBlank(message = "Account name is required")
    private String accountName;

    private String industry;
    private String website;
    private String phone;
    private String billingAddress;
    private String shippingAddress;

    @NotNull(message = "Status is required")
    private AccountStatus status;

    private List<ContactDTO> contacts;
    private List<ActivityDTO> activities;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
