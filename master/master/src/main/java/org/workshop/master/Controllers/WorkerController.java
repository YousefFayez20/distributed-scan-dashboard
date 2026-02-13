package org.workshop.master.Controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.workshop.master.Entity.Assignment;
import org.workshop.master.Entity.AssignmentStatus;
import org.workshop.master.Entity.Worker;
import org.workshop.master.Entity.WorkerStatus;
import org.workshop.master.ScanConfig;
import org.workshop.master.dto.AssignmentResponse;
import org.workshop.master.dto.HeartbeatRequest;
import org.workshop.master.services.AssignmentService;
import org.workshop.master.services.WorkerService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/worker")
@RequiredArgsConstructor
public class WorkerController {
    private final AssignmentService assignmentService;
    private final WorkerService workerService;
    @GetMapping("/heartbeat")
    public ResponseEntity<?> getHeartbeat(@RequestBody HeartbeatRequest heartbeatRequest){
        //if worker exists, update timestamp and send pending asssignment if exists any
        //if doesn't exist add to workers table
        Optional<Worker> worker = workerService.getWorkerByName(heartbeatRequest.getWorkerName());
        AssignmentResponse assignmentResponse = new AssignmentResponse();

        if(worker.isPresent() /*& worker.get().getWorkerStatus() == WorkerStatus.IDLE*/){
             //update timestamp of the worker
            workerService.updateTimestamp(worker.get().getName());
            //check if there are any pending assignments
            List<Assignment> assignments = assignmentService.getAssignmentsForWorker(worker.get().getName(), WorkerStatus.IDLE, AssignmentStatus.PENDING);
            //send the first pending assignment to the worker
            if(!assignments.isEmpty()){
                //construct response body with assignment.get(0)
                assignments.get(0);
                assignmentResponse.setWorkerName(heartbeatRequest.getWorkerName());
                assignmentResponse.setStartIp(assignments.get(0).getStartIP());
                assignmentResponse.setEndIp(assignments.get(0).getEndIP());
                assignmentResponse.setPorts(ScanConfig.PORTS);
                assignmentResponse.setInterval(ScanConfig.INTERVAL_IN_SECONDS);
                assignmentResponse.setAssignmentStatus(AssignmentStatus.RUNNING);
                //make this assignment running
                assignmentService.updateAssignmentStatus(assignments.get(0).getId(),AssignmentStatus.RUNNING);
                return ResponseEntity.ok().body(assignmentResponse);
            }
            return ResponseEntity.ok().build();

        }else{
            assignmentResponse.setAssignmentStatus(AssignmentStatus.NOT_EXIST);
            workerService.createWorker(heartbeatRequest.getWorkerName());
            //create worker and add it to database
            //respond with ok 200
            return ResponseEntity.ok().build();
        }
    }

}
