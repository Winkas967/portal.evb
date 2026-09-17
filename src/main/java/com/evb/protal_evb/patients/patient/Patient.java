package com.evb.protal_evb.patients.patient;

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
@Table(name = "patients")
@Getter
@Setter
@NoArgsConstructor
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "call_time", nullable = false)
    private OffsetDateTime callTime;

    @Column(name = "responsible", nullable = false)
    private String responsible;

    @Column(name = "call_report", nullable = false)
    private String callReport;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;
}
