package wvw.mobile.rules.explanation;

import wvw.mobile.rules.explanation.types.*;

public class ExplanationTypeFactory {
    public static ExplanationType createExplanationType(ExplanationType.Type type) {
        switch (type) {
            case TRACE_BASED:
                return new TraceBased();
            case CONTEXTUAL:
                return new Contextual();
            case COUNTERFACTUAL:
                return new Counterfactual();
            case CONTRASTIVE:
                return new Contrastive();
            default:
                throw new IllegalArgumentException("Unknown explanation type: " + type);
        }
    }
} 