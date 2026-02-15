package org.workshop.master.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.workshop.master.Entity.Worker;
import org.workshop.master.Entity.WorkerStatus;
import org.workshop.master.dto.WorkerResponse;
import org.workshop.master.repository.WorkerRepository;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkerServiceImp implements WorkerService{
    private final WorkerRepository workerRepository;
    @Override
    public Optional<Worker> getWorkerByName(String name) {
        return workerRepository.findByName(name);
    }

    @Override
    public void updateTimestamp(String name) {
        Optional<Worker> worker = getWorkerByName(name);
        worker.get().setLastSeen(Instant.now());
        workerRepository.save(worker.get());
    }

    @Override
    public Worker createWorker(String name) {
        Worker worker = new Worker();
        worker.setName(name);
        worker.setLastSeen(Instant.now());
        return workerRepository.save(worker);
    }

    @Override
    public List<WorkerResponse> getActiveWorkers() {
        List<WorkerResponse> workerResponseList = workerRepository.findAll().stream()
                .map(worker -> {
                    WorkerResponse workerResponse = new WorkerResponse();
                    workerResponse.setWorkerStatus(worker.getWorkerStatus());
                    workerResponse.setId(worker.getId());
                    workerResponse.setLastSeen(worker.getLastSeen());
                    workerResponse.setName(worker.getName());
                    return workerResponse;
                }).filter(workerResponse -> {
                    return (Duration.between(workerResponse.getLastSeen(),Instant.now()).toMinutes()) < 4;
                }
                ).collect(Collectors.toList());
        return workerResponseList;
    }

    @Override
    public Worker createNewWorker(Worker worker1) {
        return workerRepository.save(worker1);
    }

    @Override
    public Worker updateWorkerStatus(Worker worker, WorkerStatus workerStatus) {
        worker.setWorkerStatus(workerStatus);
        return workerRepository.save(worker);
    }


}
