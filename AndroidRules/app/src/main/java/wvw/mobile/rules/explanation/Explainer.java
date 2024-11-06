package wvw.mobile.rules.explanation;

import com.hp.hpl.jena.rdf.model.*;
import wvw.mobile.rules.explanation.types.*;

public class Explainer {
    private BaseRDFModel rdfModel;
    private ExplanationType explanationType;
    private ExplanationContext context;

    public void setModel(BaseRDFModel model) {
        this.rdfModel = model;
        this.context = new ExplanationContext(model);
        
        // Initialize the model
        model.createBaseModel();
        model.createRules();
        model.createInfModel();
    }

    public void setExplanationType(ExplanationType type) {
        this.explanationType = type;
    }

    public void setExplanationParameters(Resource subject, Property predicate, RDFNode object) {
        if (context == null) {
            throw new IllegalStateException("Must set model before setting parameters");
        }
        context.setSubject(subject);
        context.setPredicate(predicate);
        context.setObject(object);
    }

    public void setAlternativeModel(Model alternativeModel) {
        if (context == null) {
            throw new IllegalStateException("Must set model before setting alternative model");
        }
        context.setAlternativeModel(alternativeModel);
    }

    public String explain() {
        if (rdfModel == null || explanationType == null || context == null) {
            throw new IllegalStateException("Must set model and explanation type before explaining");
        }
        return explanationType.explain(context);
    }

    // Getters for testing/debugging
    public Model getModel() {
        return rdfModel.getModel();
    }

    public InfModel getInfModel() {
        return rdfModel.getInfModel();
    }

    public String getRules() {
        return rdfModel.getRules();
    }
}
