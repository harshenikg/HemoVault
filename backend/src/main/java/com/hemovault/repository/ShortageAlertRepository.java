package com.hemovault.repository;

import com.hemovault.model.BloodGroup;
import com.hemovault.model.ShortageAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShortageAlertRepository extends JpaRepository<ShortageAlert, Long> {
    List<ShortageAlert> findByIsResolvedFalseOrderByCreatedAtDesc();
    Optional<ShortageAlert> findByBloodGroupAndAlertTypeAndIsResolvedFalse(
            BloodGroup bloodGroup, ShortageAlert.AlertType alertType);
    List<ShortageAlert> findByBloodGroupAndIsResolvedFalse(BloodGroup bloodGroup);
}
