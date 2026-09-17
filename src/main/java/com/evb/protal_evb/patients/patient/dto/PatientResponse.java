package com.evb.protal_evb.patients.patient.dto;

import java.time.OffsetDateTime;

public record PatientResponse(
        Integer id,
        String name,
        OffsetDateTime callTime,
        String responsible,
        String callReport,
        boolean isActive
) {}
