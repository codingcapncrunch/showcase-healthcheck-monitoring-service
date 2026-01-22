package com.lozano.showcase.healthcheck_monitoring_service.api.controller;

import com.lozano.showcase.healthcheck_monitoring_service.domain.model.RunResultEntity;
import com.lozano.showcase.healthcheck_monitoring_service.domain.service.runresult.RunResultManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/runResult")
public class RunResultController {

    private RunResultManager runResultManager;

    @Autowired
    public RunResultController(RunResultManager runResultManager) {
        this.runResultManager = runResultManager;
    }

    @GetMapping
    @RequestMapping(value = "/all/{id}")
    public ResponseEntity<Page<RunResultEntity>> getRunResults(
            @PathVariable String id,
            @PageableDefault(size = 20, sort = "startDateTime", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<RunResultEntity> results = this.runResultManager.getResultsByHealthCheckId(id, pageable);
        return ResponseEntity.ok(results);
    }
}
