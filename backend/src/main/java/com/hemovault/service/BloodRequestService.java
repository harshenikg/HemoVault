package com.hemovault.service;

import com.hemovault.dto.BloodRequestDto;
import com.hemovault.dto.BloodRequestResponse;
import com.hemovault.model.BloodRequest;
import com.hemovault.model.User;
import com.hemovault.repository.BloodRequestRepository;
import com.hemovault.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Slf4j
public class BloodRequestService {

    private final BloodRequestRepository requestRepo;
    private final UserRepository userRepo;
    private final InventoryService inventoryService;

    public List<BloodRequestResponse> findAll(BloodRequest.RequestStatus status) {
        List<BloodRequest> list = status != null
                ? requestRepo.findByStatusOrderByCreatedAtDesc(status)
                : requestRepo.findAllByOrderByCreatedAtDesc();
        return list.stream().map(this::toResponse).toList();
    }

    public BloodRequestResponse findById(Long id) {
        return toResponse(requestRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Request not found: " + id)));
    }

    public long countPending() {
        return requestRepo.countByStatus(BloodRequest.RequestStatus.PENDING);
    }

    @Transactional
    public BloodRequestResponse create(BloodRequestDto dto) {
        BloodRequest req = BloodRequest.builder()
                .hospitalName(dto.getHospitalName())
                .hospitalContact(dto.getHospitalContact())
                .bloodGroup(dto.getBloodGroup())
                .unitsRequired(dto.getUnitsRequired())
                .requiredByDate(dto.getRequiredByDate())
                .priority(dto.getPriority() != null ? dto.getPriority() : BloodRequest.Priority.NORMAL)
                .patientName(dto.getPatientName())
                .clinicalReason(dto.getClinicalReason())
                .attendingDoctor(dto.getAttendingDoctor())
                .status(BloodRequest.RequestStatus.PENDING)
                .build();
        return toResponse(requestRepo.save(req));
    }

    @Transactional
    public BloodRequestResponse approve(Long id, Long adminId) {
        BloodRequest req = requestRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Request not found: " + id));

        if (req.getStatus() != BloodRequest.RequestStatus.PENDING)
            throw new IllegalStateException("Only PENDING requests can be approved");

        double available = inventoryService.getTotalAvailable(req.getBloodGroup());
        if (available < req.getUnitsRequired()) {
            throw new IllegalStateException(
                    "Insufficient stock for " + req.getBloodGroup().getLabel() +
                    ": " + available + " available, " + req.getUnitsRequired() + " required");
        }

        double deducted = inventoryService.deductUnits(req.getBloodGroup(), req.getUnitsRequired());

        User admin = userRepo.findById(adminId)
                .orElseThrow(() -> new NoSuchElementException("Admin not found"));

        req.setStatus(BloodRequest.RequestStatus.APPROVED);
        req.setUnitsIssued(deducted);
        req.setApprovedBy(admin);
        req.setApprovedAt(LocalDateTime.now());

        log.info("Request #{} approved by admin {} — {} unit(s) of {} deducted",
                id, adminId, deducted, req.getBloodGroup().getLabel());

        return toResponse(requestRepo.save(req));
    }

    @Transactional
    public BloodRequestResponse reject(Long id, String notes) {
        BloodRequest req = requestRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Request not found: " + id));

        if (req.getStatus() != BloodRequest.RequestStatus.PENDING)
            throw new IllegalStateException("Only PENDING requests can be rejected");

        req.setStatus(BloodRequest.RequestStatus.REJECTED);
        req.setAdminNotes(notes);
        return toResponse(requestRepo.save(req));
    }

    @Transactional
    public BloodRequestResponse issue(Long id) {
        BloodRequest req = requestRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Request not found: " + id));

        if (req.getStatus() != BloodRequest.RequestStatus.APPROVED)
            throw new IllegalStateException("Only APPROVED requests can be marked as ISSUED");

        req.setStatus(BloodRequest.RequestStatus.ISSUED);
        return toResponse(requestRepo.save(req));
    }

    private BloodRequestResponse toResponse(BloodRequest r) {
        return BloodRequestResponse.builder()
                .id(r.getId())
                .hospitalName(r.getHospitalName())
                .hospitalContact(r.getHospitalContact())
                .bloodGroup(r.getBloodGroup())
                .bloodGroupLabel(r.getBloodGroup().getLabel())
                .unitsRequired(r.getUnitsRequired())
                .unitsIssued(r.getUnitsIssued())
                .requestDate(r.getRequestDate())
                .requiredByDate(r.getRequiredByDate())
                .priority(r.getPriority())
                .patientName(r.getPatientName())
                .clinicalReason(r.getClinicalReason())
                .attendingDoctor(r.getAttendingDoctor())
                .status(r.getStatus())
                .adminNotes(r.getAdminNotes())
                .approvedBy(r.getApprovedBy() != null ? r.getApprovedBy().getFullName() : null)
                .approvedAt(r.getApprovedAt())
                .createdAt(r.getCreatedAt())
                .build();
    }
}
