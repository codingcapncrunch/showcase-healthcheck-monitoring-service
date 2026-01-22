package com.lozano.showcase.healthcheck_monitoring_service.domain.service.healthcheck;

import com.lozano.showcase.healthcheck_monitoring_service.domain.model.HealthCheckEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HealthCheckRepository extends JpaRepository<HealthCheckEntity, String> {

    @Query(value = "SELECT * FROM HEALTHCHECK hc WHERE hc.active = ?1", nativeQuery = true)
    List<HealthCheckEntity> findByActiveTrue(boolean isActive);
}
