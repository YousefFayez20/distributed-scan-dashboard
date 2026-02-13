package org.workshop.master.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
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
