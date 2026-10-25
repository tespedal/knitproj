package no.tuva.knitproj.application;

import no.tuva.knitproj.domain.PatternRequest;
import no.tuva.knitproj.domain.SweaterOptions;
import no.tuva.knitproj.domain.TeeOptions;

public sealed interface GenerationRequest permits GenerationRequest.Tee, GenerationRequest.Raglan {
    PatternRequest request();

    record Tee(PatternRequest request, TeeOptions options) implements GenerationRequest {
        public Tee {
            if (request == null || options == null) {
                throw new IllegalArgumentException("T-shirt request and options are required.");
            }
        }
    }

    record Raglan(PatternRequest request, SweaterOptions options) implements GenerationRequest {
        public Raglan {
            if (request == null || options == null) {
                throw new IllegalArgumentException("Sweater request and options are required.");
            }
        }
    }
}
