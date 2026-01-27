package com.lozano.showcase.healthcheck_monitoring_service.domain.service.client;

import com.lozano.showcase.healthcheck_monitoring_service.domain.model.HealthCheckEntity;
import com.lozano.showcase.healthcheck_monitoring_service.domain.model.HealthCheckHeaderEntity;
import com.lozano.showcase.healthcheck_monitoring_service.domain.model.HealthCheckRunResponse;
import com.lozano.showcase.healthcheck_monitoring_service.domain.service.runresult.RunResultManager;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

@Log4j2
@Component
@ConditionalOnProperty(name = "healthcheckclienttype", havingValue = "rest")
public class HealthCheckRestTemplateClient implements HealthCheckClient {

    private RunResultManager runResultManager;

    private RestTemplate restTemplate;

    @Autowired
    public HealthCheckRestTemplateClient(RunResultManager runResultManager) {
        this.runResultManager = runResultManager;
        this.restTemplate = new RestTemplate();
    }

    @Override
    public HealthCheckRunResponse executeHttpRequestAndGetResponse(HealthCheckEntity healthCheckEntity) {

        Exception exception = null;
        Integer httpStatusCode = null;
        ResponseEntity<String> response = null;
        UriComponentsBuilder builder = this.buildUriWithParams(healthCheckEntity);
        HttpEntity<String> requestEntity = new HttpEntity<>(this.createHeaders(healthCheckEntity));
        LocalDateTime startDateTime = LocalDateTime.now();

        try {
            response = this.restTemplate.exchange(builder.toUriString(), HttpMethod.valueOf(healthCheckEntity.getHttpMethod()), requestEntity, String.class);

        } catch (HttpClientErrorException ex){
            exception = ex;
            httpStatusCode = ex.getStatusCode().value();  // Extract actual HTTP status code (4xx, 5xx)
            log.debug("HttpClientErrorException for HC ID: '{}' - status: {} - ex message: '{}'", healthCheckEntity.getId(), httpStatusCode, ex.getMessage());

        } catch (Exception e){
            exception = e;
            httpStatusCode = determineStatusCodeFromException(e);  // Set pseudo status code for network errors
            log.debug("Exception for HC ID: '{}' - pseudo status: {} - e message: '{}'", healthCheckEntity.getId(), httpStatusCode, e.getMessage());

        }
        if (exception!=null){
            return new HealthCheckRunResponse(healthCheckEntity.getId(), builder.toUriString(), httpStatusCode, null, exception.getMessage(), startDateTime, this.calculateDuration(startDateTime), null, healthCheckEntity.getAlertThreshold());
        } else {
            return new HealthCheckRunResponse(healthCheckEntity.getId(), builder.toUriString(), response.getStatusCode().value(), response.getBody(), null, startDateTime, this.calculateDuration(startDateTime), null, healthCheckEntity.getAlertThreshold());
        }

    }

    private UriComponentsBuilder buildUriWithParams(HealthCheckEntity healthCheckEntity){
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(healthCheckEntity.getUrl());

        if (healthCheckEntity.getParams()!=null && !healthCheckEntity.getParams().isEmpty()) {
            for (Map.Entry<String, String> entry : healthCheckEntity.convertParamEntityToURLParams().entrySet()) {
                builder.queryParam(entry.getKey(), entry.getValue());
            }
        }
        return builder;
    }

    private HttpHeaders createHeaders(HealthCheckEntity healthCheckEntity){
        HttpHeaders headers = new HttpHeaders();
        if (healthCheckEntity.getHeaders()!=null && !healthCheckEntity.getHeaders().isEmpty()){
            for (HealthCheckHeaderEntity healthCheckHeaderEntity : healthCheckEntity.getHeaders()){
                headers.set(healthCheckHeaderEntity.getHeaderName(), healthCheckHeaderEntity.getHeaderValue());
            }
        }
        return headers;
    }

    private long calculateDuration(LocalDateTime startDateTime){
        LocalDateTime endDateTime = LocalDateTime.now();
        return ChronoUnit.NANOS.between(startDateTime, endDateTime);
    }

    /**
     * Determines a pseudo HTTP status code for network-level exceptions that occur before HTTP communication.
     * These codes help categorize different types of failures for monitoring purposes.
     *
     * @param exception The exception that occurred
     * @return A pseudo HTTP status code (0 for DNS failures, 599 for timeouts, 598 for connection refused, 597 for other network errors)
     */
    private Integer determineStatusCodeFromException(Exception exception) {
        String exceptionType = exception.getClass().getSimpleName();
        String message = exception.getMessage() != null ? exception.getMessage().toLowerCase() : "";

        // DNS resolution failure (e.g., UnknownHostException)
        if (exceptionType.contains("UnknownHost") || message.contains("unknown host") || message.contains("nodename nor servname provided")) {
            return 0;  // 0 indicates DNS resolution failure
        }

        // Connection timeout
        if (exceptionType.contains("Timeout") || message.contains("timed out") || message.contains("timeout")) {
            return 599;  // 599 Network Timeout (non-standard but commonly used)
        }

        // Connection refused
        if (message.contains("connection refused") || message.contains("connect timed out")) {
            return 598;  // 598 Network Connection Refused (custom code)
        }

        // SSL/TLS errors
        if (exceptionType.contains("SSL") || exceptionType.contains("Certificate") || message.contains("ssl") || message.contains("certificate")) {
            return 495;  // 495 SSL Certificate Error (nginx convention)
        }

        // Generic network error
        return 597;  // 597 Generic Network Error (custom code)
    }
}
