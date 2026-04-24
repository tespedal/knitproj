package no.tuva.knitproj.domain;

public record Measurements(double neckCircumferenceCm, double shoulderWidthCm, double shoulderAcrossCm,
                           double bustCircumferenceCm, double underbustCircumferenceCm, double waistCircumferenceCm,
                           double backLengthCm, double armholeDepthCm, double upperArmCircumferenceCm,
                           double bodyLengthCm) {

    // Default: Medium size
    public Measurements() {
        this(
                35, // neck
                37, // shoulder width
                4,  // shoulder across (note: consider renaming, see below)
                95, // bust
                80, // underbust
                76, // waist
                41, // back length (corrected from 54 → more realistic)
                21, // armhole depth
                29, // upper arm
                60  // body length (T-shirt typical)
        );
    }

    // Full constructor (scalable)

    // Getters
}