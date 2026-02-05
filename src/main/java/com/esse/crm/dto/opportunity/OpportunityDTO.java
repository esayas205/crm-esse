package com.esse.crm.dto.opportunity;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.esse.crm.dto.activity.ActivityDTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OpportunityDTO {
    private Long id;

    @NotBlank(message = "Name is required")
    private String name;

    @NotNull(message = "Stage is required")
    private OpportunityStage stage;

    @NotNull(message = "Amount is required")
    private BigDecimal amount;

    private LocalDate closeDate;

    @Min(0)
    @Max(100)
    private Integer probability;

    @NotNull(message = "Account ID is required")
    private Long accountId;

    private Long primaryLeadId;

    private List<ActivityDTO> activities;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
