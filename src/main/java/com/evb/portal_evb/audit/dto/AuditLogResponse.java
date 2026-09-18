package com.evb.portal_evb.audit.dto;

import java.time.OffsetDateTime;

public record AuditLogResponse (
        Integer id,
        String entityType,
        Integer entityId,
        String action,
        String performedByName,
        OffsetDateTime performedAt,
        String details
){}
