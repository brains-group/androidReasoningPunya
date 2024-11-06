package wvw.mobile.rules.explanation.utils;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.reasoner.Reasoner;
import com.hp.hpl.jena.reasoner.rulesys.GenericRuleReasoner;
import com.hp.hpl.jena.reasoner.rulesys.Rule;

public class StatementUtils {
    
    public static Statement generateStatement(Triple triple, Model model) {
        return model.createStatement(
            model.createResource(triple.getSubject().toString()),
            model.createProperty(triple.getPredicate().toString()),
            model.createResource(triple.getObject().toString())
        );
    }

    public static String describeTriple(Triple triple) {
        // String subject = triple.getSubject().toString();
        String subjectURI = triple.getSubject().getURI();
        String[] subjectParts = subjectURI.split("/");
        String subject = subjectParts[subjectParts.length - 1];

        // String predicate = triple.getPredicate().toString();
        String predicateURI = triple.getPredicate().getURI();
        String[] predicateParts = predicateURI.split("/");
        String predicate = predicateParts[predicateParts.length - 1];

        String object;
        if (triple.getObject().isLiteral()) {
            String literalValue = triple.getObject().getLiteral().toString();
            String[] objectParts = literalValue.split("\\^\\^");
            // Return the first part which contains the numeric value
            object = objectParts[0];
        } else {
            // object = triple.getObject().toString();
            String objectURI = triple.getObject().getURI();
            String[] objectParts = objectURI.split("/");
            object = objectParts[objectParts.length - 1];
        }
        return "Subject: " + subject + " , Predicate: " + predicate + ", Object: " + object;
    }

    public static String describeStatement(Statement statement) {
        String subjectURI = statement.getSubject().getURI();
        String[] subjectParts = subjectURI.split("/");
        String subject = subjectParts[subjectParts.length - 1];

        String predicateURI = statement.getPredicate().getURI();
        String[] predicateParts = predicateURI.split("/");
        String predicate = predicateParts[predicateParts.length - 1];

        String object;
        String literalValue;
        RDFNode objectNode = statement.getObject();
        if (objectNode.isLiteral()) {
            literalValue = objectNode.toString();
            String[] objectParts = literalValue.split("\\^\\^");
            // Return the first part which contains the numeric value
            object = objectParts[0];
        } else if (objectNode instanceof Resource) {
            Resource resource = (Resource) objectNode;
            if (resource.isURIResource()) {
                String objectURI = resource.getURI();
                String[] objectParts = objectURI.split("/");
                object = objectParts[objectParts.length - 1];
            } else {
                // Handle blank nodes or other resource types as needed
                object = resource.toString();
            }
        } else {
            // Handle other types of nodes if necessary
            object = objectNode.toString();
        }
        return "Subject: " + subject + ", Predicate: " + predicate + ", Object: " + object;
    }

    public static InfModel generateInfModel(Model baseModel, String rules) {
        Reasoner reasoner = new GenericRuleReasoner(Rule.parseRules(rules));
        reasoner.setDerivationLogging(true);
        return ModelFactory.createInfModel(reasoner, baseModel);
    }
} 