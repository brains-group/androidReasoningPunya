package wvw.mobile.rules.explanation;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.reasoner.Reasoner;
import com.hp.hpl.jena.reasoner.rulesys.GenericRuleReasoner;
import com.hp.hpl.jena.reasoner.rulesys.Rule;
import com.hp.hpl.jena.vocabulary.RDF;
import wvw.mobile.rules.explanation.utils.URIConstants;

public abstract class RDFKnowledgeBase {
    protected Model model;
    protected String rules;
    protected InfModel infModel;
    
    // Initialize the knowledge base
    public void initialize() {
        model = createBaseModel();
        rules = createRules();
        infModel = createInfModel();
    }
    
    // Abstract methods that must be implemented by concrete models
    protected abstract Model createBaseModel();
    protected abstract String createRules();
    protected abstract Model createAlternativeModel();
    
    // Common implementation for inference model creation
    protected InfModel createInfModel() {
        if (model == null || rules == null) {
            throw new IllegalStateException("Base model and rules must be created before inference model");
        }
        
        Reasoner reasoner = new GenericRuleReasoner(Rule.parseRules(rules));
        reasoner.setDerivationLogging(true);
        return ModelFactory.createInfModel(reasoner, model);
    }
    
    // Getter methods
    public Model getModel() {
        return model;
    }
    
    public String getRules() {
        return rules;
    }
    
    public InfModel getInfModel() {
        return infModel;
    }
    
    // Validation methods
    protected void validateModel() {
        if (model == null) {
            throw new IllegalStateException("Model has not been initialized");
        }
    }
    
    protected void validateRules() {
        if (rules == null) {
            throw new IllegalStateException("Rules have not been initialized");
        }
    }
    
    protected void validateInfModel() {
        if (infModel == null) {
            throw new IllegalStateException("Inference model has not been initialized");
        }
    }
} 