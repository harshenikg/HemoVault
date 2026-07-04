package com.hemovault.repository;

import com.hemovault.model.BloodRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BloodRequestRepository extends JpaRepository<BloodRequest, Long> {

    List<BloodRequest> findByStatusOrderByCreatedAtDesc(BloodRequest.RequestStatus status);

    List<BloodRequest> findAllByOrderByCreatedAtDesc();

    long countByStatus(BloodRequest.RequestStatus status);

    @Query("SELECT r FROM BloodRequest r WHERE r.createdAt >= :since " +
           "AND r.status IN ('APPROVED','ISSUED') ORDER BY r.createdAt DESC")
    List<BloodRequest> findRecentRequests(@Param("since") LocalDateTime since);
}
