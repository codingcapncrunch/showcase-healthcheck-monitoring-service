package com.lozano.showcase.healthcheck_monitoring_service.api.controller;

import com.lozano.showcase.healthcheck_monitoring_service.domain.model.AlertEntity;
import com.lozano.showcase.healthcheck_monitoring_service.domain.service.alert.AlertRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/alert")
public class AlertController {

    private AlertRepository alertRepository;

    @Autowired
    public AlertController(AlertRepository alertRepository) {
        this.alertRepository = alertRepository;
    }

    /**
     * Retrieves all alerts for a given health check ID.
     *
     * @param healthCheckId The health check ID
     * @return List of all alerts (active and inactive) for the health check
     */
    @GetMapping(value = "/healthcheck/{healthCheckId}")
    public ResponseEntity<List<AlertEntity>> getAlertsByHealthCheckId(@PathVariable String healthCheckId) {
        List<AlertEntity> alerts = this.alertRepository.findByHealthCheckId(healthCheckId);
        return ResponseEntity.ok(alerts);
    }

    /**
     * Retrieves active alerts for a given health check ID.
     *
     * @param healthCheckId The health check ID
     * @return List of active alerts for the health check
     */
    @GetMapping(value = "/healthcheck/{healthCheckId}/active")
    public ResponseEntity<List<AlertEntity>> getActiveAlertsByHealthCheckId(@PathVariable String healthCheckId) {
        List<AlertEntity> alerts = this.alertRepository.findByHealthCheckIdAndActive(healthCheckId, true);
        return ResponseEntity.ok(alerts);
    }

    /**
     * Retrieves all active alerts across all health checks.
     *
     * @return List of all active alerts
     */
    @GetMapping(value = "/active")
    public ResponseEntity<List<AlertEntity>> getAllActiveAlerts() {
        List<AlertEntity> alerts = this.alertRepository.findByActive(true);
        return ResponseEntity.ok(alerts);
    }
}
