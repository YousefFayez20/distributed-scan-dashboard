package org.workshop.master.services;

import org.workshop.master.Entity.Worker;

import java.util.Optional;

public interface WorkerService {
    public Optional<Worker> getWorkerByName(String name);
    public void updateTimestamp(String name);
    public Worker createWorker(String name);

}
