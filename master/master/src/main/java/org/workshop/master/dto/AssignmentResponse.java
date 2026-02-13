package org.workshop.master.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.workshop.master.Entity.AssignmentStatus;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AssignmentResponse {
    private String workerName;
    private String startIp;
    private String endIp;
    private int[] ports;
    private int interval;
    private AssignmentStatus assignmentStatus;

}
