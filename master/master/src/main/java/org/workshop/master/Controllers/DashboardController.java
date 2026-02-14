package org.workshop.master.Controllers;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.workshop.master.dto.ResultsResponse;
import org.workshop.master.dto.SelectedWorkers;
import org.workshop.master.dto.WorkerResponse;
import org.workshop.master.services.AssignmentService;
import org.workshop.master.services.ScanResultsService;
import org.workshop.master.services.WorkerService;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {
    //dashboard will be a simple frontend, which will select among the available workers and after pressing start scan the worker will
    //construct the assignments table based on the CIDR,ports, interval selected workers

    //after the workers send their heartbeats and be registered in the database
    //the dashboard will select from the available workers (their last_seen timestamp is less than 3 minutes)
    //after selecting the workers, dashboard will send the results (POST) endpoint to the worker
    //another endpoint (GET) will be for showing the scan results
    private final WorkerService workerService;
    private final AssignmentService assignmentService;
    private final ScanResultsService scanResultsService;
    @GetMapping("/workers")
    public List<WorkerResponse> getActiveWorkers(){
        return workerService.getActiveWorkers();
    }
    @PostMapping("/start")
    public ResponseEntity<?> startScan(@RequestBody SelectedWorkers selectedWorkers) {
        assignmentService.startScan(selectedWorkers);
        return  ResponseEntity.ok().build();
    }
    @GetMapping("/results")
    public List<ResultsResponse> getScanResults(){
        return scanResultsService.getAllScanResults();
    }
}
