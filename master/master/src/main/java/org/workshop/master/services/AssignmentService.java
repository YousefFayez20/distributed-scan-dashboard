package org.workshop.master.services;

import org.workshop.master.Entity.Assignment;
import org.workshop.master.Entity.AssignmentStatus;
import org.workshop.master.Entity.WorkerStatus;
import org.workshop.master.dto.SelectedWorkers;

import java.util.List;
import java.util.Optional;

public interface AssignmentService {
    public List<Assignment> getAssignmentsForWorker(String workerName, WorkerStatus workerStatus, AssignmentStatus assignmentStatus);
    public void updateAssignmentStatus(Long id,AssignmentStatus assignmentStatus);

    void startScan(SelectedWorkers selectedWorkers);
}
