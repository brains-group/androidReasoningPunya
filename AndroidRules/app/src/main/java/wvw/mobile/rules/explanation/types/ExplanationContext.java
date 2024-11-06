package wvw.mobile.rules.explanation.types;

import com.hp.hpl.jena.rdf.model.*;
import wvw.mobile.rules.explanation.BaseRDFModel;

public class ExplanationContext {
    private BaseRDFModel rdfModel;
    private Resource subject;
    private Property predicate;
    private RDFNode object;
    private Model alternativeModel;

    public ExplanationContext(BaseRDFModel model) {
        this.rdfModel = model;
    }

    // Getters and setters
    public BaseRDFModel getRdfModel() { return rdfModel; }
    public Resource getSubject() { return subject; }
    public Property getPredicate() { return predicate; }
    public RDFNode getObject() { return object; }
    public Model getAlternativeModel() { return alternativeModel; }
    public Model getModel() { return rdfModel.getModel(); }
    public InfModel getInfModel() { return rdfModel.getInfModel(); }

    public void setSubject(Resource subject) { this.subject = subject; }
    public void setPredicate(Property predicate) { this.predicate = predicate; }
    public void setObject(RDFNode object) { this.object = object; }
    public void setAlternativeModel(Model model) { this.alternativeModel = model; }
} 