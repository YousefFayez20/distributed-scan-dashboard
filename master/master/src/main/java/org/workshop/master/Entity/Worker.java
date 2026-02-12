package org.workshop.master.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Worker {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private Instant lastSeen;

    @Enumerated(EnumType.STRING)
    private WorkerStatus workerStatus;
    @OneToMany(mappedBy = "worker")
    private List<Assignment> assignments;
}
