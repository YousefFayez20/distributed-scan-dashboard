package org.workshop.master.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.workshop.master.Entity.ScanResults;
import org.workshop.master.repository.ScanResultsRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ScanResultsImp implements ScanResultsService{
    private final ScanResultsRepository scanResultsRepository;
    @Override
    public List<ScanResults> saveScanResults(List<ScanResults> results) {

        return scanResultsRepository.saveAll(results);
    }
}
