package com.hemovault.service;

import com.hemovault.dto.DonorRequest;
import com.hemovault.dto.DonorResponse;
import com.hemovault.model.BloodGroup;
import com.hemovault.model.Donor;
import com.hemovault.repository.DonorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class DonorService {

    private final DonorRepository donorRepo;
    private final EligibilityService eligibilityService;

    public List<DonorResponse> findAll(boolean isAdmin) {
        return donorRepo.findAll().stream()
                .map(d -> toResponse(d, isAdmin))
                .toList();
    }

    public DonorResponse findById(Long id, boolean isAdmin) {
        Donor d = donorRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Donor not found: " + id));
        return toResponse(d, isAdmin);
    }

    public List<DonorResponse> search(BloodGroup bloodGroup, String city, boolean isAdmin) {
        List<Donor> results;
        if (bloodGroup != null && city != null && !city.isBlank()) {
            results = donorRepo.findEligibleByBloodGroupsAndCity(
                    bloodGroup.compatibleDonors(), city);
        } else if (bloodGroup != null) {
            results = donorRepo.findByBloodGroup(bloodGroup);
        } else if (city != null && !city.isBlank()) {
            results = donorRepo.findByCityContainingIgnoreCase(city);
        } else {
            results = donorRepo.findAll();
        }
        return results.stream().map(d -> toResponse(d, isAdmin)).toList();
    }

    @Transactional
    public DonorResponse create(DonorRequest req, boolean isAdmin) {
        Donor donor = Donor.builder()
                .name(req.getName())
                .age(req.getAge())
                .bloodGroup(req.getBloodGroup())
                .contactNumber(req.getContactNumber())
                .email(req.getEmail())
                .city(req.getCity())
                .address(req.getAddress())
                .weight(req.getWeight())
                .hemoglobin(req.getHemoglobin())
                .bpSystolic(req.getBpSystolic())
                .bpDiastolic(req.getBpDiastolic())
                .isPregnant(req.getIsPregnant() != null ? req.getIsPregnant() : false)
                .hasRecentIllness(req.getHasRecentIllness() != null ? req.getHasRecentIllness() : false)
                .lastDonationDate(req.getLastDonationDate())
                .build();

        eligibilityService.updateDonorEligibility(donor);
        donor = donorRepo.save(donor);
        return toResponse(donor, isAdmin);
    }

    @Transactional
    public DonorResponse update(Long id, DonorRequest req, boolean isAdmin) {
        Donor donor = donorRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Donor not found: " + id));

        donor.setName(req.getName());
        donor.setAge(req.getAge());
        donor.setBloodGroup(req.getBloodGroup());
        donor.setContactNumber(req.getContactNumber());
        donor.setEmail(req.getEmail());
        donor.setCity(req.getCity());
        donor.setAddress(req.getAddress());
        donor.setWeight(req.getWeight());
        donor.setHemoglobin(req.getHemoglobin());
        donor.setBpSystolic(req.getBpSystolic());
        donor.setBpDiastolic(req.getBpDiastolic());
        if (req.getIsPregnant() != null) donor.setIsPregnant(req.getIsPregnant());
        if (req.getHasRecentIllness() != null) donor.setHasRecentIllness(req.getHasRecentIllness());
        if (req.getLastDonationDate() != null) donor.setLastDonationDate(req.getLastDonationDate());

        eligibilityService.updateDonorEligibility(donor);
        donor = donorRepo.save(donor);
        return toResponse(donor, isAdmin);
    }

    @Transactional
    public void delete(Long id) {
        if (!donorRepo.existsById(id))
            throw new NoSuchElementException("Donor not found: " + id);
        donorRepo.deleteById(id);
    }

    public DonorResponse toResponse(Donor d, boolean isAdmin) {
        DonorResponse.DonorResponseBuilder b = DonorResponse.builder()
                .id(d.getId())
                .name(d.getName())
                .age(d.getAge())
                .bloodGroup(d.getBloodGroup())
                .bloodGroupLabel(d.getBloodGroup().getLabel())
                .city(d.getCity())
                .isEligible(d.getIsEligible())
                .eligibilityReason(d.getEligibilityReason())
                .lastDonationDate(d.getLastDonationDate())
                .createdAt(d.getCreatedAt());

        if (isAdmin) {
            b.contactNumber(d.getContactNumber())
             .email(d.getEmail())
             .weight(d.getWeight())
             .hemoglobin(d.getHemoglobin())
             .address(d.getAddress());
            if (d.getBpSystolic() != null && d.getBpDiastolic() != null)
                b.bloodPressure(d.getBpSystolic() + "/" + d.getBpDiastolic());
        }

        return b.build();
    }
}
