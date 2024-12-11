package wvw.mobile.rules.explanation;

// Java Standard Libraries
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

// Apache Jena Core Libraries
import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.TypeMapper;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

// Apache Jena Reasoner Libraries
import com.hp.hpl.jena.reasoner.Derivation;
import com.hp.hpl.jena.reasoner.rulesys.RuleDerivation;
import com.hp.hpl.jena.util.PrintUtil;

// Android Libraries
import android.util.Log;


public class ExplanationRunner {

    // ------------------------------------------------------------
    // Logging
    // ------------------------------------------------------------
    

    private static final String TAG = "Explanation-Runner";
    private static StringBuilder output = new StringBuilder();

    private static void appendToOutput(String text) {
        output.append(text).append("\n");
    }

    private static void clearOutput() {
        output = new StringBuilder();
    }

    public static String getExplanation(String model, String explanationType, Statement selectedTriple) {
        clearOutput();
        
        switch (model) {
            case "loan-eligibility":
                return runLoanEligibilityExplanation(explanationType, selectedTriple);
            case "food-recommendation":
                return runFoodRecommendationExplanation(explanationType, selectedTriple);
            case "transitive":
                return runTransitiveExplanation(explanationType, selectedTriple);
            default:
                return "Invalid model selected";
        }
    }

    private static String runFoodRecommendationExplanation(String type, Statement selectedTriple) {
        try {
            // Register prefixes first
            com.hp.hpl.jena.util.PrintUtil.registerPrefix("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
            com.hp.hpl.jena.util.PrintUtil.registerPrefix("schema", "http://schema.org/");
            com.hp.hpl.jena.util.PrintUtil.registerPrefix("ex", "http://example.com/");
            com.hp.hpl.jena.util.PrintUtil.registerPrefix("foaf", "http://xmlns.com/foaf/0.1/");
            com.hp.hpl.jena.util.PrintUtil.registerPrefix("xsd", "http://www.w3.org/2001/XMLSchema#");

            Explainer explainer = new Explainer();
            Model baseModel = ModelFactory.getFoodRecommendationBaseModel();
            if (baseModel == null) {
                return "Error: Food recommendation base model is null";
            }
            explainer.Model(baseModel);
            
            String rules = ModelFactory.getFoodRecommendationRules();
            if (rules == null || rules.isEmpty()) {
                return "Error: Food recommendation rules are empty";
            }
            explainer.Rules(rules);
            
            switch (type) {
                case "trace-based":
                    return explainer.GetFullTraceBasedExplanation(
                        selectedTriple.getSubject(),
                        selectedTriple.getPredicate(),
                        selectedTriple.getObject()
                    );
                case "contextual":
                    return explainer.GetShallowContextualExplanation(
                        selectedTriple.getSubject(),
                        selectedTriple.getPredicate(),
                        selectedTriple.getObject()
                    );
                case "contrastive":
                    return explainer.GetFullContrastiveExplanation_B(
                        selectedTriple,
                        ModelFactory.getFoodRecommendationBaseModelBanana()
                    );
                case "counterfactual":
                    return explainer.GetCounterfactualExplanation(selectedTriple);
                default:
                    return "Invalid explanation type";
            }
        } catch (Exception e) {
            Log.e(TAG, "Error generating explanation", e);
            return "Error generating explanation: " + e.getMessage() + "\n" + e.getStackTrace()[0].toString();
        }
    }

    private static String runTransitiveExplanation(String type, Statement selectedTriple) {
        try {
            // Register prefixes first
            com.hp.hpl.jena.util.PrintUtil.registerPrefix("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
            com.hp.hpl.jena.util.PrintUtil.registerPrefix("schema", "http://schema.org/");
            com.hp.hpl.jena.util.PrintUtil.registerPrefix("ex", "http://example.com/");
            com.hp.hpl.jena.util.PrintUtil.registerPrefix("xsd", "http://www.w3.org/2001/XMLSchema#");

            Explainer explainer = new Explainer();
            Model baseModel = ModelFactory.getTransitiveBaseModel();
            if (baseModel == null) {
                return "Error: Transitive base model is null";
            }
            explainer.Model(baseModel);
            
            String rules = ModelFactory.getTransitiveRules();
            if (rules == null || rules.isEmpty()) {
                return "Error: Transitive rules are empty";
            }
            explainer.Rules(rules);

            switch (type) {
                case "trace-based":
                    return explainer.GetFullTraceBasedExplanation(
                        selectedTriple.getSubject(),
                        selectedTriple.getPredicate(),
                        selectedTriple.getObject()
                    );
                case "contextual":
                    return explainer.GetShallowContextualExplanation(
                        selectedTriple.getSubject(),
                        selectedTriple.getPredicate(),
                        selectedTriple.getObject()
                    );
                default:
                    return "Only trace-based and contextual explanations are supported for the transitive model";
            }
        } catch (Exception e) {
            return "Error generating explanation: " + e.getMessage() + "\n" + e.getStackTrace()[0].toString();
        }
    }

    private static String runLoanEligibilityExplanation(String type, Statement selectedTriple) {
        try {
            // Register prefixes first
            com.hp.hpl.jena.util.PrintUtil.registerPrefix("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
            com.hp.hpl.jena.util.PrintUtil.registerPrefix("schema", "http://schema.org/");
            com.hp.hpl.jena.util.PrintUtil.registerPrefix("ex", "http://example.com/");
            com.hp.hpl.jena.util.PrintUtil.registerPrefix("foaf", "http://xmlns.com/foaf/0.1/");
            com.hp.hpl.jena.util.PrintUtil.registerPrefix("xsd", "http://www.w3.org/2001/XMLSchema#");

            Explainer explainer = new Explainer();
            explainer.Model(ModelFactory.getLoanEligibilityBaseModel());
            explainer.Rules(ModelFactory.getLoanEligibilityRules());
            
            switch (type) {
                case "trace-based":
                    return explainer.GetFullTraceBasedExplanation(
                        selectedTriple.getSubject(),
                        selectedTriple.getPredicate(),
                        selectedTriple.getObject()
                    );
                case "contextual":
                    return explainer.GetShallowContextualExplanation(
                        selectedTriple.getSubject(),
                        selectedTriple.getPredicate(),
                        selectedTriple.getObject()
                    );
                case "contrastive":
                    return explainer.GetFullContrastiveExplanation_B(
                        selectedTriple,
                        ModelFactory.getLoanEligibilityBaseModelSecondType()
                    );
                case "counterfactual":
                    return explainer.GetCounterfactualExplanation(selectedTriple);
                default:
                    return "Invalid explanation type";
            }
        } catch (Exception e) {
            return "Error generating explanation: " + e.getMessage() + "\n" + e.getStackTrace()[0].toString();
        }
    }
}