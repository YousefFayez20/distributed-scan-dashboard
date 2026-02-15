package org.workshop.master.services;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.workshop.master.Entity.Assignment;
import org.workshop.master.Entity.AssignmentStatus;
import org.workshop.master.Entity.WorkerStatus;
import org.workshop.master.ScanConfig;
import org.workshop.master.Utility.IpUtility;
import org.workshop.master.dto.SelectedWorkers;
import org.workshop.master.repository.AssignmentRepository;
import org.workshop.master.repository.WorkerRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AssignmentServiceImp implements AssignmentService {
    private final AssignmentRepository assignmentRepository;
    private final WorkerRepository workerRepository;

    @Override
    public List<Assignment> getAssignmentsForWorker(String workerName, WorkerStatus workerStatus,
            AssignmentStatus assignmentStatus) {
        List<Assignment> assignments = assignmentRepository.findByWorker_NameAndWorker_WorkerStatusAndStatus(workerName,
                workerStatus, assignmentStatus);

        return assignments;
    }

    @Override
    public void updateAssignmentStatus(Long id, AssignmentStatus assignmentStatus) {
        Optional<Assignment> assignment = assignmentRepository.findById(id);
        if (assignment.isPresent()) {
            assignment.get().setStatus(assignmentStatus);
            assignmentRepository.save(assignment.get());
        }
    }

    @Override
    public void startScan(SelectedWorkers selectedWorkers) {
        if (assignmentRepository.existsByStatusIn(List.of(AssignmentStatus.PENDING, AssignmentStatus.RUNNING))) {
            return;
        }
        List<String> workerNames = selectedWorkers.getWorkerNames();
        long[] ips = IpUtility.cidrToRange(ScanConfig.CIDR);
        long startIP = ips[0];
        long endIP = ips[1];
        long chunkSize = 16;
        /*
         * // long chunkSize = (long) Math.ceil((endIP - startIP + 1) /
         * workerNames.size());
         * int i = 0;
         * for (String worker : workerNames) {
         * Assignment assignment = new Assignment();
         * assignment.setWorker(workerRepository.findByName(worker)
         * .orElseThrow(() -> new EntityNotFoundException("No worker with this name")));
         * assignment.setStatus(AssignmentStatus.PENDING);
         * assignment.setStartIP(IpUtility.longToIp(startIP + i * chunkSize));
         * assignment.setEndIP(IpUtility.longToIp(Math.min(endIP, startIP + i *
         * chunkSize + chunkSize - 1)));
         * assignmentRepository.save(assignment);
         * i++;
         * }
         */
        long current = startIP;

        while (current <= endIP) {
            Assignment assignment = new Assignment();
            assignment.setWorker(null);
            assignment.setStatus(AssignmentStatus.PENDING);
            assignment.setStartIP(IpUtility.longToIp(current));
            long endOfChunk = Math.min(endIP, current + chunkSize - 1);
            assignment.setEndIP(IpUtility.longToIp(endOfChunk));
            assignmentRepository.save(assignment);
            current += chunkSize;

        }
    }

    @Override
    public Assignment getAssignment(Long assignmentId) {
        return assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new EntityNotFoundException("No assignment found"));
    }

    @Override
    public Optional<Assignment> getAvailableAssignment(String workerName) {
        Optional<Assignment> assignment = assignmentRepository
                .findFirstByWorkerIsNullAndStatus(AssignmentStatus.PENDING);
        if (assignment.isPresent()) {
            Assignment assignment1 = assignment.get();
            assignment1.setWorker(workerRepository.findByName(workerName)
                    .orElseThrow(() -> new EntityNotFoundException("Worker doesn't exist")));
            assignment1.setStatus(AssignmentStatus.RUNNING);
            return Optional.of(assignmentRepository.save(assignment1));
        }
        return Optional.empty();
    }
}
