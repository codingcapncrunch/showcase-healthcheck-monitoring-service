package com.lozano.showcase.healthcheck_monitoring_service.domain.model;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "ALERT")
public class AlertEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ALERT_ID")
    private String id;

    @Column(name = "HEALTHCHECK_ID")
    private String healthCheckId;

    @Column(name = "CREATED_DATE_TIME")
    private LocalDateTime createdDateTime;

    @Column(name = "END_DATE_TIME")
    private LocalDateTime endDateTime;

    @Column(name = "ACTIVE")
    private boolean active;
}
