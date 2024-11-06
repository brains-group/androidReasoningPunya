package wvw.mobile.rules.explanation;

// Jena Model imports
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.vocabulary.RDF;

// Jena Reasoner imports
import com.hp.hpl.jena.reasoner.Reasoner;
import com.hp.hpl.jena.reasoner.rulesys.GenericRuleReasoner;
import com.hp.hpl.jena.reasoner.rulesys.Rule;

// Java imports
import java.math.BigDecimal;

public class FoodRecommendationModel extends BaseRDFModel {
    private static final String ATE_URI = EX_URI + "ate";
    private static final String WEIGHT_URI = SCHEMA_URI + "weight";
    private static final String USDA_URI = "http://idea.rpi.edu/heals/kb/usda-ontology#";
    
    @Override
    public Model createBaseModel() {
        // Implementation from ModelFactory.getFoodRecommendationBaseModel()
        model = ModelFactory.createDefaultModel();
        
        // Create the resources (copied from ModelFactory.getFoodRecommendationBaseModel())
        Resource user = model.createResource(FOAF_URI + "Person");
        Resource observation = model.createResource(SCHEMA_URI + "Observation");
        Resource usdaFood = model.createResource(USDA_URI + "09003"); // Apple
        
        // Add properties
        user.addProperty(model.createProperty(ATE_URI), observation);
        user.addProperty(model.createProperty(RDF_URI + "type"), user);
        
        observation.addLiteral(model.createProperty(WEIGHT_URI), 
            model.createTypedLiteral(new BigDecimal(118))); // xsd:decimal
        observation.addProperty(model.createProperty(SCHEMA_URI + "unitText"), "g");
        observation.addProperty(model.createProperty(SCHEMA_URI + "variableMeasured"), usdaFood);
        
        usdaFood.addLiteral(model.createProperty(USDA_URI + "sugar"), 
            model.createTypedLiteral(new BigDecimal(10.39)));
            
        return model;
    }
    
    @Override
    public String createRules() {
        rules = "[rule1: (?obs schema:weight ?weight) (?obs schema:variableMeasured ?food)" +
               "(?food usda:sugar ?sugar) multiply(?weight, ?sugar, ?totalSugar) -> " +
               "(?person ex:totalSugars ?totalSugar)]";
        return rules;
    }
    
    @Override
    public InfModel createInfModel() {
        if (model == null) {
            createBaseModel();
        }
        if (rules == null) {
            createRules();
        }
        
        Reasoner reasoner = new GenericRuleReasoner(Rule.parseRules(rules));
        reasoner.setDerivationLogging(true);
        infModel = ModelFactory.createInfModel(reasoner, model);
        return infModel;
    }
    
    @Override
    public Model createAlternativeModel() {
        Model altModel = ModelFactory.createDefaultModel();
        
        Resource user = altModel.createResource(FOAF_URI + "Person");
        Resource observation = altModel.createResource(SCHEMA_URI + "Observation");
        Resource usdaFood = altModel.createResource(USDA_URI + "09040"); // Banana
        
        user.addProperty(altModel.createProperty(ATE_URI), observation);
        user.addProperty(altModel.createProperty(RDF_URI + "type"), user);
        
        observation.addLiteral(altModel.createProperty(WEIGHT_URI), 
            altModel.createTypedLiteral(new BigDecimal(118)));
        observation.addProperty(altModel.createProperty(SCHEMA_URI + "unitText"), "g");
        observation.addProperty(altModel.createProperty(SCHEMA_URI + "variableMeasured"), usdaFood);
        
        usdaFood.addLiteral(altModel.createProperty(USDA_URI + "sugar"), 
            altModel.createTypedLiteral(new BigDecimal(12.2)));
            
        return altModel;
    }
} 