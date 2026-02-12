package org.workshop.master.Controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.workshop.master.dto.HeartbeatRequest;
import org.workshop.master.services.AssignmentService;

@RestController
@RequestMapping("/api/worker")
@RequiredArgsConstructor
public class WorkerController {
    private final AssignmentService assignmentService;
    @GetMapping("/heartbeat")
    public ResponseEntity<?> getHeartbeat(@RequestBody HeartbeatRequest heartbeatRequest){
        if(){

        }else{

        }
    }

}
