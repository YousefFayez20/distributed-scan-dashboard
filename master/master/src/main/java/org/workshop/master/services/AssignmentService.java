package org.workshop.master.services;

import org.workshop.master.Entity.Assignment;
import org.workshop.master.Entity.AssignmentStatus;
import org.workshop.master.Entity.WorkerStatus;

import java.util.List;
import java.util.Optional;

public interface AssignmentService {
    public List<Assignment> getAssignmentsForWorker(String workerName, WorkerStatus workerStatus, AssignmentStatus assignmentStatus);
}
