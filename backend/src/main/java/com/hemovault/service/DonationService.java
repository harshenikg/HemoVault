package com.hemovault.service;

import com.hemovault.dto.DonationRequest;
import com.hemovault.dto.DonationResponse;
import com.hemovault.model.Donation;
import com.hemovault.model.Donor;
import com.hemovault.repository.DonationRepository;
import com.hemovault.repository.DonorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Slf4j
public class DonationService {

    private final DonationRepository donationRepo;
    private final DonorRepository donorRepo;
    private final InventoryService inventoryService;
    private final EligibilityService eligibilityService;

    public List<DonationResponse> findAll() {
        return donationRepo.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toResponse)
                .toList();
    }

    public List<DonationResponse> findByDonor(Long donorId) {
        return donationRepo.findByDonorId(donorId).stream()
                .map(this::toResponse)
                .toList();
    }

    public DonationResponse findById(Long id) {
        return toResponse(donationRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Donation not found: " + id)));
    }

    @Transactional
    public DonationResponse record(DonationRequest req) {
        Donor donor = donorRepo.findById(req.getDonorId())
                .orElseThrow(() -> new NoSuchElementException("Donor not found: " + req.getDonorId()));

        double units = req.getUnitsDonated() != null ? req.getUnitsDonated() : 1.0;
        double hb    = req.getHemoglobinAtDonation() != null ? req.getHemoglobinAtDonation() : 0.0;

        boolean accepted = hb >= 12.5;
        Donation.DonationStatus status = accepted
                ? Donation.DonationStatus.COMPLETED
                : Donation.DonationStatus.DEFERRED;
        String deferReason = accepted ? null : "Hemoglobin " + hb + " g/dL below 12.5 threshold";

        Donation donation = Donation.builder()
                .donor(donor)
                .bloodGroup(donor.getBloodGroup())
                .unitsDonated(units)
                .donationDate(req.getDonationDate())
                .hemoglobinAtDonation(hb)
                .bpSystolic(req.getBpSystolic())
                .bpDiastolic(req.getBpDiastolic())
                .status(status)
                .deferReason(deferReason)
                .notes(req.getNotes())
                .build();

        donation = donationRepo.save(donation);

        String stockImpact;
        if (accepted) {
            LocalDate expiry = req.getDonationDate().plusDays(35);
            inventoryService.addUnits(
                    donor.getBloodGroup(), units,
                    req.getDonationDate(), expiry,
                    "DON-" + donation.getId(),
                    "Donation by " + donor.getName());
            stockImpact = "+" + units + " unit(s) of " + donor.getBloodGroup().getLabel() + " added to inventory";

            // Update donor's last donation date and re-check eligibility
            donor.setLastDonationDate(req.getDonationDate());
            eligibilityService.updateDonorEligibility(donor);
            donorRepo.save(donor);
        } else {
            stockImpact = "Deferred — Hb " + hb + " below 12.5 g/dL";
        }

        DonationResponse resp = toResponse(donation);
        resp.setStockImpact(stockImpact);
        return resp;
    }

    public DonationResponse toResponse(Donation d) {
        return DonationResponse.builder()
                .id(d.getId())
                .donorId(d.getDonor().getId())
                .donorName(d.getDonor().getName())
                .bloodGroup(d.getBloodGroup())
                .bloodGroupLabel(d.getBloodGroup().getLabel())
                .unitsDonated(d.getUnitsDonated())
                .donationDate(d.getDonationDate())
                .hemoglobinAtDonation(d.getHemoglobinAtDonation())
                .status(d.getStatus())
                .deferReason(d.getDeferReason())
                .createdAt(d.getCreatedAt())
                .build();
    }
}
