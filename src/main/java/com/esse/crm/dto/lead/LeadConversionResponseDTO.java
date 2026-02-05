package com.esse.crm.dto.lead;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeadConversionResponseDTO {
    private Long leadId;
    private Long accountId;
    private Long contactId;
    private Long opportunityId;
}
