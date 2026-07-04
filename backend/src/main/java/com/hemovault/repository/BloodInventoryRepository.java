package com.hemovault.repository;

import com.hemovault.model.BloodGroup;
import com.hemovault.model.BloodInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BloodInventoryRepository extends JpaRepository<BloodInventory, Long> {

    List<BloodInventory> findByBloodGroupAndStatus(BloodGroup bloodGroup, BloodInventory.InventoryStatus status);

    List<BloodInventory> findAllByOrderByCreatedAtDesc();

    /** FIFO: Available batches for a blood group ordered by earliest expiry first */
    @Query("SELECT b FROM BloodInventory b WHERE b.bloodGroup = :bg " +
           "AND b.status = 'AVAILABLE' AND b.expiryDate >= CURRENT_DATE " +
           "ORDER BY b.expiryDate ASC")
    List<BloodInventory> findAvailableByBloodGroupFIFO(@Param("bg") BloodGroup bg);

    /** Total available units for a blood group (non-expired) */
    @Query("SELECT COALESCE(SUM(b.unitsAvailable), 0) FROM BloodInventory b " +
           "WHERE b.bloodGroup = :bg AND b.status = 'AVAILABLE' AND b.expiryDate >= CURRENT_DATE")
    Double getTotalAvailableByBloodGroup(@Param("bg") BloodGroup bg);

    /** Batches where expiry_date < today and still AVAILABLE (to be expired) */
    @Query("SELECT b FROM BloodInventory b WHERE b.expiryDate < CURRENT_DATE " +
           "AND b.status = 'AVAILABLE'")
    List<BloodInventory> findExpired();

    /** Batches expiring between today and warningDate */
    @Query("SELECT b FROM BloodInventory b WHERE b.expiryDate BETWEEN CURRENT_DATE AND :warningDate " +
           "AND b.status = 'AVAILABLE' ORDER BY b.expiryDate ASC")
    List<BloodInventory> findExpiringBefore(@Param("warningDate") LocalDate warningDate);
}
