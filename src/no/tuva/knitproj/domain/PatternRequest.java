package no.tuva.knitproj.domain;

public class PatternRequest {
    private final Measurements measurements;
    private final Gauge gauge;
    private final FitPreference fitPreference;

    public PatternRequest(
            Measurements measurements,
            Gauge gauge,
            FitPreference fitPreference
    ) {
        this.measurements = measurements;
        this.gauge = gauge;
        this.fitPreference = fitPreference;
    }

    public Measurements getMeasurements() {
        return measurements;
    }

    public Gauge getGauge() {
        return gauge;
    }

    public FitPreference getFitPreference() {
        return fitPreference;
    }
}
