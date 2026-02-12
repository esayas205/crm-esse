package com.esse.crm.entity;

import com.esse.crm.dto.activity.ActivityType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "activities")
@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Activity extends AuditedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActivityType type;

    @Column(nullable = false)
    private String subject;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "due_at")
    private LocalDateTime dueAt;

    private boolean completed;

    private String outcome;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lead_id", insertable = false, updatable = false)
    private Lead lead;

    @Column(name = "lead_id")
    private Long leadId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "opportunity_id", insertable = false, updatable = false)
    private Opportunity opportunity;

    @Column(name = "opportunity_id")
    private Long opportunityId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", insertable = false, updatable = false)
    private Account account;

    @Column(name = "account_id")
    private Long accountId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contact_id", insertable = false, updatable = false)
    private Contact contact;

    @Column(name = "contact_id")
    private Long contactId;

    @PrePersist
    @PreUpdate
    private void validateParent() {
        // Validation removed to allow multiple parents (e.g. Account and Contact in Many-to-Many context)
        // or just let it be. Original code was too strict.
    }
}
