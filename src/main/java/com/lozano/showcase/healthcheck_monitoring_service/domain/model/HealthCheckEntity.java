package com.lozano.showcase.healthcheck_monitoring_service.domain.model;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Data
@Entity
@Table(name="HEALTHCHECK")
public class HealthCheckEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "HEALTHCHECK_ID")
    private String id;

    @Column(name = "HEALTHCHECK_URL")
    private String url;

    @Column(name = "ACTIVE")
    private boolean active;

    @Column(name = "HTTP_METHOD")
    private String httpMethod;

    @Column(name = "ALERT_THRESHOLD")
    private Integer alertThreshold;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "HEALTHCHECK_ID", referencedColumnName = "HEALTHCHECK_ID")
    private Set<HealthCheckParamEntity> params;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "HEALTHCHECK_ID", referencedColumnName = "HEALTHCHECK_ID")
    private Set<HealthCheckHeaderEntity> headers;

    @Transient
    public Map<String, String> convertParamEntityToURLParams(){
        Map<String, String> params = new HashMap<>();
        for (HealthCheckParamEntity healthCheckParamEntity : this.getParams()){
            params.put(healthCheckParamEntity.getParamName(), healthCheckParamEntity.getParamValue());
        }
        return params;
    }
}
