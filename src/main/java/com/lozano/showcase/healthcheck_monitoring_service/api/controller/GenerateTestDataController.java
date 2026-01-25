package com.lozano.showcase.healthcheck_monitoring_service.api.controller;

import com.lozano.showcase.healthcheck_monitoring_service.api.model.HealthCheck;
import com.lozano.showcase.healthcheck_monitoring_service.api.model.HealthCheckHttpMethod;
import com.lozano.showcase.healthcheck_monitoring_service.api.model.KeyValuePair;
import com.lozano.showcase.healthcheck_monitoring_service.api.translator.HealthCheckTranslator;
import com.lozano.showcase.healthcheck_monitoring_service.domain.model.HealthCheckEntity;
import com.lozano.showcase.healthcheck_monitoring_service.domain.service.healthcheck.HealthCheckManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping(value = "/test")
public class GenerateTestDataController {

    private HealthCheckTranslator healthCheckTranslator;
    private HealthCheckManager healthCheckManager;

    @Autowired
    public GenerateTestDataController(HealthCheckTranslator healthCheckTranslator, HealthCheckManager healthCheckManager) {
        this.healthCheckTranslator = healthCheckTranslator;
        this.healthCheckManager = healthCheckManager;
    }

    @PostMapping(value = "/generate/healthchecks")
    public ResponseEntity<List<HealthCheck>> generateTestHealthChecks() throws Exception {
        List<HealthCheck> createdHealthChecks = new ArrayList<>();

        // Create 4 different test HealthChecks
        createdHealthChecks.add(createTestHealthCheck(
            "Google Health Check",
            "https://www.google.com",
            HealthCheckHttpMethod.GET,
            true,
            createHeaders("User-Agent", "HealthCheck-Monitor/1.0"),
            null
        ));

        createdHealthChecks.add(createTestHealthCheck(
            "GitHub API Health Check",
            "https://api.github.com/status",
            HealthCheckHttpMethod.GET,
            true,
            createHeaders("Accept", "application/json"),
            null
        ));

        createdHealthChecks.add(createTestHealthCheck(
            "JSONPlaceholder API Health Check",
            "https://jsonplaceholder.typicode.com/posts/1",
            HealthCheckHttpMethod.GET,
            true,
            createHeaders("Content-Type", "application/json"),
            null
        ));

        createdHealthChecks.add(createTestHealthCheck(
            "Example.com Health Check",
            "https://example.com",
            HealthCheckHttpMethod.GET,
            false,  // inactive for testing
            createHeaders("User-Agent", "HealthCheck-Monitor/1.0"),
            createParams("test", "true")
        ));

        return ResponseEntity.ok(createdHealthChecks);
    }

    private HealthCheck createTestHealthCheck(String name, String url, HealthCheckHttpMethod method,
                                              boolean active, Set<KeyValuePair> headers,
                                              Set<KeyValuePair> params) throws Exception {
        HealthCheck healthCheck = new HealthCheck();
        healthCheck.setUrl(url);
        healthCheck.setHttpMethod(method);
        healthCheck.setActive(active);
        healthCheck.setHeaders(headers);
        healthCheck.setParams(params);

        // Create the health check using the existing createHealthCheck logic
        HealthCheckEntity created = this.healthCheckManager.createHealthCheck(
            this.healthCheckTranslator.toDomainModel(healthCheck)
        );

        return this.healthCheckTranslator.toApiModel(created);
    }

    private Set<KeyValuePair> createHeaders(String key, String value) {
        Set<KeyValuePair> headers = new HashSet<>();
        KeyValuePair header = new KeyValuePair();
        header.setKey(key);
        header.setValue(value);
        headers.add(header);
        return headers;
    }

    private Set<KeyValuePair> createParams(String key, String value) {
        Set<KeyValuePair> params = new HashSet<>();
        KeyValuePair param = new KeyValuePair();
        param.setKey(key);
        param.setValue(value);
        params.add(param);
        return params;
    }
}
