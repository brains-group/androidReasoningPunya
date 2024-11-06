package wvw.mobile.rules.explanation;

import wvw.mobile.rules.explanation.models.*;
import wvw.mobile.rules.explanation.types.ModelType;

public class RDFModelFactory {
    public static BaseRDFModel createModel(ModelType type) {
        switch (type) {
            case FOOD_RECOMMENDATION:
                return new FoodRecommendationModel();
            case LOAN_ELIGIBILITY:
                return new LoanEligibilityModel();
            default:
                throw new IllegalArgumentException("Unknown model type: " + type);
        }
    }
} 