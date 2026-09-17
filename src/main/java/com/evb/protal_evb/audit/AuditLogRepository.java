package com.evb.protal_evb.audit;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Integer> {
    List<AuditLog> findAllByOrderByPerformedAtDesc();
    List<AuditLog> findByEntityTypeAndEntityIdOrderByPerformedAtDesc(String entityType, Integer entityId);
}
