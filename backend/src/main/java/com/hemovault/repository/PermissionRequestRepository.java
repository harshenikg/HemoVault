package com.hemovault.repository;

import com.hemovault.model.PermissionRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PermissionRequestRepository extends JpaRepository<PermissionRequest, Long> {
    List<PermissionRequest> findByStatusOrderByCreatedAtDesc(PermissionRequest.PermStatus status);
    boolean existsByUserIdAndStatus(Long userId, PermissionRequest.PermStatus status);
    List<PermissionRequest> findByUserId(Long userId);
}
