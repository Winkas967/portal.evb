package com.evb.portal_evb.patients.patient.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

public record PatientRequest(
        @NotBlank(message = "Preencha o nome do paciente")
        @Size(max = 150, message = "O nome deve ter no máximo 150 caracteres")
        String name,

        OffsetDateTime callTime,

        @NotBlank(message = "Preencha o responsável pelo atendimento")
        @Size(max = 150, message = "O responsável deve ter no máximo 150 caracteres")
        String responsible,

        @NotBlank(message = "Preencha o relato do atendimento")
        @Size(max = 150, message = "O relato deve ter no máximo 150 caracteres")
        String callReport
) {}
