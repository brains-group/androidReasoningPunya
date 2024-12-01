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
    
    // Helper method for consistent logging
    private static void logSection(String title) {
        Log.d(TAG, "\n" + "=".repeat(75));
        Log.d(TAG, title);
        Log.d(TAG, "=".repeat(75));
    }
    
    private static void logSubSection(String title) {
        Log.d(TAG, "\n" + "-".repeat(50));
        Log.d(TAG, title);
        Log.d(TAG, "-".repeat(50));
    }
    
    private static void logDetail(String message) {
        for (String line : message.split("\n")) {
            Log.d(TAG, "\t" + line);
        }
    }

    public static void print(String message) {
        Log.d("Explanation-Runner", message);
    }



    // ------------------------------------------------------------
    // Trace-Based Explanations
    // ------------------------------------------------------------


    // Transitive
    public static void runTraceBasedExplanationTestTransitive() {
        logSection("Running Trace-Based Explanation Test on Transitive Rules");

        // Set up the Explainer
        logSubSection("Setting up Explainer");
        String rules = "[transitiveRule: (?a ex:equals ?b) (?b ex:equals ?c) -> (?a ex:equals ?c)]";
        Explainer explainer = new Explainer();
        explainer.Model(ModelFactory.getTransitiveBaseModel());
        explainer.Rules(rules);
        logDetail("Rules loaded: " + rules);

        // Generate and log explanation
        logSubSection("Trace-Based Explanation");
        String traceResponse = explainer.GetFullTraceBasedExplanation(
            explainer.Model().getResource("A"),
            explainer.Model().getProperty("http://example.com/equals"),
            explainer.Model().getResource("D"));
        logDetail(traceResponse);
    }

    // Food Recommendation
    public static void runTraceBasedExplanationTestFoodRecommendation() {
        logSection("Running Trace-Based Explanation Test on Food Recommendation");

        // Set up the Explainer
        Explainer explainer = new Explainer();

        // Set up the Model with namespaces
        logSubSection("Model Setup");
        Model model = ModelFactory.getFoodRecommendationBaseModel();
        model.setNsPrefix("schema", "http://schema.org/");
        model.setNsPrefix("usda", "http://example.com/usda#");
        model.setNsPrefix("ex", "http://example.com/");
        model.setNsPrefix("foaf", "http://xmlns.com/foaf/0.1/");
        explainer.Model(model);

        logDetail("Base Model contents:");
        StmtIterator baseStmts = explainer.Model().listStatements();
        while(baseStmts.hasNext()) {
            logDetail(baseStmts.next().toString());
        }

        // Set up Rules
        logSubSection("Rules Configuration");
        explainer.Rules(ModelFactory.getFoodRecommendationRulesPrefix());
        logDetail("Rules loaded:");
        logDetail(ModelFactory.getFoodRecommendationRulesPrefix());

        // Generate explanation
        logSubSection("Generating Trace-Based Explanation");
        TypeMapper tm = TypeMapper.getInstance();
        RDFDatatype xsdDouble = tm.getTypeByName("http://www.w3.org/2001/XMLSchema#double");
        logDetail("Data types defined");

        String traceResponse;
        try {
            traceResponse = explainer.GetFullTraceBasedExplanation(
                explainer.Model().getResource("http://xmlns.com/foaf/0.1/Person"),
                explainer.Model().getProperty("http://example.com/totalSugars"),
                ResourceFactory.createTypedLiteral("20.78", xsdDouble)
            );
            logDetail(traceResponse);
        } catch (Exception e) {
            logDetail("Error generating explanation: " + e.getMessage());
            Log.e(TAG, "Error in trace-based explanation", e);
        }
    }

    // Loan Eligibility
    public static void runTraceBasedExplanationTestLoanEligibility() {
        logSection("Running Trace-Based Explanation Test on Loan Eligibility");

        // Set up the Explainer
        logSubSection("Base Model Contents");
        Explainer explainer = new Explainer();
        explainer.Model(ModelFactory.getLoanEligibilityBaseModel());
        explainer.Rules(ModelFactory.getLoanEligibilityRules());
        
        // Log the base model contents
        explainer.Model().listStatements().forEachRemaining(
            statement -> logDetail(statement.toString())
        );

        // Log the rules
        logSubSection("Rules Loaded");
        logDetail(explainer.Rules());
        
        // Create the target statement parameters for Alex's loan ineligibility
        Resource applicant1 = explainer.Model().getResource("http://example.com/applicant1");
        Property loanEligibility = explainer.Model().getProperty("http://example.com/loanEligibility");
        RDFNode eligibilityValue = ResourceFactory.createPlainLiteral("Not Eligible - DTI Too High");

        // Generate and log the trace-based explanation
        String traceResponse = explainer.GetFullTraceBasedExplanation(
            applicant1,
            loanEligibility,
            ResourceFactory.createPlainLiteral("Not Eligible")
        );
        
        logSubSection("Trace-Based Explanation");
        logDetail(traceResponse);
    }



    // ------------------------------------------------------------
    // Contextual Explanations
    // ------------------------------------------------------------


    // Transitive
    public static void runContextualExplanationTestTransitive() {
        logSection("Running Contextual Explanation Test on Transitive Rules");

        // Set up the Explainer
        logSubSection("Setting up Explainer");
        String rules = "[transitiveRule: (?a ex:equals ?b) (?b ex:equals ?c) -> (?a ex:equals ?c)]";
        Explainer explainer = new Explainer();
        explainer.Model(ModelFactory.getTransitiveBaseModel());
        explainer.Rules(rules);
        logDetail("Rules loaded: " + rules);

        // Generate explanations
        logSubSection("Contextual Explanations");
        String shallowExplanation = explainer.GetShallowContextualExplanation(
                explainer.Model().getResource("A"),
                explainer.Model().getProperty("http://example.com/equals"),
                explainer.Model().getResource("D")
        );
        logDetail("Shallow Contextual Explanation:");
        logDetail(shallowExplanation);

        String simpleExplanation = explainer.GetSimpleContextualExplanation(
                explainer.Model().getResource("A"),
                explainer.Model().getProperty("http://example.com/equals"),
                explainer.Model().getResource("D")
        );
        logDetail("\nSimple Contextual Explanation:");
        logDetail(simpleExplanation);
    }

    // Food Recommendation
    public static void runContextualExplanationTestFoodRecommendation() {
        logSection("Running Contextual Explanation Test on Food Recommendation");

        // Set up the Explainer
        Explainer explainer = new Explainer();

        // Set up the Model with namespaces
        logSubSection("Model Setup");
        Model model = ModelFactory.getFoodRecommendationBaseModel();
        model.setNsPrefix("schema", "http://schema.org/");
        model.setNsPrefix("usda", "http://example.com/usda#");
        model.setNsPrefix("ex", "http://example.com/");
        model.setNsPrefix("foaf", "http://xmlns.com/foaf/0.1/");
        explainer.Model(model);

        logDetail("Base Model contents:");
        StmtIterator baseStmts = explainer.Model().listStatements();
        while(baseStmts.hasNext()) {
            logDetail(baseStmts.next().toString());
        }

        // Set up Rules
        logSubSection("Rules Configuration");
        explainer.Rules(ModelFactory.getFoodRecommendationRulesPrefix());
        logDetail("Rules loaded:");
        logDetail(ModelFactory.getFoodRecommendationRulesPrefix());

        // Generate explanations
        logSubSection("Generating Contextual Explanations");
        TypeMapper tm = TypeMapper.getInstance();
        RDFDatatype xsdDouble = tm.getTypeByName("http://www.w3.org/2001/XMLSchema#double");
        logDetail("Data types defined");

        try {
            String shallowExplanation = explainer.GetShallowContextualExplanation(
                    explainer.Model().getResource("http://xmlns.com/foaf/0.1/Person"),
                    explainer.Model().getProperty("http://example.com/totalSugars"),
                    ResourceFactory.createTypedLiteral("20.78", xsdDouble)
            );
            logDetail("Shallow Contextual Explanation:");
            logDetail(shallowExplanation);

            String simpleExplanation = explainer.GetSimpleContextualExplanation(
                    explainer.Model().getResource("http://xmlns.com/foaf/0.1/Person"),
                    explainer.Model().getProperty("http://example.com/totalSugars"),
                    ResourceFactory.createTypedLiteral("20.78", xsdDouble)
            );
            logDetail("\nSimple Contextual Explanation:");
            logDetail(simpleExplanation);
        } catch (Exception e) {
            logDetail("Error generating explanation: " + e.getMessage());
            Log.e(TAG, "Error in contextual explanation", e);
        }
    }

    // Loan Eligibility
    public static void runContextualExplanationTestLoanEligibility() {
        logSection("Running Contextual Explanation Test on Loan Eligibility");

        // Set up the Explainer
        Explainer explainer = new Explainer();
        explainer.Model(ModelFactory.getLoanEligibilityBaseModel());
        explainer.Rules(ModelFactory.getLoanEligibilityRules());
        
        // Log the base model contents
        logSubSection("Base Model Contents");
        explainer.Model().listStatements().forEachRemaining(
            statement -> logDetail(statement.toString())
        );

        // Log the rules
        logSubSection("Rules Loaded");
        logDetail(explainer.Rules());
        
        // Create the target statement parameters for loan eligibility check
        Resource applicant1 = explainer.Model().getResource("http://example.com/applicant1");
        Property loanEligibility = explainer.Model().getProperty("http://example.com/loanEligibility");
        RDFNode eligibilityValue = ResourceFactory.createPlainLiteral("Not Eligible - DTI Too High");

        // Generate and log the contextual explanations
        logSubSection("Generating Contextual Explanations");
        
        String shallowExplanation = explainer.GetShallowContextualExplanation(
            applicant1,
            loanEligibility,
            ResourceFactory.createPlainLiteral("Not Eligible")
        );
        
        String simpleExplanation = explainer.GetSimpleContextualExplanation(
            applicant1,
            loanEligibility,
            ResourceFactory.createPlainLiteral("Not Eligible")
        );
        
        logSubSection("Shallow Contextual Explanation");
        logDetail(shallowExplanation);

        logSubSection("Simple Contextual Explanation");
        logDetail(simpleExplanation);
    }



    // ------------------------------------------------------------
    // Contrastive Explanations
    // ------------------------------------------------------------


    // Food Recommendation
    public static void runContrastiveExplanationTestFoodRecommendation() {
        logSection("Running Contrastive Explanation Test on Food Recommendation");

        // Set up the Explainer
        logSubSection("Model Setup");
        Explainer explainer2 = new Explainer();
        explainer2.Model(ModelFactory.getFoodRecommendationBaseModel());
        explainer2.Rules(ModelFactory.getFoodRecommendationRulesPrefix());

        logDetail("Base Model contents:");
        StmtIterator baseStmts = explainer2.Model().listStatements();
        while(baseStmts.hasNext()) {
            logDetail(baseStmts.next().toString());
        }

        logDetail("\nRules loaded:");
        logDetail(explainer2.Rules());

        // Set up the Additional Model
        logSubSection("Inference Model Setup");
        InfModel infModel = ModelFactory.getFoodRecommendationInfModel();
        logDetail("Inference Model contents:");
        StmtIterator infStmts = infModel.listStatements();
        while(infStmts.hasNext()) {
            logDetail(infStmts.next().toString());
        }

        // Generate explanation
        logSubSection("Generating Contrastive Explanation");
        Resource observation = infModel.getResource("http://example.com/observation");
        Property allowedToEat = infModel.getProperty("http://example.com/allowedToEat");
        logDetail("Resources and properties configured");

        StmtIterator itr = infModel.listStatements(observation, allowedToEat, (RDFNode) null);
        while(itr.hasNext()) {
            Statement s = itr.next();
            String contrastiveExplanation = explainer2.GetFullContrastiveExplanation_B(
                s, 
                ModelFactory.getFoodRecommendationBaseModelBanana()
            );
            logDetail("\nContrastive Explanation:");
            logDetail(contrastiveExplanation);
        }
    }

    // Loan Eligibility
    public static void runContrastiveExplanationTestLoanEligibility() {
        logSection("Running Contrastive Explanation Test on Loan Eligibility");

        // Set up the Explainer
        Explainer explainer = new Explainer();
        explainer.Model(ModelFactory.getLoanEligibilityBaseModel());
        explainer.Rules(ModelFactory.getLoanEligibilityRules());

        logSubSection("Base Model Contents");
        StmtIterator baseStmts = explainer.Model().listStatements();
        while(baseStmts.hasNext()) {
            logDetail(baseStmts.next().toString());
        }

        logSubSection("Rules Loaded");
        logDetail(explainer.Rules());

        // Set up the Inference Model
        logSubSection("Inference Model Contents");
        InfModel infModel = ModelFactory.getLoanEligibilityInfModel();
        StmtIterator infStmts = infModel.listStatements();
        while(infStmts.hasNext()) {
            logDetail(infStmts.next().toString());
        }

        // Set up the Additional Inference Model
        logSubSection("Additional Inference Model Contents");
        InfModel additionalInfModel = ModelFactory.getLoanEligibilityInfModelSecondType();
        StmtIterator additionalInfStmts = additionalInfModel.listStatements();
        while(additionalInfStmts.hasNext()) {
            logDetail(additionalInfStmts.next().toString());
        }

        // Create the target statement parameters for loan eligibility check
        Resource applicant1 = explainer.Model().getResource("http://example.com/applicant1");
        Property loanEligibility = explainer.Model().getProperty("http://example.com/loanEligibility");
        RDFNode eligibilityValue = ResourceFactory.createPlainLiteral("Not Eligible - DTI Too High");

        // Generate and log contrastive explanation
        logSubSection("Generating Contrastive Explanation");

        StmtIterator itr = infModel.listStatements(applicant1, loanEligibility, ResourceFactory.createPlainLiteral("Not Eligible"));
        String contrastiveExplanation = "";
        while (itr.hasNext()) {
            Statement s = itr.next();
            contrastiveExplanation += explainer.GetFullContrastiveExplanation_B(
                s, 
                ModelFactory.getLoanEligibilityBaseModelSecondType()
            );
        }

        logSubSection("Contrastive Explanation");
        logDetail(contrastiveExplanation);
    }



    // ------------------------------------------------------------
    // Counterfactual Explanations
    // ------------------------------------------------------------


    // Transitive
    public static void runCounterfactualExplanationTestTransitive() {
        logSection("Running Counterfactual Explanation Test on Transitive");
    }

    // Food Recommendation
    public static void runCounterfactualExplanationTestFoodRecommendation() {
        logSection("Running Counterfactual Explanation Test on Food Recommendation");
    }

    // Loan Eligibility
    public static void runCounterfactualExplanationTestLoanEligibility() {
        logSection("Running Counterfactual Explanation Test on Loan Eligibility");
        
        // Set up the Explainer
        Explainer explainer = new Explainer();
        explainer.Model(ModelFactory.getLoanEligibilityBaseModel());
        explainer.Rules(ModelFactory.getLoanEligibilityRules());
        
        // Log initial state
        logSubSection("Base Model Contents");
        StmtIterator baseStmts = explainer.Model().listStatements();
        while(baseStmts.hasNext()) {
            logDetail(baseStmts.next().toString());
        }
        
        logSubSection("Rules Loaded");
        logDetail(explainer.Rules());

        // Set up the Inference Model
        logSubSection("Inference Model Contents");
        InfModel infModel = ModelFactory.getLoanEligibilityInfModel();
        StmtIterator infStmts = infModel.listStatements();
        while(infStmts.hasNext()) {
            logDetail(infStmts.next().toString());
        }

        logSubSection("Generating Counterfactual Explanation");

        // Create the target statement parameters for loan eligibility check
        Resource applicant1 = infModel.getResource("http://example.com/applicant1");
        Property loanEligibility = explainer.Model().getProperty("http://example.com/loanEligibility");

        StmtIterator itr = infModel.listStatements(applicant1, loanEligibility, ResourceFactory.createPlainLiteral("Not Eligible"));
        while (itr.hasNext()) {
            Statement s = itr.next();
            String explanation = explainer.GetCounterfactualExplanation(s);
            logSubSection("Counterfactual Explanation");
            logDetail(explanation);
        }
    }



    // ------------------------------------------------------------
    // All Explanation Tests
    // ------------------------------------------------------------


    // Runs all explanation tests
    public static void runExplanationTest(String test, String explanation) {
        if (test.equals("transitive")) {
            if (explanation.equals("trace-based")) { 
                runTraceBasedExplanationTestTransitive();
            } else if (explanation.equals("contextual")) { 
                runContextualExplanationTestTransitive();
            } else {
                logSection("INVALID EXPLANATION/TEST: " + test + ", " + explanation);
            }
        } else if (test.equals("food-recommendation")) {
            if (explanation.equals("trace-based")) { 
                runTraceBasedExplanationTestFoodRecommendation();
            } else if (explanation.equals("contextual")) {
                runContextualExplanationTestFoodRecommendation();
            } else if (explanation.equals("contrastive")) { 
                runContrastiveExplanationTestFoodRecommendation();
            } else if (explanation.equals("counterfactual")) { 
                runCounterfactualExplanationTestFoodRecommendation();
            } else {
                logSection("INVALID EXPLANATION/TEST: " + test + ", " + explanation);
            }
        } else if (test.equals("loan-eligibility")) {
            if (explanation.equals("trace-based")) {
                runTraceBasedExplanationTestLoanEligibility();
            } else if (explanation.equals("contextual")) {
                runContextualExplanationTestLoanEligibility();
            } else if (explanation.equals("contrastive")) {
                runContrastiveExplanationTestLoanEligibility();
            } else if (explanation.equals("counterfactual")) {
                runCounterfactualExplanationTestLoanEligibility();
            } else {
                logSection("INVALID EXPLANATION/TEST: " + test + ", " + explanation);
            }
        } else {
            logSection("INVALID EXPLANATION/TEST: " + test + ", " + explanation);
        }
    }


    public static void run () {
        // Create model
        PrintUtil.registerPrefix("ex", ModelFactory.getGlobalURI());

        // Trace-Based Explanations
        // runExplanationTest("transitive", "trace-based");
        // runExplanationTest("food-recommendation", "trace-based");
        runExplanationTest("loan-eligibility", "trace-based");

        // Contextual Explanations
        // runExplanationTest("transitive", "contextual");
        // runExplanationTest("food-recommendation", "contextual");
        runExplanationTest("loan-eligibility", "contextual");

        // Contrastive Explanations
        // runExplanationTest("transitive", "contrastive");
        // runExplanationTest("food-recommendation", "contrastive");
        runExplanationTest("loan-eligibility", "contrastive");
        
        // Counterfactual Explanations
        // runExplanationTest("transitive", "counterfactual");
        // runExplanationTest("food-recommendation", "counterfactual");
        runExplanationTest("loan-eligibility", "counterfactual");     
    }
}