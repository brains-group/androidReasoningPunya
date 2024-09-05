package wvw.mobile.rules.explanation;

import android.util.Log;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.datatypes.TypeMapper;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.reasoner.Derivation;
import com.hp.hpl.jena.reasoner.rulesys.RuleDerivation;
import com.hp.hpl.jena.util.PrintUtil;

public class ExplanationRunner {

    public static void print(String message) {
        Log.d("Explanation-Runner", message);
    }


    public static void runContextualExplanationTest(){
        // Set-up rules
        String rules = "[transitiveRule: (?a ex:equals ?b) (?b ex:equals ?c) -> (?a ex:equals ?c)]";

        // Create and set-up the Explainer
        Explainer explainer = new Explainer();
        explainer.Model(ModelFactory.getTransitiveBaseModel());
        explainer.Rules(rules);

        // Generate the contextual explanation.
        String results = "Transitive_Explainer -- ContextualExplanation\n";
        results += explainer.GetShallowContextualExplanation(
                explainer.Model().getResource("A"),
                explainer.Model().getProperty("http://example.com/equals"),
                explainer.Model().getResource("D")
        );

        results += "\n";

        results += explainer.GetSimpleContextualExplanation(
                explainer.Model().getResource("A"),
                explainer.Model().getProperty("http://example.com/equals"),
                explainer.Model().getResource("D")
        );

        print(results + "\n");
    }

    public static void runCounterfactualExplanationTest(){

        // Set-up the Explainer
        Explainer explainer2 = new Explainer();
        explainer2.Model(ModelFactory.getAIMEBaseModel());
        explainer2.Rules(ModelFactory.getAIMERules());

        // Set-up the Additional Model needed to run the counterfactual explanation
        InfModel infModel = ModelFactory.getAIMEInfModel();

        Resource person  = infModel.getResource(ModelFactory.getPersonURI());
        Property totalSugars = infModel.getProperty("http://example.com/totalSugars");

        StmtIterator itr = infModel.listStatements(person, totalSugars, (RDFNode) null);

        // Use the Explainer to generate a counterfactual explanation.
        String res = "AIME_Explainer -- CounterfactualExplanation\n";
        while(itr.hasNext()) {
            Statement s = itr.next();
            res += explainer2.GetFullCounterfactualExplanation(s, ModelFactory.getAIMEBaseModelBanana());
        }
        print(res);
    }

    public static void runTraceBasedExplanationTest(){

        // Set up the Explainer
        String rules = "[transitiveRule: (?a ex:equals ?b) (?b ex:equals ?c) -> (?a ex:equals ?c)]";
        Explainer explainer = new Explainer();
        explainer.Model(ModelFactory.getTransitiveBaseModel());
        explainer.Rules(rules);

        // Demonstrate a trace-based explanation.
        String traceResponse = explainer.GetFullTracedBaseExplanation(explainer.Model().getResource("A"),
                explainer.Model().getProperty("http://example.com/equals"),
                explainer.Model().getResource("D"));
        print(traceResponse);
    }


    // ==================================================================================
    // ==========================     capuzb Contributions     ==========================
    // ==================================================================================

    public static void runTraceBasedExplanationTestAIME(){

        print("Running Trace-Based Explanation Test on AIME...");

        // Set up the Explainer
        Explainer explainer = new Explainer();
        explainer.Model(ModelFactory.getAIMEBaseModel());
        explainer.Rules(ModelFactory.getAIMERules());
        // explainer.Model().createTypedLiteral( (Double)20.78).getValue();
        // XSDDatatype xsdDouble = XSDDatatype.XSDdouble;
        TypeMapper tm = TypeMapper.getInstance();
        RDFDatatype xsdDouble = tm.getTypeByName("http://www.w3.org/2001/XMLSchema#double");

        // Demonstrate a trace-based explanation.
        String traceResponse = explainer.GetFullTracedBaseExplanation_B(
                explainer.Model().getResource("http://xmlns.com/foaf/0.1/Person"),
                explainer.Model().getProperty("http://example.com/totalSugars"),
                // null,
                // explainer.Model().getResource("20.78^^http://www.w3.org/2001/XMLSchema#double"));
                // explainer.Model().getResource( explainer.Model().createTypedLiteral( "20.78", RDFDatatype ).getString() ) );
                ResourceFactory.createTypedLiteral("20.78", xsdDouble));
                // null);
        print(traceResponse);
    }

    public static void runContextualExplanationTestAIME(){

        print("Running Contextual Explanation Test on AIME...");

        // Create and set-up the Explainer
        Explainer explainer = new Explainer();
        explainer.Model(ModelFactory.getAIMEBaseModel());
        explainer.Rules(ModelFactory.getAIMERules());

        TypeMapper tm = TypeMapper.getInstance();
        RDFDatatype xsdDouble = tm.getTypeByName("http://www.w3.org/2001/XMLSchema#double");

        // Generate the contextual explanation.
        String results = "AIME_Explainer -- ContextualExplanation\n";
        results += explainer.GetShallowContextualExplanation(
                explainer.Model().getResource("http://xmlns.com/foaf/0.1/Person"),
                explainer.Model().getProperty("http://example.com/totalSugars"),
                // explainer.Model().getResource("\"20.78\"^^http://www.w3.org/2001/XMLSchema#double")
                ResourceFactory.createTypedLiteral("20.78", xsdDouble)
        );

        results += "\n";

        results += explainer.GetSimpleContextualExplanation(
                explainer.Model().getResource("http://xmlns.com/foaf/0.1/Person"),
                explainer.Model().getProperty("http://example.com/totalSugars"),
                // explainer.Model().getResource("\"20.78\"^^http://www.w3.org/2001/XMLSchema#double")
                ResourceFactory.createTypedLiteral("20.78", xsdDouble)
        );

        print(results + "\n");

        // TODO: Figure out why this isn't working
    }

    public static void runCounterfactualExplanationTestAIME(){

        print("Running Counterfactual Explanation Test on AIME...");

        // Set-up the Explainer
        Explainer explainer2 = new Explainer();
        explainer2.Model(ModelFactory.getAIMEBaseModel());
        explainer2.Rules(ModelFactory.getAIMERules());

        // Set-up the Additional Model needed to run the counterfactual explanation
        InfModel infModel = ModelFactory.getAIMEInfModel();

        Resource person  = infModel.getResource(ModelFactory.getPersonURI());
        Property totalSugars = infModel.getProperty("http://example.com/totalSugars");

        StmtIterator itr = infModel.listStatements(person, totalSugars, (RDFNode) null);

        // Use the Explainer to generate a counterfactual explanation.
        String result = "AIME_Explainer -- CounterfactualExplanation\n";
        while(itr.hasNext()) {
            Statement s = itr.next();
            result += explainer2.GetFullCounterfactualExplanation_B(s, ModelFactory.getAIMEBaseModelBanana());
        }
        print(result);
    }

    public static void runContrastiveExplanationTestAIME(){

        print("Running Contrastive Explanation Test on AIME...");

        // TODO
    }

    // ==================================================================================
    // ==========================     End of Contributions     ==========================
    // ==================================================================================

    public static void run () {
        // Create model...
        PrintUtil.registerPrefix("ex", ModelFactory.getGlobalURI());

        print(ModelFactory.getAIMEBaseModel().toString());
        print(ModelFactory.getAIMERules());

        runContextualExplanationTest();
        runCounterfactualExplanationTest();
        runTraceBasedExplanationTest();
        // Brendan's Tests
        runTraceBasedExplanationTestAIME();
        runContextualExplanationTestAIME();
        runCounterfactualExplanationTestAIME();
        runContrastiveExplanationTestAIME();
    }
}