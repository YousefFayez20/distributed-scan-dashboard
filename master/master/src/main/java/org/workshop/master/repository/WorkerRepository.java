package org.workshop.master.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.workshop.master.Entity.Worker;

import java.util.Optional;

public interface WorkerRepository extends JpaRepository<Worker,Long> {
    Optional<Worker> findByName(String name);
}
