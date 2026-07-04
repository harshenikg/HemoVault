package com.hemovault.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "shortage_thresholds")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ShortageThreshold {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "blood_group", nullable = false, unique = true)
    private BloodGroup bloodGroup;

    @Column(name = "threshold_units", nullable = false)
    @Builder.Default
    private Double thresholdUnits = 10.0;

    @Column(name = "critical_units", nullable = false)
    @Builder.Default
    private Double criticalUnits = 5.0;
}
