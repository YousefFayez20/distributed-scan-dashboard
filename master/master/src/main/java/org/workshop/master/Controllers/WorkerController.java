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
        if(worker.isPresent() && worker.get().getWorkerStatus() == WorkerStatus.IDLE){
             //update timestamp of the worker
            workerService.updateTimestamp(worker.get().getName());
            //check if there are any pending assignments
            List<Assignment> assignments = assignmentService.getAssignmentsForWorker(worker.get().getName(), WorkerStatus.IDLE, AssignmentStatus.PENDING);
            //send the first pending assignment to the worker
            //make this assignment running

        }else{
            //create worker and add it to database
            //respond with ok 200

        }
    }

}
