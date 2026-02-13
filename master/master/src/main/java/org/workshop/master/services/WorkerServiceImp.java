package org.workshop.master.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.workshop.master.Entity.Worker;
import org.workshop.master.repository.WorkerRepository;

import java.time.Instant;
import java.util.Optional;

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
    }
}
