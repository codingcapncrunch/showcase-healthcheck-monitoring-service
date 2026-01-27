package com.lozano.showcase.healthcheck_monitoring_service.domain.service.alert;

import com.lozano.showcase.healthcheck_monitoring_service.domain.model.HealthCheckRunResponse;

public interface AlertManager {

    void notifyLatestRunResult(HealthCheckRunResponse response);

}
