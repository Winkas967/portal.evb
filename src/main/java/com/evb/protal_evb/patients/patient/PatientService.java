package com.evb.protal_evb.patients.patient;

import com.evb.protal_evb.audit.AuditLogService;
import com.evb.protal_evb.patients.patient.dto.PatientRequest;
import com.evb.protal_evb.patients.patient.dto.PatientResponse;
import com.evb.protal_evb.patients.patient.dto.PatientUpdateRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class PatientService {

    private static final String ENTITY_TYPE = "PATIENT";

    private final PatientRepository patientRepository;
    private final AuditLogService auditLogService;

    public PatientService(PatientRepository patientRepository, AuditLogService auditLogService) {
        this.patientRepository = patientRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public List<PatientResponse> findAll() {
        return patientRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional
    public PatientResponse create(PatientRequest request) {
        Patient patient = new Patient();
        patient.setName(request.name().trim());
        patient.setCallTime(request.callTime() != null ? request.callTime() : OffsetDateTime.now());
        patient.setResponsible(request.responsible().trim());
        patient.setCallReport(request.callReport().trim());

        Patient saved = patientRepository.save(patient);
        auditLogService.record(ENTITY_TYPE, saved.getId(), "CREATE", "Paciente cadastrado: " + saved.getName());
        return toResponse(saved);
    }

    @Transactional
    public PatientResponse update(Integer id, PatientUpdateRequest request) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Não foi encontrado paciente com o ID informado."));

        if (!patient.isActive()) {
            throw new IllegalArgumentException("Não é possível editar um paciente inativo.");
        }

        if (request.name() != null) {
            String name = request.name().trim();
            if (name.isEmpty()) {
                throw new IllegalArgumentException("Preencha o campo de nome.");
            }
            patient.setName(name);
        }
        if (request.callTime() != null) {
            patient.setCallTime(request.callTime());
        }
        if (request.responsible() != null) {
            String responsible = request.responsible().trim();
            if (responsible.isEmpty()) {
                throw new IllegalArgumentException("Preencha o campo de responsável.");
            }
            patient.setResponsible(responsible);
        }
        if (request.callReport() != null) {
            String callReport = request.callReport().trim();
            if (callReport.isEmpty()) {
                throw new IllegalArgumentException("Preencha o relato do atendimento.");
            }
            patient.setCallReport(callReport);
        }

        auditLogService.record(ENTITY_TYPE, patient.getId(), "UPDATE", "Paciente atualizado: " + patient.getName());
        return toResponse(patient);
    }

    @Transactional
    public PatientResponse deactivate(Integer id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Não foi encontrado paciente com o ID informado."));

        if (!patient.isActive()) {
            throw new IllegalArgumentException("O paciente já está inativo.");
        }

        patient.setActive(false);
        auditLogService.record(ENTITY_TYPE, patient.getId(), "DEACTIVATE", "Paciente excluído (desativado): " + patient.getName());
        return toResponse(patient);
    }

    @Transactional
    public PatientResponse reactivate(Integer id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Não foi encontrado paciente com o ID informado."));

        if (patient.isActive()) {
            throw new IllegalArgumentException("O paciente já está ativo.");
        }

        patient.setActive(true);
        auditLogService.record(ENTITY_TYPE, patient.getId(), "REACTIVATE", "Paciente reativado: " + patient.getName());
        return toResponse(patient);
    }

    private PatientResponse toResponse(Patient patient) {
        return new PatientResponse(patient.getId(), patient.getName(), patient.getCallTime(),
                patient.getResponsible(), patient.getCallReport(), patient.isActive());
    }
}
