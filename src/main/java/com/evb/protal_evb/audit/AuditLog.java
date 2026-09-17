package com.evb.protal_evb.audit;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "entity_type", nullable = false)
    private String entityType;

    @Column(name = "entity_id", nullable = false)
    private Integer entityId;

    @Column(name = "action", nullable = false)
    private String action;

    @Column(name = "performed_by_user_id")
    private Integer performedByUserId;

    @Column(name = "performed_by_name", nullable = false)
    private String performedByName;

    @Column(name = "performed_at", nullable = false)
    private OffsetDateTime performedAt = OffsetDateTime.now();

    @Column(name = "details")
    private String details;
}
