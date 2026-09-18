package com.evb.portal_evb.patients.patient.dto;

import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

public record PatientUpdateRequest(
        @Size(max = 150, message = "O nome deve ter no máximo 150 caracteres")
        String name,

        OffsetDateTime callTime,

        @Size(max = 150, message = "O responsável deve ter no máximo 150 caracteres")
        String responsible,

        @Size(max = 150, message = "O relato deve ter no máximo 150 caracteres")
        String callReport
) {}
