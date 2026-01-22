package com.lozano.showcase.healthcheck_monitoring_service.domain.service.runresult;

import com.lozano.showcase.healthcheck_monitoring_service.domain.model.RunResultEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface RunResultRepository extends JpaRepository<RunResultEntity, String> {

    // Paginated version using Spring Data JPA method naming convention
    Page<RunResultEntity> findAllByHealthCheckId(String healthCheckId, Pageable pageable);
}
