package org.workshop.master.services;

import org.workshop.master.Entity.ScanResults;

import java.util.List;

public interface ScanResultsService {
    public List<ScanResults> saveScanResults(List<ScanResults> results);
}
