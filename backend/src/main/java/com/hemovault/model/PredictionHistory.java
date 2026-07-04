package com.hemovault.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "prediction_history")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PredictionHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "blood_group", nullable = false)
    private BloodGroup bloodGroup;

    @Column(name = "prediction_date", nullable = false)
    private LocalDate predictionDate;

    @Column(name = "predicted_units", nullable = false)
    private Double predictedUnits;

    @Column(name = "actual_units")
    private Double actualUnits;

    @Column(name = "prediction_method", length = 50)
    @Builder.Default
    private String predictionMethod = "MOVING_AVERAGE";

    @Column(name = "window_days")
    @Builder.Default
    private Integer windowDays = 30;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
