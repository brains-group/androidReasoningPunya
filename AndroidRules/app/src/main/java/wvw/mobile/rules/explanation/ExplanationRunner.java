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

    public static String getExplanation(String model, String explanationType) {
        clearOutput();
        
        if (model.equals("loan-eligibility")) {
            switch (explanationType) {
                case "trace-based":
                    return runLoanEligibilityExplanation("trace-based");
                case "contextual":
                    return runLoanEligibilityExplanation("contextual");
                case "contrastive":
                    return runLoanEligibilityExplanation("contrastive");
                case "counterfactual":
                    return runLoanEligibilityExplanation("counterfactual");
                default:
                    return "Invalid explanation type selected";
            }
        }
        return "Invalid model selected";
    }

    private static String runLoanEligibilityExplanation(String type) {
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
            
            Resource applicant1 = explainer.Model().getResource("http://example.com/applicant1");
            Property loanEligibility = explainer.Model().getProperty("http://example.com/loanEligibility");
            
            InfModel infModel = ModelFactory.getLoanEligibilityInfModel();
            StmtIterator itr = infModel.listStatements(applicant1, loanEligibility, 
                ResourceFactory.createPlainLiteral("Not Eligible"));
            
            if (!itr.hasNext()) {
                return "No eligible statements found";
            }
            
            Statement s = itr.next();
            switch (type) {
                case "trace-based":
                    return explainer.GetFullTraceBasedExplanation(
                        applicant1,
                        loanEligibility,
                        ResourceFactory.createPlainLiteral("Not Eligible")
                    );
                case "contextual":
                    return explainer.GetShallowContextualExplanation(
                        applicant1,
                        loanEligibility,
                        ResourceFactory.createPlainLiteral("Not Eligible")
                    );
                case "contrastive":
                    return explainer.GetFullContrastiveExplanation_B(
                        s,
                        ModelFactory.getLoanEligibilityBaseModelSecondType()
                    );
                case "counterfactual":
                    return explainer.GetCounterfactualExplanation(s);
                default:
                    return "Invalid explanation type";
            }
        } catch (Exception e) {
            return "Error generating explanation: " + e.getMessage() + "\n" + e.getStackTrace()[0].toString();
        }
    }
}