package com.lozano.showcase.healthcheck_monitoring_service.api.model;

import lombok.Data;

import java.util.Set;

@Data
public class HealthCheck {

    private String id;

    private String url;

    private boolean active;

    private HealthCheckHttpMethod httpMethod;

    private Integer alertThreshold;

    private Set<KeyValuePair> params;

    private Set<KeyValuePair> headers;
}
