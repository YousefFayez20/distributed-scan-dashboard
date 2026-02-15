package org.workshop.master.services;

import org.workshop.master.Entity.Worker;
import org.workshop.master.Entity.WorkerStatus;
import org.workshop.master.dto.WorkerResponse;

import java.util.List;
import java.util.Optional;

public interface WorkerService {
    public Optional<Worker> getWorkerByName(String name);
    public void updateTimestamp(String name);
    public Worker createWorker(String name);

    public List<WorkerResponse> getActiveWorkers();

    Worker createNewWorker(Worker worker1);
    Worker updateWorkerStatus(Worker worker, WorkerStatus workerStatus);
}
