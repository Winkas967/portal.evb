package com.evb.portal_evb.audit;

import com.evb.portal_evb.audit.dto.AuditLogResponse;
import com.evb.portal_evb.security.UserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public void record(String entityType, Integer entityId, String action, String details) {
        AuditLog log = new AuditLog();
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setAction(action);
        log.setDetails(details);

        UserPrincipal current = currentUser();
        if (current != null) {
            log.setPerformedByUserId(current.getId());
            log.setPerformedByName(current.getUsername());
        } else {
            log.setPerformedByName("sistema");
        }
        auditLogRepository.save(log);
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponse> findAll() {
        return auditLogRepository.findAllByOrderByPerformedAtDesc().stream().map(this::toResponse).toList();
    }

    private AuditLogResponse toResponse(AuditLog log) {
        return new AuditLogResponse(log.getId(), log.getEntityType(), log.getEntityId(), log.getAction(),
                log.getPerformedByName(), log.getPerformedAt(), log.getDetails());
    }

    private UserPrincipal currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal principal) {
            return principal;
        }
        return null;
    }
}
