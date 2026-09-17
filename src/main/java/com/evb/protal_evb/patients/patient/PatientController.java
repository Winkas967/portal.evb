package com.evb.protal_evb.patients.patient;

import com.evb.protal_evb.patients.patient.dto.PatientRequest;
import com.evb.protal_evb.patients.patient.dto.PatientResponse;
import com.evb.protal_evb.patients.patient.dto.PatientUpdateRequest;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @GetMapping
    public List<PatientResponse> findAll() {
        return patientService.findAll();
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PatientResponse> create(@Valid @RequestBody PatientRequest request) {
        PatientResponse created = patientService.create(request);
        return ResponseEntity.created(URI.create("/api/patients/" + created.id())).body(created);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PatientResponse> update(@PathVariable Integer id, @Valid @RequestBody PatientUpdateRequest request) {
        return ResponseEntity.ok(patientService.update(id, request));
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<PatientResponse> deactivate(@PathVariable Integer id) {
        return ResponseEntity.ok(patientService.deactivate(id));
    }

    @PatchMapping("/{id}/reactivate")
    public ResponseEntity<PatientResponse> reactivate(@PathVariable Integer id) {
        return ResponseEntity.ok(patientService.reactivate(id));
    }
}
