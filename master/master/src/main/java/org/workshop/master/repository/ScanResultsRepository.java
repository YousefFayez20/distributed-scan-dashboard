package org.workshop.master.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.workshop.master.Entity.ScanResults;

public interface ScanResultsRepository extends JpaRepository<ScanResults,Long> {
}
