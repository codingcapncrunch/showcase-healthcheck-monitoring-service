package com.lozano.showcase.healthcheck_monitoring_service.domain.service.alert;

import com.lozano.showcase.healthcheck_monitoring_service.domain.model.AlertEntity;
import com.lozano.showcase.healthcheck_monitoring_service.domain.model.HealthCheckRunResponse;
import com.lozano.showcase.healthcheck_monitoring_service.domain.model.RunResultHealth;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Log4j2
@Service
public class AlertManagerImpl implements AlertManager {

    // In-memory cache: Key = healthCheckId, Value = consecutive unhealthy count
    private final ConcurrentHashMap<String, AtomicInteger> unhealthyCountCache;

    private final AlertRepository alertRepository;

    @Autowired
    public AlertManagerImpl(AlertRepository alertRepository) {
        this.alertRepository = alertRepository;
        this.unhealthyCountCache = new ConcurrentHashMap<>();
    }

    @Override
    public void notifyLatestRunResult(HealthCheckRunResponse response) {
        if (response == null || response.getHealthCheckId() == null) {
            log.warn("AlertManagerImpl - Received null response or healthCheckId, skipping");
            return;
        }

        String healthCheckId = response.getHealthCheckId();
        RunResultHealth health = response.getHealth();
        Integer alertThreshold = response.getAlertThreshold();

        // Skip processing if no alert threshold is configured
        if (alertThreshold == null || alertThreshold <= 0) {
            log.debug("AlertManagerImpl - HealthCheck '{}' has no alert threshold configured, skipping", healthCheckId);
            return;
        }

        if (health == RunResultHealth.UNHEALTHY) {
            // Increment the consecutive unhealthy count (capped at threshold)
            int newCount = incrementUnhealthyCount(healthCheckId, alertThreshold);
            log.info("AlertManagerImpl - HealthCheck '{}' is UNHEALTHY. Consecutive failures: {}/{}",
                     healthCheckId, newCount, alertThreshold);

            // Check if threshold is exceeded
            if (newCount >= alertThreshold) {
                // Check if there's already an active alert for this health check
                List<AlertEntity> activeAlerts = alertRepository.findByHealthCheckIdAndActive(healthCheckId, true);

                if (activeAlerts.isEmpty()) {
                    // Create a new alert only if no active alert exists
                    createAlert(healthCheckId);
                    log.warn("AlertManagerImpl - ALERT CREATED for HealthCheck '{}'. Threshold exceeded: {}/{}",
                             healthCheckId, newCount, alertThreshold);
                } else {
                    log.debug("AlertManagerImpl - Active alert already exists for HealthCheck '{}', skipping creation",
                              healthCheckId);
                }
            }

        } else if (health == RunResultHealth.HEALTHY) {
            // Decrement the count by one since it's now healthy
            int currentCount = getUnhealthyCount(healthCheckId);

            if (currentCount > 0) {
                int newCount = decrementUnhealthyCount(healthCheckId);
                log.info("AlertManagerImpl - HealthCheck '{}' is HEALTHY. Decremented failure count from {} to {}",
                         healthCheckId, currentCount, newCount);

                // Only close alerts when count reaches zero
                if (newCount == 0) {
                    log.info("AlertManagerImpl - HealthCheck '{}' failure count reached zero, checking for active alerts to close",
                             healthCheckId);
                    closeActiveAlerts(healthCheckId);
                }
            } else {
                log.debug("AlertManagerImpl - HealthCheck '{}' is HEALTHY. No unhealthy count to decrement",
                          healthCheckId);
            }

        } else {
            // UNKNOWN state - log but don't change count
            log.warn("AlertManagerImpl - HealthCheck '{}' has UNKNOWN health status", healthCheckId);
        }
    }

    /**
     * Increments the consecutive unhealthy count for a health check.
     * The count will never exceed the alertThreshold.
     *
     * @param healthCheckId The health check ID
     * @param alertThreshold The maximum value the count should reach
     * @return The new count after incrementing (capped at alertThreshold)
     */
    private int incrementUnhealthyCount(String healthCheckId, int alertThreshold) {
        AtomicInteger count = unhealthyCountCache.computeIfAbsent(
            healthCheckId,
            k -> new AtomicInteger(0)
        );

        // Increment only if below threshold, otherwise keep at threshold
        int currentValue = count.get();
        if (currentValue < alertThreshold) {
            return count.incrementAndGet();
        }
        return currentValue; // Already at threshold, don't increment
    }

    /**
     * Decrements the consecutive unhealthy count for a health check by one.
     * Removes the entry from cache if count reaches zero.
     *
     * @param healthCheckId The health check ID
     * @return The new count after decrementing (0 if removed from cache)
     */
    private int decrementUnhealthyCount(String healthCheckId) {
        AtomicInteger count = unhealthyCountCache.get(healthCheckId);
        if (count != null) {
            int newCount = count.decrementAndGet();
            if (newCount <= 0) {
                // Remove from cache when count reaches zero or below
                unhealthyCountCache.remove(healthCheckId);
                return 0;
            }
            return newCount;
        }
        return 0;
    }

    /**
     * Resets the consecutive unhealthy count for a health check.
     *
     * @param healthCheckId The health check ID
     */
    private void resetUnhealthyCount(String healthCheckId) {
        unhealthyCountCache.remove(healthCheckId);
    }

    /**
     * Gets the current consecutive unhealthy count for a health check.
     *
     * @param healthCheckId The health check ID
     * @return The current count, or 0 if not in cache
     */
    private int getUnhealthyCount(String healthCheckId) {
        AtomicInteger count = unhealthyCountCache.get(healthCheckId);
        return count != null ? count.get() : 0;
    }

    /**
     * Creates a new alert entity for the given health check.
     *
     * @param healthCheckId The health check ID that triggered the alert
     */
    private void createAlert(String healthCheckId) {
        AlertEntity alert = new AlertEntity();
        alert.setHealthCheckId(healthCheckId);
        alert.setCreatedDateTime(LocalDateTime.now());
        alert.setActive(true);
        alert.setEndDateTime(null); // No end date yet, alert is active

        alertRepository.save(alert);
        log.info("AlertManagerImpl - Alert created and saved for HealthCheck '{}'", healthCheckId);
    }

    /**
     * Closes all active alerts for the given health check.
     *
     * @param healthCheckId The health check ID
     */
    private void closeActiveAlerts(String healthCheckId) {
        List<AlertEntity> activeAlerts = alertRepository.findByHealthCheckIdAndActive(healthCheckId, true);

        if (!activeAlerts.isEmpty()) {
            LocalDateTime now = LocalDateTime.now();
            for (AlertEntity alert : activeAlerts) {
                alert.setActive(false);
                alert.setEndDateTime(now);
                alertRepository.save(alert);
                log.info("AlertManagerImpl - Alert closed for HealthCheck '{}'. Alert ID: {}",
                         healthCheckId, alert.getId());
            }
        }
    }
}
