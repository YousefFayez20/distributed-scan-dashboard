package org.workshop.master.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ResultsResponse {
    private String workerName;
    private List<ResultItem> data;
    private boolean isFinished;
}
