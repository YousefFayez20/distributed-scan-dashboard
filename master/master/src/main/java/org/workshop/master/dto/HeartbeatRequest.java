package org.workshop.master.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class HeartbeatRequest {
    private String workerName;
}
