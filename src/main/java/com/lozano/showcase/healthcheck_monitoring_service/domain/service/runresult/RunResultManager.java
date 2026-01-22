package com.lozano.showcase.healthcheck_monitoring_service.domain.service.runresult;

import com.lozano.showcase.healthcheck_monitoring_service.domain.model.HealthCheckRunResponse;
import com.lozano.showcase.healthcheck_monitoring_service.domain.model.RunResultEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RunResultManager {

    void logRunResult(HealthCheckRunResponse healthCheckRunResponse);

    // Paginated version
    Page<RunResultEntity> getResultsByHealthCheckId(String healthCheckId, Pageable pageable);

}
