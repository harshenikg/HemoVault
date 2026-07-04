package com.hemovault.service;

import com.hemovault.dto.DonorResponse;
import com.hemovault.dto.EmergencyMatchRequest;
import com.hemovault.dto.EmergencyMatchResponse;
import com.hemovault.model.BloodGroup;
import com.hemovault.model.Donor;
import com.hemovault.repository.DonorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmergencyMatchingService {

    private final DonorRepository donorRepo;
    private final InventoryService inventoryService;
    private final DonorService donorService;

    public EmergencyMatchResponse findMatches(EmergencyMatchRequest req, boolean isAdmin) {
        BloodGroup bg = req.getBloodGroup();
        List<BloodGroup> compatibleDonors = bg.compatibleDonors();

        List<Donor> donors;
        if (req.getCity() != null && !req.getCity().isBlank()) {
            donors = donorRepo.findEligibleByBloodGroupsAndCity(compatibleDonors, req.getCity());
            // Also add non-city matches if few results
            if (donors.size() < 5) {
                List<Donor> allMatches = donorRepo.findEligibleByBloodGroups(compatibleDonors);
                allMatches.stream()
                        .filter(d -> donors.stream().noneMatch(e -> e.getId().equals(d.getId())))
                        .forEach(donors::add);
            }
        } else {
            donors = donorRepo.findEligibleByBloodGroups(compatibleDonors);
        }

        // Score and sort: lower score = higher priority
        String searchCity = req.getCity() != null ? req.getCity().toLowerCase() : "";
        List<Donor> sorted = donors.stream()
                .sorted(Comparator.comparingInt(d -> {
                    int score = 0;
                    if (!d.getBloodGroup().equals(bg)) score += 20;
                    if (!searchCity.isBlank() && !d.getCity().toLowerCase().contains(searchCity)) score += 10;
                    return score;
                }))
                .limit(10)
                .toList();

        List<DonorResponse> matched = sorted.stream()
                .map(d -> donorService.toResponse(d, isAdmin))
                .toList();

        double stockAvailable = inventoryService.getTotalAvailable(bg);
        double needed = req.getUnitsRequired() != null ? req.getUnitsRequired() : 1.0;
        String stockStatus;
        if (stockAvailable >= needed) stockStatus = "SUFFICIENT";
        else if (stockAvailable > 0)  stockStatus = "PARTIAL";
        else                          stockStatus = "UNAVAILABLE";

        List<String> compatibleLabels = compatibleDonors.stream()
                .map(BloodGroup::getLabel).toList();

        return EmergencyMatchResponse.builder()
                .compatibleGroups(compatibleLabels)
                .matchedDonors(matched)
                .totalMatchesFound(donors.size())
                .stockAvailable(stockAvailable)
                .stockStatus(stockStatus)
                .build();
    }
}
