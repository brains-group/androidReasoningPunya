package wvw.mobile.rules.explanation.types;

import android.util.Log;
import com.hp.hpl.jena.rdf.model.*;
import wvw.mobile.rules.explanation.utils.StatementUtils;

public class Contextual implements ExplanationType {
    private static final String TAG = "Contextual";

    @Override
    public String explain(ExplanationContext context) {
        // Combine both shallow and simple contextual explanations
        StringBuilder explanation = new StringBuilder();
        
        explanation.append(getShallowContextualExplanation(context));
        explanation.append("\n\n");
        explanation.append(getSimpleContextualExplanation(context));
        
        return explanation.toString();
    }

    /**
     * Produces a single-sentence contextual explanation as to how the inputted statement
     * was derived by a reasoner.
     */
    private String getSimpleContextualExplanation(ExplanationContext context) {
        Log.d(TAG, "\tGenerating Simple Contextual Explanation for (" + 
              context.getSubject() + ", " + context.getPredicate() + ", " + 
              context.getObject() + ")");

        StringBuilder explanation = new StringBuilder();
        InfModel model = context.getInfModel();
        
        Log.d(TAG, "\t\tGetSimpleContextualExplanation() Model:\n\t\t\t" + model);

        StmtIterator itr = model.listStatements(
            context.getSubject(), 
            context.getPredicate(), 
            context.getObject()
        );

        while(itr.hasNext()) {
            explanation.append(generateSimpleContextualExplanation(itr.next(), model));
            explanation.append("\n\n");
        }
        return explanation.toString();
    }

    /**
     * Produces a brief user-readable contextual explanation of how the inputted statement was
     * concluded. Based on the Contextual Ontology:
     * https://tetherless-world.github.io/explanation-ontology/modeling/#casebased/
     */
    private String getShallowContextualExplanation(ExplanationContext context) {
        Log.d(TAG, "\tGenerating Shallow Contextual Explanation for (" + 
              context.getSubject() + ", " + context.getPredicate() + ", " + 
              context.getObject() + ")");

        StringBuilder explanation = new StringBuilder();
        InfModel model = context.getInfModel();
        
        Log.d(TAG, "\t\tGetShallowContextualExplanation() Model:\n\t\t\t" + model);
        
        StmtIterator itr = model.listStatements(
            context.getSubject(), 
            context.getPredicate(), 
            context.getObject()
        );

        // Append all explanations to the results.
        while(itr.hasNext()) {
            explanation.append(generateShallowTrace(itr.next(), model));
        }

        return explanation.toString();
    }

    /**
     * Helper method to generate a simple contextual explanation for a statement
     */
    private String generateSimpleContextualExplanation(Statement statement, InfModel model) {
        StringBuilder explanation = new StringBuilder("");

        Iterator<Derivation> itr = model.getDerivation(s);

        while(itr.hasNext()){
            RuleDerivation derivation = (RuleDerivation) itr.next();
            explanation.append(derivation.getConclusion().toString());
            explanation.append(" because ");

            List<Triple> matches = derivation.getMatches();
            int matchIndex = 0;
            for (Triple match : matches){
                Statement binding = generateStatement(match);
                explanation.append(binding.getSubject().toString());
                explanation.append(" ");
                explanation.append(binding.getPredicate().toString());
                explanation.append( " ");
                explanation.append(binding.getObject().toString());
                if (matchIndex < matches.size()-1){
                    explanation.append(", ");
                }

                matchIndex++;
            }

        }
        explanation.append(".");
        return explanation.toString();
    }

    /**
     * Helper method to generate a shallow trace for a statement
     */
    private String generateShallowTrace(Statement statement, InfModel model) {
        StringBuilder explanation = new StringBuilder("(");

        Iterator<Derivation> itr = model.getDerivation(s);

        while(itr.hasNext()){
            RuleDerivation derivation = (RuleDerivation) itr.next();
            explanation.append(derivation.getConclusion().toString());
            explanation.append("\n");
            explanation.append("( is based on rule ");
            // Print the rule name:
            explanation.append(derivation.getRule().toShortString());
            explanation.append("\n");

            explanation.append("and is in relation to the following situation: \n");
            for (Triple match : derivation.getMatches()){
                Statement binding = generateStatement(match);
                explanation.append(binding.toString());
                explanation.append("\n");

            }

        }

        explanation.append(")");
        return explanation.toString();
    }
}
