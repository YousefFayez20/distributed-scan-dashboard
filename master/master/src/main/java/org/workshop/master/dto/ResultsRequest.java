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
public class ResultsRequest {
    private String workerName;
    private List<ResultItem> data;
    private boolean isFinished;
    private Long assignmentId;
}
