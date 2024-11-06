package wvw.mobile.rules.explanation;

import android.util.Log;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.datatypes.TypeMapper;
import wvw.mobile.rules.explanation.types.*;
import wvw.mobile.rules.explanation.utils.URIConstants;
import wvw.mobile.rules.explanation.models.TransitiveModel;

public class ExplanationRunner {
    private static final String TAG = "Explanation-Runner";
    private static final TypeMapper typeMapper = TypeMapper.getInstance();
    private static final RDFDatatype xsdDouble = typeMapper.getTypeByName("http://www.w3.org/2001/XMLSchema#double");

    public static void print(String message) {
        Log.d(TAG, message);
    }

    public static void runExplanation(ModelType modelType, ExplanationType.Type explanationType) {
        print(String.format("Running %s explanation on %s model...", explanationType, modelType));
        
        // Create model and explainer
        BaseRDFModel model = RDFModelFactory.createModel(modelType);
        Explainer explainer = new Explainer();
        explainer.setModel(model);
        explainer.setExplanationType(ExplanationTypeFactory.createExplanationType(explanationType));
        
        // Set up parameters based on model type
        setupExplanationParameters(explainer, modelType);
        
        // Generate and print explanation
        String result = explainer.explain();
        print(result);
    }

    private static void setupExplanationParameters(Explainer explainer, ModelType modelType) {
        Model model = explainer.getModel();
        
        switch (modelType) {
            case FOOD_RECOMMENDATION:
                setupFoodRecommendationParameters(explainer, model);
                break;
            case LOAN_ELIGIBILITY:
                setupLoanEligibilityParameters(explainer, model);
                break;
            case TRANSITIVE:
                setupTransitiveParameters(explainer, modelType);
                break;
        }
    }

    private static void setupFoodRecommendationParameters(Explainer explainer, Model model) {
        Resource person = model.getResource(URIConstants.FOAF_URI + "Person");
        Property totalSugars = model.getProperty(URIConstants.EX_URI + "totalSugars");
        RDFNode sugarValue = ResourceFactory.createTypedLiteral("20.78", xsdDouble);
        
        explainer.setExplanationParameters(person, totalSugars, sugarValue);
    }

    private static void setupLoanEligibilityParameters(Explainer explainer, Model model) {
        Resource applicant = model.getResource(URIConstants.FOAF_URI + "Person");
        Property debtToIncomeRatio = model.getProperty(URIConstants.EX_URI + "debtToIncomeRatio");
        RDFNode ratioValue = ResourceFactory.createTypedLiteral("0.30", xsdDouble);
        
        explainer.setExplanationParameters(applicant, debtToIncomeRatio, ratioValue);
    }

    private static void setupTransitiveParameters(Explainer explainer, ModelType modelType) {
        TransitiveModel transitiveModel = (TransitiveModel) explainer.getModel();
        
        // We want to explain how A equals D (which requires inference through B and C)
        Resource nodeA = transitiveModel.getNode("A");
        Resource nodeD = transitiveModel.getNode("D");
        Property equals = transitiveModel.getEqualsProperty();
        
        explainer.setExplanationParameters(nodeA, equals, nodeD);
        
        // For counterfactual explanations, we need the alternative model
        // which shows a different path from A to D (through E)
        if (modelType == ModelType.COUNTERFACTUAL) {
            explainer.setAlternativeModel(transitiveModel.createAlternativeModel());
        }
    }

    public static void run() {
        // Run all combinations of models and explanation types
        for (ModelType modelType : ModelType.values()) {
            for (ExplanationType.Type explanationType : ExplanationType.Type.values()) {
                runExplanation(modelType, explanationType);
            }
        }
    }
}