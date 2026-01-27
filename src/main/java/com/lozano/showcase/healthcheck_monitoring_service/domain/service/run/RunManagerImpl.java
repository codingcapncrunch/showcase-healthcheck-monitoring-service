package com.lozano.showcase.healthcheck_monitoring_service.domain.service.run;

import com.lozano.showcase.healthcheck_monitoring_service.api.model.RunStateEnum;
import com.lozano.showcase.healthcheck_monitoring_service.domain.model.HealthCheckEntity;
import com.lozano.showcase.healthcheck_monitoring_service.domain.model.HealthCheckRunResponse;
import com.lozano.showcase.healthcheck_monitoring_service.domain.model.RunResultHealth;
import com.lozano.showcase.healthcheck_monitoring_service.domain.service.alert.AlertManager;
import com.lozano.showcase.healthcheck_monitoring_service.domain.service.client.HealthCheckClient;
import com.lozano.showcase.healthcheck_monitoring_service.domain.service.healthcheck.HealthCheckManager;
import com.lozano.showcase.healthcheck_monitoring_service.domain.service.runresult.RunResultManager;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Log4j2
@Service
public class RunManagerImpl implements RunManager{

    private HealthCheckClient healthCheckClient;
    private HealthCheckManager healthCheckManager;
    private RunStateEnum runState;
    private RunResultManager runResultManager;
    private AlertManager alertManager;

    @Autowired
    public RunManagerImpl(HealthCheckClient healthCheckClient, HealthCheckManager healthCheckManager, RunResultManager runResultManager, AlertManager alertManager) {
        this.healthCheckClient = healthCheckClient;
        this.healthCheckManager = healthCheckManager;
        this.runResultManager = runResultManager;
        this.alertManager = alertManager;
        this.runState = RunStateEnum.RUNNING;
    }


    @Override
    public boolean stopRunManager() {
        this.runState = RunStateEnum.STOPPED;
        return true;
    }

    @Override
    public boolean startRunManager() {
        this.runState = RunStateEnum.RUNNING;
        return true;
    }

    @Override
    public RunStateEnum getRunState() {
        return this.runState;
    }

    //10 seconds
    @Scheduled(fixedRateString = "${run.fixedRateInMillis}")
    private void run(){

        if (this.runState.equals(RunStateEnum.RUNNING)){
            log.info("Run manager run started...");
            for (HealthCheckEntity healthCheck : this.healthCheckManager.getAllHealthCheckByStatus(true)){
                log.info("Simulating run for HealthCheck ID {}", healthCheck.getId());

                HealthCheckRunResponse runResponse = this.healthCheckClient.executeHttpRequestAndGetResponse(healthCheck);
                runResponse.setHealth(this.determineRunResultHealth(runResponse));
                this.runResultManager.logRunResult(runResponse);
                this.alertManager.notifyLatestRunResult(runResponse);

            }
        } else {
            log.warn("Run manager at stopped state - not running active HealthChecks.");
        }
    }

    private RunResultHealth determineRunResultHealth(HealthCheckRunResponse runResponse){
        if (runResponse.getHttpStatusCode()>=200 && runResponse.getHttpStatusCode()<300){
            return RunResultHealth.HEALTHY;
        } else {
            return RunResultHealth.UNHEALTHY;
        }
    }
}
