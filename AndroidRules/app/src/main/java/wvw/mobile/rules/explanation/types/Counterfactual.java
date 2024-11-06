package wvw.mobile.rules.explanation.types;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.reasoner.Derivation;
import com.hp.hpl.jena.reasoner.rulesys.RuleDerivation;
import com.hp.hpl.jena.graph.Triple;
import wvw.mobile.rules.explanation.utils.StatementUtils;

public class Counterfactual implements ExplanationType {
    
    @Override
    public String explain(ExplanationContext context) {
        Statement statement = context.getInfModel().createStatement(
            context.getSubject(),
            context.getPredicate(),
            context.getObject()
        );
        return generateCounterfactualExplanation(statement, context);
    }

    private String generateCounterfactualExplanation(Statement statement, ExplanationContext context) {
        InfModel thisInfModel = context.getInfModel();
        InfModel otherInfModel = StatementUtils.generateInfModel(context.getAlternativeModel(), context.getRdfModel().getRules());
        String results = "";

        StmtIterator itr = thisInfModel.listStatements(statement.getSubject(), statement.getPredicate(), (RDFNode) null);
        StmtIterator itr2 = otherInfModel.listStatements(statement.getSubject(), statement.getPredicate(), (RDFNode) null);

        Iterator<Derivation> thisDerivItr = thisInfModel.getDerivation(statement);
        Iterator<Derivation> otherDerivItr = otherInfModel.getDerivation(statement);

        while (thisDerivItr.hasNext()) {
            RuleDerivation thisDerivation = (RuleDerivation) thisDerivItr.next();
            RuleDerivation otherDerivation = null;

            if (otherDerivItr.hasNext()) {
                otherDerivation = (RuleDerivation) otherDerivItr.next();
            } else if (itr2.hasNext()) {
                Statement otherMatch = itr2.next();
                otherDerivItr = otherInfModel.getDerivation(otherMatch);
                otherDerivation = (RuleDerivation) otherDerivItr.next();
            }

            results += compareDerivations(thisDerivation, otherDerivation, context);
        }
        return results;
    }

    private String compareDerivations(RuleDerivation thisDerivation, RuleDerivation otherDerivation, ExplanationContext context) {
        String results = "";
        Triple thisConclusion = thisDerivation.getConclusion();
        Triple otherConclusion = otherDerivation != null ? otherDerivation.getConclusion() : null;

        if (otherConclusion == null) {
            results += "This model concluded: " + StatementUtils.describeTriple(thisConclusion) + "\n";
            results += "Alternate model didn't conclude anything.\n";
            return results;
        }

        if (thisConclusion.sameAs(otherConclusion.getSubject(), otherConclusion.getPredicate(), otherConclusion.getObject())) {
            results += handleMatchingConclusions(thisDerivation, context);
        } else {
            results += handleDifferentConclusions(thisDerivation, otherDerivation, context);
        }

        return results;
    }

    private String handleMatchingConclusions(RuleDerivation thisDerivation, ExplanationContext context) {
        String results = "Both model concluded: " + StatementUtils.describeTriple(thisDerivation.getConclusion()) + "\n";
        for (Triple match : thisDerivation.getMatches()) {
            Statement matchStatement = StatementUtils.generateStatement(match, context.getModel());
            results += generateCounterfactualExplanation(matchStatement, context) + "\n";
        }
        return results;
    }

    private String handleDifferentConclusions(RuleDerivation thisDerivation, RuleDerivation otherDerivation, ExplanationContext context) {
        String results = "This model concluded: " + StatementUtils.describeTriple(thisDerivation.getConclusion()) + " using Matches: \n";
        
        for (Triple match : thisDerivation.getMatches()) {
            Statement matchStatement = StatementUtils.generateStatement(match, context.getModel());
            results += "  " + StatementUtils.describeStatement(matchStatement) + "\n";
        }

        results += "Alternate model concluded: " + StatementUtils.describeTriple(otherDerivation.getConclusion()) + " instead using Matches: \n";
        
        for (Triple match : otherDerivation.getMatches()) {
            Statement matchStatement = StatementUtils.generateStatement(match, context.getModel());
            results += "  " + StatementUtils.describeStatement(matchStatement) + "\n";
        }

        for (Triple match : thisDerivation.getMatches()) {
            Statement matchStatement = StatementUtils.generateStatement(match, context.getModel());
            if (!context.getModel().contains(matchStatement)) {
                results += generateCounterfactualExplanation(matchStatement, context) + "\n";
            }
        }

        return results;
    }
}
