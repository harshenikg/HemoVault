package com.hemovault.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "donors", indexes = {
        @Index(name = "idx_donor_blood_group", columnList = "blood_group"),
        @Index(name = "idx_donor_city", columnList = "city")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Donor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private Integer age;

    @Enumerated(EnumType.STRING)
    @Column(name = "blood_group", nullable = false)
    private BloodGroup bloodGroup;

    @Column(name = "contact_number", length = 20)
    private String contactNumber;

    @Column(length = 100)
    private String email;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(columnDefinition = "TEXT")
    private String address;

    private Double weight;
    private Double hemoglobin;
    private Integer bpSystolic;
    private Integer bpDiastolic;

    @Column(name = "is_pregnant")
    @Builder.Default
    private Boolean isPregnant = false;

    @Column(name = "has_recent_illness")
    @Builder.Default
    private Boolean hasRecentIllness = false;

    @Column(name = "last_donation_date")
    private LocalDate lastDonationDate;

    @Column(name = "is_eligible")
    @Builder.Default
    private Boolean isEligible = true;

    @Column(name = "eligibility_reason", columnDefinition = "TEXT")
    private String eligibilityReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
