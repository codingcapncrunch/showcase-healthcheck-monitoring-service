package com.lozano.showcase.healthcheck_monitoring_service.domain.service.alert;

import com.lozano.showcase.healthcheck_monitoring_service.domain.model.AlertEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<AlertEntity, String> {

    // Find all alerts for a specific health check
    List<AlertEntity> findByHealthCheckId(String healthCheckId);

    // Find all active alerts
    List<AlertEntity> findByActive(boolean active);

    // Find active alerts for a specific health check
    List<AlertEntity> findByHealthCheckIdAndActive(String healthCheckId, boolean active);
}
