package org.workshop.master.Controllers;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.workshop.master.Entity.*;
import org.workshop.master.ScanConfig;
import org.workshop.master.Utility.IpUtility;
import org.workshop.master.dto.AssignmentResponse;
import org.workshop.master.dto.HeartbeatRequest;
import org.workshop.master.dto.ResultsRequest;
import org.workshop.master.services.AssignmentService;
import org.workshop.master.services.ScanResultsService;
import org.workshop.master.services.WorkerService;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/worker")
@RequiredArgsConstructor
public class WorkerController {
    private final AssignmentService assignmentService;
    private final WorkerService workerService;
    private final ScanResultsService scanResultsService;

    @PostMapping("/heartbeat")
    public ResponseEntity<?> getHeartbeat(@RequestBody HeartbeatRequest heartbeatRequest) {
        // if worker exists, update timestamp and send pending asssignment if exists any
        // if doesn't exist add to workers table
        Optional<Worker> worker = workerService.getWorkerByName(heartbeatRequest.getWorkerName());
        AssignmentResponse assignmentResponse = new AssignmentResponse();

        if (worker.isPresent() /* & worker.get().getWorkerStatus() == WorkerStatus.IDLE */) {
            // update timestamp of the worker
            workerService.updateTimestamp(worker.get().getName());
            /*
            // check if there are any pending assignments
            List<Assignment> assignments = assignmentService.getAssignmentsForWorker(worker.get().getName(),
                    WorkerStatus.IDLE, AssignmentStatus.PENDING);
            // send the first pending assignment to the worker
            if (!assignments.isEmpty()) {
                // construct response body with assignment.get(0)
                assignments.get(0);

             */
            Optional<Assignment> assignmentOptional = assignmentService.getAvailableAssignment(worker.get().getName());

            if(assignmentOptional.isPresent()){
                assignmentResponse.setWorkerName(heartbeatRequest.getWorkerName());
               // assignmentResponse.setStartIp(assignments.get(0).getStartIP());
                //assignmentResponse.setEndIp(assignments.get(0).getEndIP());
                 Assignment assignment = assignmentOptional.get();
                 assignmentResponse.setStartIp(assignment.getStartIP());
                 assignmentResponse.setEndIp(assignment.getEndIP());
                assignmentResponse.setPorts(ScanConfig.PORTS);
                assignmentResponse.setInterval(ScanConfig.INTERVAL_IN_SECONDS);
                assignmentResponse.setAssignmentStatus(AssignmentStatus.RUNNING);
                assignmentResponse.setAssignmentId(assignment.getId());
                //assignmentResponse.setAssignmentId(assignments.get(0).getId());
                // make this assignment running
               // assignmentService.updateAssignmentStatus(assignments.get(0).getId(), AssignmentStatus.RUNNING);
                workerService.updateWorkerStatus(worker.get(), WorkerStatus.BUSY);

                return ResponseEntity.ok().body(assignmentResponse);
            }
            return ResponseEntity.ok("worker has no pending assignments");

        } else {
            assignmentResponse.setAssignmentStatus(AssignmentStatus.NOT_EXIST);

            Worker worker1 = new Worker();
            worker1.setWorkerStatus(WorkerStatus.IDLE);
            worker1.setName(heartbeatRequest.getWorkerName());
            worker1.setLastSeen(Instant.now());
            workerService.createNewWorker(worker1);
            // create worker and add it to database
            // respond with ok 200
            return ResponseEntity.ok("Worker added to database");
        }
    }

    @PostMapping("/results")
    public ResponseEntity<?> getResults(@RequestBody ResultsRequest resultsRequest) {
        // now we will to map the results response to Scan results
        Worker worker = workerService.getWorkerByName(resultsRequest.getWorkerName())
                .orElseThrow(() -> new EntityNotFoundException());
        List<ScanResults> entities = resultsRequest.getData().stream().map(resultItem -> {
            ScanResults scanResults = new ScanResults();
            scanResults.setWorker(worker);
            scanResults.setIp(IpUtility.ipToLong(resultItem.getIp()));
            scanResults.setPort(resultItem.getPort());
            scanResults.setScannedAt(Instant.now());
            scanResults.setAssignment(assignmentService.getAssignment(resultsRequest.getAssignmentId()));
            scanResults.setStatus(ScanStatus.valueOf(resultItem.getStatus()));
            return scanResults;
        }).collect(Collectors.toList());
        if (resultsRequest.isFinished()) {
            assignmentService.updateAssignmentStatus(resultsRequest.getAssignmentId(), AssignmentStatus.FINISHED);
            workerService.updateWorkerStatus(worker, WorkerStatus.IDLE);
        }
        scanResultsService.saveScanResults(entities);
        return ResponseEntity.ok("Results saved Successfully");
    }

}
