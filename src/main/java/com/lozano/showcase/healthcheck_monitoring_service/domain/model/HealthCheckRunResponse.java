package com.lozano.showcase.healthcheck_monitoring_service.domain.model;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class HealthCheckRunResponse {

    private static final int MAX_RESPONSE_BODY_LENGTH = 255;

    private String healthCheckId;
    private String url;
    private Integer httpStatusCode;
    private String responseBody;
    private String errorMessage;
    private LocalDateTime startDateTime;
    private long durationInMillis;
    private RunResultHealth health;

    public HealthCheckRunResponse(String healthCheckId, String url, Integer httpStatusCode, String responseBody,
                                  String errorMessage, LocalDateTime startDateTime, long durationInMillis,
                                  RunResultHealth health) {
        this.healthCheckId = healthCheckId;
        this.url = url;
        this.httpStatusCode = httpStatusCode;
        this.setResponseBody(responseBody);  // Use setter to apply truncation
        this.errorMessage = errorMessage;
        this.startDateTime = startDateTime;
        this.durationInMillis = durationInMillis;
        this.health = health;
    }

    public void setHealthCheckId(String healthCheckId) {
        this.healthCheckId = healthCheckId;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setHttpStatusCode(Integer httpStatusCode) {
        this.httpStatusCode = httpStatusCode;
    }

    public void setResponseBody(String responseBody) {
        if (responseBody != null && responseBody.length() > MAX_RESPONSE_BODY_LENGTH) {
            this.responseBody = responseBody.substring(0, MAX_RESPONSE_BODY_LENGTH);
        } else {
            this.responseBody = responseBody;
        }
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public void setStartDateTime(LocalDateTime startDateTime) {
        this.startDateTime = startDateTime;
    }

    public void setDurationInMillis(long durationInMillis) {
        this.durationInMillis = durationInMillis;
    }

    public void setHealth(RunResultHealth health) {
        this.health = health;
    }
}
