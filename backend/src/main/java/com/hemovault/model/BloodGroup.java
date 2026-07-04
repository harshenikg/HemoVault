package com.hemovault.model;

import java.util.Arrays;
import java.util.List;

public enum BloodGroup {
    A_POS("A+"),
    A_NEG("A-"),
    B_POS("B+"),
    B_NEG("B-"),
    AB_POS("AB+"),
    AB_NEG("AB-"),
    O_POS("O+"),
    O_NEG("O-");

    private final String label;

    BloodGroup(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static BloodGroup fromLabel(String label) {
        return Arrays.stream(values())
                .filter(bg -> bg.label.equalsIgnoreCase(label))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown blood group: " + label));
    }

    /**
     * Returns the list of blood groups this donor can donate TO (recipients).
     */
    public List<BloodGroup> compatibleRecipients() {
        return switch (this) {
            case A_POS  -> List.of(A_POS, AB_POS);
            case A_NEG  -> List.of(A_POS, A_NEG, AB_POS, AB_NEG);
            case B_POS  -> List.of(B_POS, AB_POS);
            case B_NEG  -> List.of(B_POS, B_NEG, AB_POS, AB_NEG);
            case AB_POS -> List.of(AB_POS);
            case AB_NEG -> List.of(AB_POS, AB_NEG);
            case O_POS  -> List.of(A_POS, B_POS, AB_POS, O_POS);
            case O_NEG  -> List.of(A_POS, A_NEG, B_POS, B_NEG, AB_POS, AB_NEG, O_POS, O_NEG);
        };
    }

    /**
     * Returns the list of blood groups that can donate TO this recipient.
     */
    public List<BloodGroup> compatibleDonors() {
        return Arrays.stream(values())
                .filter(donor -> donor.compatibleRecipients().contains(this))
                .toList();
    }

    /**
     * Returns true if this donor can donate to the given recipient.
     */
    public boolean isCompatibleDonorFor(BloodGroup recipient) {
        return this.compatibleRecipients().contains(recipient);
    }
}
