package com.hemovault.repository;

import com.hemovault.model.BloodGroup;
import com.hemovault.model.ShortageThreshold;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShortageThresholdRepository extends JpaRepository<ShortageThreshold, Long> {
    Optional<ShortageThreshold> findByBloodGroup(BloodGroup bloodGroup);
}
