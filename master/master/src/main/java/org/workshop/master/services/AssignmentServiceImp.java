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
public class AssignmentServiceImp implements AssignmentService{
    private final AssignmentRepository assignmentRepository;
    private final WorkerRepository workerRepository;

    @Override
    public List<Assignment> getAssignmentsForWorker(String workerName, WorkerStatus workerStatus, AssignmentStatus assignmentStatus) {
        List<Assignment> assignments = assignmentRepository.findByWorker_NameAndWorker_WorkerStatusAndStatus(workerName,workerStatus,assignmentStatus);

        return assignments;
    }

    @Override
    public void updateAssignmentStatus(Long id, AssignmentStatus assignmentStatus) {
        Optional<Assignment> assignment = assignmentRepository.findById(id);
        if(assignment.isPresent()){
            assignment.get().setStatus(assignmentStatus);
            assignmentRepository.save(assignment.get());
        }
    }

    @Override
    public void startScan(SelectedWorkers selectedWorkers) {
        List<String>workerNames = selectedWorkers.getWorkerNames();
        long [] ips = IpUtility.cidrToRange(ScanConfig.CIDR);
        long startIP = ips[0];
        long endIP = ips[1];
        long chunkSize = (long)Math.ceil((endIP - startIP +1)/workerNames.size());
        int i =0;
        for(String worker : workerNames){
            Assignment assignment = new Assignment();
            assignment.setWorker(workerRepository.findByName(worker)
                    .orElseThrow(()-> new EntityNotFoundException("No worker with this name")));
            assignment.setStatus(AssignmentStatus.PENDING);
            assignment.setStartIP(IpUtility.longToIp(startIP + i*chunkSize));
            assignment.setEndIP(IpUtility.longToIp(Math.min(endIP,startIP + i*chunkSize*2)));
            assignmentRepository.save(assignment);
            i++;
        }
    }
}
