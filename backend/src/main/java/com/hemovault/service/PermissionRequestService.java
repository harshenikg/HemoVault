package com.hemovault.service;

import com.hemovault.dto.PermissionRequestDto;
import com.hemovault.dto.PermissionRequestResponse;
import com.hemovault.model.PermissionRequest;
import com.hemovault.model.User;
import com.hemovault.repository.PermissionRequestRepository;
import com.hemovault.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class PermissionRequestService {

    private final PermissionRequestRepository permRepo;
    private final UserRepository userRepo;

    public List<PermissionRequestResponse> getPending() {
        return permRepo.findByStatusOrderByCreatedAtDesc(PermissionRequest.PermStatus.PENDING)
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public PermissionRequestResponse create(PermissionRequestDto dto, User user) {
        if (permRepo.existsByUserIdAndStatus(user.getId(), PermissionRequest.PermStatus.PENDING))
            throw new IllegalStateException("You already have a pending admin permission request");

        PermissionRequest pr = PermissionRequest.builder()
                .user(user)
                .organization(dto.getOrganization())
                .reason(dto.getReason())
                .status(PermissionRequest.PermStatus.PENDING)
                .build();
        return toResponse(permRepo.save(pr));
    }

    @Transactional
    public PermissionRequestResponse grant(Long id, User admin) {
        PermissionRequest pr = permRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Permission request not found: " + id));
        pr.setStatus(PermissionRequest.PermStatus.GRANTED);
        pr.setReviewedBy(admin);
        pr.setReviewedAt(LocalDateTime.now());
        permRepo.save(pr);

        User target = pr.getUser();
        target.setIsAdmin(true);
        userRepo.save(target);

        return toResponse(pr);
    }

    @Transactional
    public PermissionRequestResponse deny(Long id, User admin) {
        PermissionRequest pr = permRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Permission request not found: " + id));
        pr.setStatus(PermissionRequest.PermStatus.DENIED);
        pr.setReviewedBy(admin);
        pr.setReviewedAt(LocalDateTime.now());
        return toResponse(permRepo.save(pr));
    }

    private PermissionRequestResponse toResponse(PermissionRequest pr) {
        return PermissionRequestResponse.builder()
                .id(pr.getId())
                .userId(pr.getUser().getId())
                .username(pr.getUser().getUsername())
                .fullName(pr.getUser().getFullName())
                .organization(pr.getOrganization())
                .reason(pr.getReason())
                .status(pr.getStatus())
                .createdAt(pr.getCreatedAt())
                .reviewedAt(pr.getReviewedAt())
                .build();
    }
}
