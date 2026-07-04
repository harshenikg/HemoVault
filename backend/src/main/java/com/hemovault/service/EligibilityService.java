package com.hemovault.service;

import com.hemovault.dto.EligibilityRequest;
import com.hemovault.dto.EligibilityResponse;
import com.hemovault.model.Donor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
public class EligibilityService {

    public EligibilityResponse check(EligibilityRequest req) {
        List<String> reasons = new ArrayList<>();

        if (req.getAge() != null && (req.getAge() < 18 || req.getAge() > 65))
            reasons.add("Age must be between 18 and 65 years (current: " + req.getAge() + ")");

        if (req.getWeight() != null && req.getWeight() < 50)
            reasons.add("Weight must be at least 50 kg (current: " + req.getWeight() + " kg)");

        if (req.getHemoglobin() != null && req.getHemoglobin() < 12.5)
            reasons.add("Hemoglobin must be at least 12.5 g/dL (current: " + req.getHemoglobin() + ")");

        if (req.getBpSystolic() != null && req.getBpDiastolic() != null) {
            if (req.getBpSystolic() < 90 || req.getBpSystolic() > 160)
                reasons.add("Systolic BP must be 90–160 mmHg (current: " + req.getBpSystolic() + ")");
            if (req.getBpDiastolic() < 60 || req.getBpDiastolic() > 100)
                reasons.add("Diastolic BP must be 60–100 mmHg (current: " + req.getBpDiastolic() + ")");
        }

        if (req.getLastDonationDate() != null) {
            long daysSince = ChronoUnit.DAYS.between(req.getLastDonationDate(), LocalDate.now());
            if (daysSince < 90)
                reasons.add("Must wait 90 days between donations (" + daysSince + " days since last donation)");
        }

        if (Boolean.TRUE.equals(req.getHasRecentIllness()))
            reasons.add("Recent illness in the past 2 weeks disqualifies donation");

        if (Boolean.TRUE.equals(req.getIsPregnant()))
            reasons.add("Pregnancy disqualifies donation");

        if (Boolean.TRUE.equals(req.getOnBloodThinners()))
            reasons.add("Currently on blood thinners — not eligible");

        if (Boolean.TRUE.equals(req.getRecentTattoo()))
            reasons.add("Tattoo within the last 6 months — not eligible");

        boolean eligible = reasons.isEmpty();
        return EligibilityResponse.builder()
                .eligible(eligible)
                .status(eligible ? "ELIGIBLE" : "NOT ELIGIBLE")
                .reasons(reasons)
                .build();
    }

    /** Updates a Donor entity's eligibility fields in-place using donor's own data. */
    public void updateDonorEligibility(Donor donor) {
        List<String> reasons = new ArrayList<>();

        if (donor.getAge() != null && (donor.getAge() < 18 || donor.getAge() > 65))
            reasons.add("Age must be between 18 and 65 years");

        if (donor.getWeight() != null && donor.getWeight() < 50)
            reasons.add("Weight must be at least 50 kg");

        if (donor.getHemoglobin() != null && donor.getHemoglobin() < 12.5)
            reasons.add("Hemoglobin below 12.5 g/dL");

        if (donor.getBpSystolic() != null && donor.getBpDiastolic() != null) {
            if (donor.getBpSystolic() < 90 || donor.getBpSystolic() > 160)
                reasons.add("Systolic BP out of range (90–160)");
            if (donor.getBpDiastolic() < 60 || donor.getBpDiastolic() > 100)
                reasons.add("Diastolic BP out of range (60–100)");
        }

        if (donor.getLastDonationDate() != null) {
            long daysSince = ChronoUnit.DAYS.between(donor.getLastDonationDate(), LocalDate.now());
            if (daysSince < 90)
                reasons.add("Must wait 90 days between donations");
        }

        if (Boolean.TRUE.equals(donor.getHasRecentIllness()))
            reasons.add("Recent illness in past 2 weeks");

        if (Boolean.TRUE.equals(donor.getIsPregnant()))
            reasons.add("Currently pregnant");

        donor.setIsEligible(reasons.isEmpty());
        donor.setEligibilityReason(reasons.isEmpty() ? "All criteria met" : String.join("; ", reasons));
    }
}
