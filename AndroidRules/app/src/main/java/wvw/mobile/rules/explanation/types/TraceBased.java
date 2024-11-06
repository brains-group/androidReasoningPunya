package wvw.mobile.rules.explanation.types;

import android.util.Log;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.reasoner.Derivation;
import com.hp.hpl.jena.reasoner.rulesys.RuleDerivation;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import wvw.mobile.rules.explanation.utils.StatementUtils;

public class TraceBased implements ExplanationType {
    private static final String TAG = "TraceBased";

    @Override
    public String explain(ExplanationContext context) {
        StringBuilder explanation = new StringBuilder();
        
        explanation.append(generateTraceBasedExplanation(
            context.getModel(),
            context.getInfModel(),
            context.getSubject(),
            context.getPredicate(),
            context.getObject()
        ));

        return explanation.toString();
    }

    private String generateTraceBasedExplanation(Model baseModel, InfModel inf,
                                               Resource subject, Property predicate, RDFNode object) {
        Log.d(TAG, "\tGenerating Trace-Based Explanation...\n\t\t(" + 
              subject + ", " + predicate + ", " + object + ")");

        String answer = "";
        StmtIterator stmtItr = inf.listStatements(subject, predicate, object);

        // Debug
        Log.d(TAG, "Base Model: " + baseModel.toString() + 
              "\nInf Model: " + inf.toString() + 
              "\nTriple: (" + subject + ", " + predicate + ", " + object + ")");

        while (stmtItr.hasNext()) {
            Statement s = stmtItr.next();
            answer += traceDerivation(inf, baseModel, s, 0) + "\n\n";
        }
        return answer;
    }

    private String traceDerivation(InfModel infModel, Model baseModel, 
                                 Statement statement, int tabs) {
        String results = "";

        // Find the triples (matches) and rule that was used to
        // assert this statement, if it exists in the infModel.
        Iterator<Derivation> derivItr = infModel.getDerivation(statement);
        
        while(derivItr.hasNext()) {
            RuleDerivation derivation = (RuleDerivation) derivItr.next();
            Triple conclusion = derivation.getConclusion();
            
            results += (tabOffset(tabs) + "Conclusion: " + 
                       StatementUtils.describeTriple(conclusion) + 
                       " used the following matches: \n");

            // Process matches
            for (Triple match : derivation.getMatches()) {
                results += processMatch(match, baseModel, infModel, tabs);
            }

            // Add rule information
            results += tabOffset(tabs) + 
                      "And paired them with the following rule: \n";
            results += tabOffset(tabs) + derivation.getRule().toString() + "\n";
            results += tabOffset(tabs) + "to reach this conclusion.\n";
        }
        return results;
    }

    private String processMatch(Triple match, Model baseModel, 
                              InfModel infModel, int tabs) {
        String results = "";
        Statement matchStatement = createMatchStatement(match);

        if (matchStatement != null) {
            // If the statement is in base model
            if (baseModel.contains(matchStatement)) {
                results += tabOffset(tabs) + " Match: " + 
                          StatementUtils.describeStatement(matchStatement) + "\n";
            }

            // If the statement was derived
            if (!baseModel.contains(matchStatement)) {
                results += tabOffset(tabs) + " Match: " + 
                          StatementUtils.describeStatement(matchStatement) + "\n";
                results += traceDerivation(infModel, baseModel, 
                                         matchStatement, tabs + 1) + "\n";
            }
        }
        return results;
    }

    private Statement createMatchStatement(Triple match) {
        Resource matchResource = ResourceFactory.createResource(
            match.getSubject().getURI());
        Property matchProperty = ResourceFactory.createProperty(
            match.getPredicate().getURI());
        Node obj = match.getObject();

        if (!obj.isLiteral()) {
            Resource matchObject = ResourceFactory.createResource(
                match.getObject().getURI());
            return ResourceFactory.createStatement(
                matchResource, matchProperty, matchObject);
        } else {
            Literal l = ResourceFactory.createTypedLiteral(
                obj.getLiteralValue().toString(), 
                obj.getLiteralDatatype());
            return ResourceFactory.createStatement(
                matchResource, matchProperty, l);
        }
    }

    private String tabOffset(int tabs) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tabs; i++) {
            sb.append("\t");
        }
        return sb.toString();
    }
}
