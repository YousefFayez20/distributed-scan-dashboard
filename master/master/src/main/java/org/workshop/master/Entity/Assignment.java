package org.workshop.master.Entity;

import jakarta.persistence.*;

import java.util.List;

@Entity
public class Assignment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "worker_id")
    private Worker worker;

    private String startIP;
    private String endIP;
    @Enumerated(EnumType.STRING)
    private AssignmentStatus status;
    @OneToMany(mappedBy = "assignment")
    private List<ScanResults> scanResults;


}
