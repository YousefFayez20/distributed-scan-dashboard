package org.workshop.master.dto;


import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.workshop.master.Entity.WorkerStatus;

import java.time.Instant;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class WorkerResponse {
    private Long id;
    private String name;
    private Instant lastSeen;
    private WorkerStatus workerStatus;
}
