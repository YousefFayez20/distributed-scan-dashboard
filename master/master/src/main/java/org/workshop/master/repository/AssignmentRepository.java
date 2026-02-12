package org.workshop.master.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.workshop.master.Entity.Assignment;

import java.util.List;
import java.util.Optional;

public interface AssignmentRepository extends JpaRepository<Assignment,Long> {
}
