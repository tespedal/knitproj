package no.tuva.knitproj.application;

import no.tuva.knitproj.domain.PatternResult;
import no.tuva.knitproj.pattern.tee.TeeCalculationResults;

public record GeneratedPattern(TeeCalculationResults calculations, PatternResult pattern) {}
