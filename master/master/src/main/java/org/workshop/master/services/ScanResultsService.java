package org.workshop.master.services;

import org.workshop.master.Entity.ScanResults;
import org.workshop.master.dto.ResultsResponse;

import java.util.List;

public interface ScanResultsService {
    public List<ScanResults> saveScanResults(List<ScanResults> results);
    public List<ResultsResponse> getAllScanResults();
}
