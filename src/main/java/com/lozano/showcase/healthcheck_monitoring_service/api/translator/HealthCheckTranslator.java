package com.lozano.showcase.healthcheck_monitoring_service.api.translator;

import com.lozano.showcase.healthcheck_monitoring_service.api.model.HealthCheck;
import com.lozano.showcase.healthcheck_monitoring_service.api.model.HealthCheckHttpMethod;
import com.lozano.showcase.healthcheck_monitoring_service.api.model.KeyValuePair;
import com.lozano.showcase.healthcheck_monitoring_service.domain.model.HealthCheckEntity;
import com.lozano.showcase.healthcheck_monitoring_service.domain.model.HealthCheckHeaderEntity;
import com.lozano.showcase.healthcheck_monitoring_service.domain.model.HealthCheckParamEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class HealthCheckTranslator extends Translator <HealthCheck, HealthCheckEntity>{

    private HealthCheckParamTranslator healthCheckParamTranslator;
    private HealthCheckHeaderTranslator healthCheckHeaderTranslator;

    @Autowired
    public HealthCheckTranslator(HealthCheckParamTranslator healthCheckParamTranslator, HealthCheckHeaderTranslator healthCheckHeaderTranslator) {
        this.healthCheckParamTranslator = healthCheckParamTranslator;
        this.healthCheckHeaderTranslator = healthCheckHeaderTranslator;
    }

    @Override
    public HealthCheck toApiModel(HealthCheckEntity domain) {
        HealthCheck healthCheck = new HealthCheck();
        healthCheck.setId(domain.getId());
        healthCheck.setUrl(domain.getUrl());
        healthCheck.setActive(domain.isActive());
        healthCheck.setHttpMethod(HealthCheckHttpMethod.valueOf(domain.getHttpMethod()));
        healthCheck.setAlertThreshold(domain.getAlertThreshold());

        Set<KeyValuePair> keyValuePairSet = new HashSet<>();
        if (domain.getParams()!=null){
            for (HealthCheckParamEntity healthCheckParamEntity : domain.getParams()){
                keyValuePairSet.add(this.healthCheckParamTranslator.toApiModel(healthCheckParamEntity));
            }
        }
        healthCheck.setParams(keyValuePairSet);

        Set<KeyValuePair> keyValuePairSet2 = new HashSet<>();
        if (domain.getHeaders()!=null){
            for (HealthCheckHeaderEntity healthCheckHeaderEntity : domain.getHeaders()){
                keyValuePairSet2.add(this.healthCheckHeaderTranslator.toApiModel(healthCheckHeaderEntity));
            }
        }
        healthCheck.setHeaders(keyValuePairSet2);

        return healthCheck;
    }

    @Override
    public HealthCheckEntity toDomainModel(HealthCheck api) {
        HealthCheckEntity healthCheckEntity = new HealthCheckEntity();
        healthCheckEntity.setId(api.getId());
        healthCheckEntity.setUrl(api.getUrl());
        healthCheckEntity.setActive(api.isActive());
        healthCheckEntity.setHttpMethod(api.getHttpMethod().toString());
        healthCheckEntity.setAlertThreshold(api.getAlertThreshold());

        Set<HealthCheckParamEntity> healthCheckParamEntities = new HashSet<>();
        if (api.getParams()!=null){
            for (KeyValuePair keyValuePair : api.getParams()){
                healthCheckParamEntities.add(this.healthCheckParamTranslator.toDomainModel(keyValuePair));
            }
        }
        healthCheckEntity.setParams(healthCheckParamEntities);

        Set<HealthCheckHeaderEntity> healthCheckHeaderEntities = new HashSet<>();
        if (api.getHeaders()!=null){
            for (KeyValuePair keyValuePair : api.getHeaders()){
                healthCheckHeaderEntities.add(this.healthCheckHeaderTranslator.toDomainModel(keyValuePair));
            }
        }
        healthCheckEntity.setHeaders(healthCheckHeaderEntities);

        return healthCheckEntity;
    }
}
