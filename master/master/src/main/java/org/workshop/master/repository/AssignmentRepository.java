package org.workshop.master.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.workshop.master.Entity.Assignment;
import org.workshop.master.Entity.AssignmentStatus;
import org.workshop.master.Entity.WorkerStatus;

import java.util.List;
import java.util.Optional;

public interface AssignmentRepository extends JpaRepository<Assignment,Long> {
    List<Assignment> findByWorker_NameAndWorker_WorkerStatusAndStatus
            (
                    String workerName,
                    WorkerStatus workerStatus,
                    AssignmentStatus assignmentStatus
            );
}
