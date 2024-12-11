package wvw.mobile.rules.explanation;

// Java Standard Libraries
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;

// Apache Jena Libraries
import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.reasoner.Reasoner;
import com.hp.hpl.jena.reasoner.rulesys.GenericRuleReasoner;
import com.hp.hpl.jena.reasoner.rulesys.Rule;
import com.hp.hpl.jena.util.PrintUtil;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;

// Android Libraries
import android.content.Context;
import android.content.res.AssetManager;


public class ModelFactory {

    // ============================================================================
    // Shared Resources and URIs
    // ===========================================================================

    // Global URI
    private static String ex = "http://example.com/";
    private static String schemaURI = "http://schema.org/";
    private static String rdfURI = RDF.getURI();
    private static String foaf = "http://xmlns.com/foaf/0.1/";

    // Commonly Used URIs
    private static String personURI = foaf + "Person";
    private static String observationURI = schemaURI + "Observation";
    private static String valueURI = schemaURI + "value";
    private static String unitURI = schemaURI + "unitText";
    private static String nameURI = ex + "name";

    // ============================================================================
    // Food Recommendation Model Resources
    // ============================================================================

    private static String ateURI      = ex + "ate";
    private static String weightURI = schemaURI + "weight";
    private static String variableMeasuredURI = schemaURI + "variableMeasured";
    private static String usdaURI = "http://idea.rpi.edu/heals/kb/usda-ontology#";

    // ============================================================================
    // Loan Eligibility Model Resources
    // ============================================================================

    private static String incomeURI = ex + "income";
    private static String debtURI = ex + "debt";
    private static String creditScoreURI = ex + "creditScore";
    private static String loanEligibilityURI = ex + "loanEligibility";

    // ============================================================================
    // Getter Methods for URIs
    // ============================================================================

    public static String getGlobalURI() {return ex;}

    public static String getPersonURI() {return personURI;}

    public static String getObservavtionURI() {return observationURI;}

    public static String getIncomeURI() {return incomeURI;}

    public static String getDebtURI() {return debtURI;}

    public static String getCreditScoreURI() {return creditScoreURI;}

    public static String getLoanEligibilityURI() {return loanEligibilityURI;}

    // ============================================================================
    // Transitive Model Methods
    // ============================================================================

    // Creates a simple transitive model with resources A, B, C, D and property ex:equals   
    public static Model getTransitiveBaseModel() {

        Model model = com.hp.hpl.jena.rdf.model.ModelFactory.createDefaultModel();

        // Constructs RDF
        Resource A  = model.createResource("A");
        Resource B  = model.createResource("B");
        Resource C  = model.createResource("C");
        Resource D  = model.createResource("D");

        Property equals = model.createProperty(ex + "equals");

        A.addProperty(equals, B);
        B.addProperty(equals, C);
        C.addProperty(equals, D);

        return model;
    }

    // Creates an inference model with transitive reasoning over the ex:equals property
    public static InfModel getTransitiveInfModel() {
        Model model = getTransitiveBaseModel();
        PrintUtil.registerPrefix("ex", ex);
        // Create Rules:
        // See https://jena.apache.org/documentation/inference/#RULEsyntax for specifics on rule syntax.
        String rules = "[transitiveRule: (?a ex:equals ?b) (?b ex:equals ?c) -> (?a ex:equals ?c)]";

        // Construct the reasoner and new model to include reasoning over the rules.
        Reasoner reasoner = new GenericRuleReasoner(Rule.parseRules(rules));
        reasoner.setDerivationLogging(true);
        return com.hp.hpl.jena.rdf.model.ModelFactory.createInfModel(reasoner, model);
    }

    // Add this method to the existing Transitive Model Methods section
    public static String getTransitiveRules() {
        // Use the same rule format as in getTransitiveInfModel
        return "[transitiveRule: (?a ex:equals ?b) (?b ex:equals ?c) -> (?a ex:equals ?c)]";
    }



    // ============================================================================
    // Food Recommendation Model Methods
    // ============================================================================

    // Creates the base model for the Food Recommendation model
    public static Model getFoodRecommendationBaseModel() {
        // Creating the model used in FoodRecommendation tutorial with Person, Observe:eat usda:Apple
        Model model = com.hp.hpl.jena.rdf.model.ModelFactory.createDefaultModel();

        // Create the resources
        Resource user = model.createResource(personURI);
        Resource observation = model.createResource(observationURI);
        Resource usdaFood = model.createResource("http://idea.rpi.edu/heals/kb/usda#09003"); // An Apple

        // Create properties
        Property ateProperty = model.createProperty(ateURI);
        Property typeProperty = model.createProperty(rdfURI + "type");
        Property nameProperty = model.createProperty(nameURI); // New property for person's name

        // Create literals
        Literal foodWeight = model.createTypedLiteral(new BigDecimal(200)); // xsd:decimal
        String unitText = "g"; // xsd:string
        String brendanName = "Brendan"; // Name "Brendan"

        // Add statements
        user.addProperty(ateProperty, observation);
        user.addProperty(typeProperty, user);
        user.addProperty(nameProperty, brendanName); // Add name property with value "Brendan" to the person
        observation.addLiteral(model.createProperty(weightURI), foodWeight);
        observation.addProperty(model.createProperty(unitURI), unitText);
        observation.addProperty(model.createProperty(variableMeasuredURI), usdaFood);
        usdaFood.addLiteral(model.createProperty(usdaURI + "sugar"), model.createTypedLiteral(new BigDecimal(10.39)));

        // Set prefix for better printing
        model.setNsPrefix("schema", schemaURI);
        model.setNsPrefix("ex", ex);
        model.setNsPrefix("foaf", foaf);
        model.setNsPrefix("usda", usdaURI);

        return model;
    }

    // Creates the rules for the Food Recommendation model with prefixes
    public static String getFoodRecommendationRules() {
        // Define prefixes as constants to avoid errors
        String schemaPrefix = "http://schema.org/";
        // String usdaPrefix = "http://example.com/usda#";
        String usdaPrefix = "http://idea.rpi.edu/heals/kb/usda-ontology#";
        String exPrefix = "http://example.com/";
        String rdfPrefix = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
        String foafPrefix = "http://xmlns.com/foaf/0.1/";

        String rule1 = "[CalculateSugarContent: ";
        rule1 += "( ?var " + schemaPrefix + "weight ?weight ) ";
        rule1 += "( ?var " + schemaPrefix + "variableMeasured ?foodstuff ) ";
        rule1 += "( ?foodstuff " + usdaPrefix + "sugar ?sugarsPer100g ) ";
        rule1 += "quotient(?weight, '100.0'^^xsd:float, ?scaledWeight) ";
        rule1 += "product(?scaledWeight, ?sugarsPer100g, ?sugars) ";
        rule1 += "-> (?var " + exPrefix + "sugars ?sugars)";
        rule1 += "]";

        String rule2 = "[TrackTotalSugars: ";
        rule2 += "( ?user " + rdfPrefix + "type " + foafPrefix + "Person) ";
        rule2 += "( ?user " + exPrefix + "ate ?food) ";
        rule2 += "( ?food " + exPrefix + "sugars ?sugar) ";
        rule2 += "sum(?sugar, '0.0'^^xsd:float, ?totalSugars) ";
        rule2 += "-> ( ?user " + exPrefix + "totalSugars ?totalSugars ) ";
        rule2 += "]";

        String rule3 = "[AllowFoodUnderLimit: ";
        rule3 += "( ?observation " + exPrefix + "sugars ?sugars ) ";
        rule3 += "lessThan(?sugars, '25.0'^^http://www.w3.org/2001/XMLSchema#float) ";
        rule3 += "-> ( ?observation " + exPrefix + "allowedToEat 'true'^^http://www.w3.org/2001/XMLSchema#boolean ) ";
        rule3 += "]";

        String rule4 = "[RestrictFoodOverLimit: ";
        rule4 += "( ?observation " + exPrefix + "sugars ?sugars ) ";
        rule4 += "greaterThan(?sugars, '25.0'^^http://www.w3.org/2001/XMLSchema#float) ";
        rule4 += "-> ( ?observation " + exPrefix + "allowedToEat 'false'^^http://www.w3.org/2001/XMLSchema#boolean ) ";
        rule4 += "]";

        return rule1 + " " + rule2 + " " + rule3 + " " + rule4;
    }

    // Creates the inference model for the Food Recommendation model
    public static InfModel getFoodRecommendationInfModel() {
        Model baseModel = getFoodRecommendationBaseModel();

        // Create the ruleset from FoodRecommendation tutorial
        PrintUtil.registerPrefix("schema", schemaURI);
        PrintUtil.registerPrefix("usda", usdaURI);
        PrintUtil.registerPrefix("rdf", rdfURI);
        PrintUtil.registerPrefix("ex", ex);
        PrintUtil.registerPrefix("foaf", foaf);

        String rules = getFoodRecommendationRules();

        Reasoner reasoner = new GenericRuleReasoner(Rule.parseRules(rules));

        reasoner.setDerivationLogging(true);
        return com.hp.hpl.jena.rdf.model.ModelFactory.createInfModel(reasoner, baseModel);
    }

    // Creates the base model for the Food Recommendation model with banana
    public static Model getFoodRecommendationBaseModelBanana() {
        // creating the model used in FoodRecommendation tutorial with Person, Observe:eat usda:Apple
        Model model = com.hp.hpl.jena.rdf.model.ModelFactory.createDefaultModel();

        // create the resource
        Resource user = model.createResource(personURI);
        Resource observation = model.createResource(observationURI);
        // Apple usda food, User recored food weight, weight unit
        Resource usdaFood = model.createResource("http://idea.rpi.edu/heals/kb/usda#09040"); // Banana
        Literal foodWeight = model.createTypedLiteral(new BigDecimal(118)); // xsd:decimal
        String unitText   = "g";
        // add the property
        user.addProperty(model.createProperty(ateURI), observation);
        user.addProperty(model.createProperty(rdfURI + "type"), user);

        observation.addLiteral(model.createProperty(weightURI), foodWeight);
        observation.addProperty(model.createProperty(unitURI), unitText);
        observation.addProperty(model.createProperty(variableMeasuredURI), usdaFood);
        usdaFood.addLiteral(model.createProperty(usdaURI + "sugar"), model.createTypedLiteral(new BigDecimal(12.2)));

        // set prefix for better printing
        model.setNsPrefix( "schema", schemaURI );
        model.setNsPrefix( "ex", ex );
        model.setNsPrefix( "foaf", foaf );
        model.setNsPrefix( "usda", usdaURI );
        return model;
    }

    // Creates the inference model for the Food Recommendation model with banana
    public static InfModel getFoodRecommendationInfModelBanana() {
        Model baseModel = getFoodRecommendationBaseModelBanana();

        // Create the ruleset from FoodRecommendation tutorial
        PrintUtil.registerPrefix("schema", schemaURI);
        PrintUtil.registerPrefix("usda", usdaURI);
        PrintUtil.registerPrefix("rdf", rdfURI);
        PrintUtil.registerPrefix("ex", ex);
        PrintUtil.registerPrefix("foaf", foaf);

        String rules = getFoodRecommendationRules();

        Reasoner reasoner = new GenericRuleReasoner(Rule.parseRules(rules));

        reasoner.setDerivationLogging(true);
        InfModel infModel = com.hp.hpl.jena.rdf.model.ModelFactory.createInfModel(reasoner, baseModel);
        return infModel;
    }



    // ============================================================================
    // Loan Eligibility Model Methods
    // ============================================================================

    // Creates the base model for the Loan Eligibility model
    public static Model getLoanEligibilityBaseModel() {
        Model model = com.hp.hpl.jena.rdf.model.ModelFactory.createDefaultModel();
        
        // Create resources for applicants
        Resource applicant1 = model.createResource("http://example.com/applicant1");
        Resource applicant2 = model.createResource("http://example.com/applicant2");
        Resource applicant3 = model.createResource("http://example.com/applicant3");
        Resource personType = model.createResource("http://xmlns.com/foaf/0.1/Person");
        
        // Create properties
        Property type = model.createProperty(rdfURI + "type");
        Property name = model.createProperty(ex + "name");
        Property creditScore = model.createProperty(ex + "creditScore");
        Property monthlyDebt = model.createProperty(schemaURI + "monthlyDebt");
        Property monthlyIncome = model.createProperty(schemaURI + "monthlyIncome");
        
        // Applicant 1 (Alex): High DTI but good credit
        applicant1.addProperty(type, personType)
                 .addProperty(name, "Alex")
                 .addProperty(creditScore, model.createTypedLiteral(680, XSDDatatype.XSDint))
                 .addProperty(monthlyDebt, model.createTypedLiteral(2000.0f, XSDDatatype.XSDfloat))
                 .addProperty(monthlyIncome, model.createTypedLiteral(5000.0f, XSDDatatype.XSDfloat));
        
        // Applicant 2 (Beth): Good DTI but borderline credit
        applicant2.addProperty(type, personType)
                 .addProperty(name, "Beth")
                 .addProperty(creditScore, model.createTypedLiteral(605, XSDDatatype.XSDint))
                 .addProperty(monthlyDebt, model.createTypedLiteral(1500.0f, XSDDatatype.XSDfloat))
                 .addProperty(monthlyIncome, model.createTypedLiteral(5000.0f, XSDDatatype.XSDfloat));
        
        // Applicant 3 (Charlie): Good DTI and good credit
        applicant3.addProperty(type, personType)
                 .addProperty(name, "Charlie")
                 .addProperty(creditScore, model.createTypedLiteral(700, XSDDatatype.XSDint))
                 .addProperty(monthlyDebt, model.createTypedLiteral(1000.0f, XSDDatatype.XSDfloat))
                 .addProperty(monthlyIncome, model.createTypedLiteral(5000.0f, XSDDatatype.XSDfloat));
        
        // Set prefixes for better printing
        model.setNsPrefix("rdf", rdfURI);
        model.setNsPrefix("schema", schemaURI);
        model.setNsPrefix("ex", ex);
        model.setNsPrefix("foaf", "http://xmlns.com/foaf/0.1/");
        model.setNsPrefix("xsd", "http://www.w3.org/2001/XMLSchema#");
        
        return model;
    }

    // Creates the rules for the Loan Eligibility model
    public static String getLoanEligibilityRules() {
        return "[DTIRule: "
            + "(?applicant rdf:type foaf:Person) "
            + "(?applicant schema:monthlyDebt ?debt) "
            + "(?applicant schema:monthlyIncome ?income) "
            + "quotient(?debt, ?income, ?dti) "
            + "-> (?applicant ex:dtiRatio ?dti)]"
            + "\n"
            + "[EligibilityRule: "
            + "(?applicant rdf:type foaf:Person) "
            + "(?applicant ex:dtiRatio ?dti) "
            + "(?applicant ex:creditScore ?score) "
            + "lessThan(?dti, '0.35'^^xsd:double) "
            + "greaterThan(?score, '620'^^xsd:int) "
            + "-> (?applicant ex:loanEligibility 'Eligible')]"
            + "\n"
            + "[NotEligibleDTIRule: "
            + "(?applicant rdf:type foaf:Person) "
            + "(?applicant ex:dtiRatio ?dti) "
            + "greaterThan(?dti, '0.349999'^^xsd:double) "
            + "-> (?applicant ex:loanEligibility 'Not Eligible')]"
            + "\n"
            + "[NotEligibleCreditRule: "
            + "(?applicant rdf:type foaf:Person) "
            + "(?applicant ex:creditScore ?score) "
            + "lessThan(?score, '621'^^xsd:int) "
            + "-> (?applicant ex:loanEligibility 'Not Eligible')]";
    }

    // Creates the inference model for the Loan Eligibility model
    public static InfModel getLoanEligibilityInfModel() {
        // Register prefixes first - before creating the reasoner
        PrintUtil.registerPrefix("schema", schemaURI);
        PrintUtil.registerPrefix("rdf", rdfURI);
        PrintUtil.registerPrefix("ex", ex);
        PrintUtil.registerPrefix("foaf", "http://xmlns.com/foaf/0.1/");
        PrintUtil.registerPrefix("xsd", "http://www.w3.org/2001/XMLSchema#");

        // Get the base model that defines the applicant's data
        Model baseModel = getLoanEligibilityBaseModel();

        // Combine the rules
        String rules = getLoanEligibilityRules();

        // Create a reasoner using the rules and apply it to the base model
        Reasoner reasoner = new GenericRuleReasoner(Rule.parseRules(rules));
        reasoner.setDerivationLogging(true);

        // Return the inference model
        return com.hp.hpl.jena.rdf.model.ModelFactory.createInfModel(reasoner, baseModel);
    }

    // Creates the base model for the Loan Eligibility model (second type)
    public static Model getLoanEligibilityBaseModelSecondType() {
        Model model = com.hp.hpl.jena.rdf.model.ModelFactory.createDefaultModel();
        
        // Create resources for applicant (using Charlie's data)
        Resource applicant = model.createResource("http://example.com/applicant1");
        Resource personType = model.createResource("http://xmlns.com/foaf/0.1/Person");
        
        // Create properties
        Property type = model.createProperty(rdfURI + "type");
        Property name = model.createProperty(ex + "name");
        Property creditScore = model.createProperty(ex + "creditScore");
        Property monthlyDebt = model.createProperty(schemaURI + "monthlyDebt");
        Property monthlyIncome = model.createProperty(schemaURI + "monthlyIncome");
        
        // Add Charlie's properties to applicant4
        applicant.addProperty(type, personType)
                .addProperty(name, "Trent")
                .addProperty(creditScore, model.createTypedLiteral(700, XSDDatatype.XSDint))
                .addProperty(monthlyDebt, model.createTypedLiteral(1000.0f, XSDDatatype.XSDfloat))
                .addProperty(monthlyIncome, model.createTypedLiteral(5000.0f, XSDDatatype.XSDfloat));
        
        // Set prefixes for better printing
        model.setNsPrefix("rdf", rdfURI);
        model.setNsPrefix("schema", schemaURI);
        model.setNsPrefix("ex", ex);
        model.setNsPrefix("foaf", "http://xmlns.com/foaf/0.1/");
        model.setNsPrefix("xsd", "http://www.w3.org/2001/XMLSchema#");
        
        return model;
    }

    // Creates the inference model for the Loan Eligibility model (second type)
    public static InfModel getLoanEligibilityInfModelSecondType() {
        // Get the base model that defines the applicant's data
        Model baseModel = getLoanEligibilityBaseModelSecondType();

        // Register prefixes for easier output
        PrintUtil.registerPrefix("schema", schemaURI);
        PrintUtil.registerPrefix("rdf", rdfURI);
        PrintUtil.registerPrefix("ex", ex);

        // Use the same rules as the original loan eligibility model
        String rules = getLoanEligibilityRules();

        // Create a reasoner using the rules and apply it to the base model
        Reasoner reasoner = new GenericRuleReasoner(Rule.parseRules(rules));
        reasoner.setDerivationLogging(true);

        // Return the inference model
        return com.hp.hpl.jena.rdf.model.ModelFactory.createInfModel(reasoner, baseModel);
    }

}


