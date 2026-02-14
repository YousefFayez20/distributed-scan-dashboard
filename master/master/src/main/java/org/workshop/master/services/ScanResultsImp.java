package org.workshop.master.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.workshop.master.Entity.ScanResults;
import org.workshop.master.Utility.IpUtility;
import org.workshop.master.dto.ResultItem;
import org.workshop.master.dto.ResultsResponse;
import org.workshop.master.repository.ScanResultsRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScanResultsImp implements ScanResultsService{
    private final ScanResultsRepository scanResultsRepository;
    @Override
    public List<ScanResults> saveScanResults(List<ScanResults> results) {

        return scanResultsRepository.saveAll(results);
    }

    @Override
    public List<ResultsResponse> getAllScanResults() {

        return scanResultsRepository.findAll().stream().map(
                scanResults -> {
                    ResultsResponse response = new ResultsResponse();
                    response.setWorkerName(scanResults.getWorker().getName());
                    response.setIp(IpUtility.longToIp(scanResults.getIp()));
                    response.setPort(scanResults.getPort());
                    response.setStatus(scanResults.getStatus().toString());
                    return response;
                }
        ).collect(Collectors.toList());
    }
}
