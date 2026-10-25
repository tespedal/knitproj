package no.tuva.knitproj.application;

import no.tuva.knitproj.domain.PatternResult;
import no.tuva.knitproj.pattern.raglan.RaglanCalculationResults;

public record GeneratedRaglanPattern(RaglanCalculationResults calculations, PatternResult pattern) {}
