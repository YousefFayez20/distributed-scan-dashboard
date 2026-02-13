package org.workshop.master.services;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.workshop.master.Entity.Assignment;
import org.workshop.master.Entity.AssignmentStatus;
import org.workshop.master.Entity.WorkerStatus;
import org.workshop.master.repository.AssignmentRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AssignmentServiceImp implements AssignmentService{
    private final AssignmentRepository assignmentRepository;

    @Override
    public List<Assignment> getAssignmentsForWorker(String workerName, WorkerStatus workerStatus, AssignmentStatus assignmentStatus) {
        List<Assignment> assignments = assignmentRepository.findByWorker_NameAndWorker_WorkerStatusAndStatus(workerName,workerStatus,assignmentStatus);

        return assignments;
    }
}
