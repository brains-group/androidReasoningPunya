package wvw.mobile.rules.explanation;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.reasoner.rulesys.GenericRuleReasoner;
import com.hp.hpl.jena.reasoner.rulesys.Rule;
import com.hp.hpl.jena.reasoner.Reasoner;
import com.hp.hpl.jena.util.PrintUtil;
import com.hp.hpl.jena.vocabulary.RDF;

import android.content.Context;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InputStream;

import android.content.res.AssetManager;


public class ModelFactory {
    // Global URI
    private static String ex  = "http://example.com/";

    // Related to AIME Tutorial and Loan Eligibility Model
    private static String rdfURI = RDF.getURI();
    private static String schemaURI = "http://schema.org/";
    private static String ateURI      = ex + "ate";
    private static String observationURI = schemaURI + "Observation";
    private static String weightURI = schemaURI + "weight";
    private static String unitURI = schemaURI + "unitText";
    private static String variableMeasuredURI = schemaURI + "variableMeasured";
    private static String incomeURI = ex + "income";
    private static String debtURI = ex + "debt";
    private static String creditScoreURI = ex + "creditScore";
    private static String valueURI = schemaURI + "value";
    private static String loanEligibilityURI = ex + "loanEligibility";

    private static String foaf = "http://xmlns.com/foaf/0.1/";
    private static String personURI = foaf + "Person";

    private static String nameURI = ex + "name";
    private static String usdaURI = "http://idea.rpi.edu/heals/kb/usda-ontology#";

    public static String getGlobalURI() {
        return ex;
    }

    public static String getPersonURI() {
        return personURI;
    }

    public static String getObservavtionURI() {
        return observationURI;
    }

    public static String getIncomeURI() {return incomeURI;}

    public static String getDebtURI() {return debtURI;}

    public static String getCreditScoreURI() {return creditScoreURI;}

    public static String getLoanEligibilityURI() {return loanEligibilityURI;}

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

    public static Model getAIMEBaseModel() {
        // Creating the model used in AIME tutorial with Person, Observe:eat usda:Apple
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

//    public static Model getLoanEligibilityBaseModel() {
//        // Creating the model for loan eligibility with Person, Income, Debt, CreditScore
//        Model model = com.hp.hpl.jena.rdf.model.ModelFactory.createDefaultModel();
//
//        // Add namespace prefixes
//        model.setNsPrefix("schema", "http://schema.org/");  // Add schema.org prefix
//        model.setNsPrefix("ex", "http://example.com/");
//        model.setNsPrefix("foaf", "http://xmlns.com/foaf/0.1/");
//
//        // Create the resources
//        Resource applicant = model.createResource(personURI);
//        Resource income = model.createResource(incomeURI);
//        Resource debt = model.createResource(debtURI);
//        Resource creditScore = model.createResource(creditScoreURI);
//
//        // Create properties
//        Property hasValueProperty = model.createProperty(valueURI);
//        Property hasUnitProperty = model.createProperty(unitURI);
//        Property creditScoreProperty = model.createProperty(creditScoreURI);
//        Property typeProperty = model.createProperty(rdfURI + "type");
//        Property nameProperty = model.createProperty(nameURI); // Property for applicant's name
//
//        // Create literals
//        Literal monthlyIncome = model.createTypedLiteral(new BigDecimal(5000)); // Monthly income in dollars
//        Literal monthlyDebt = model.createTypedLiteral(new BigDecimal(1500)); // Monthly debt in dollars
//        Literal creditScoreValue = model.createTypedLiteral(new Integer(700)); // Credit score as integer
//        String dollars = "USD"; // Unit of currency (dollars)
//        String scoreUnit = "integer"; // Unit for credit score
//        String applicantName = "Alex"; // Name "Alex"
//
//        // Add statements
//        applicant.addProperty(typeProperty, applicant);
//        applicant.addProperty(nameProperty, applicantName); // Add name property with value "Alex" to the applicant
//        income.addLiteral(hasValueProperty, monthlyIncome);
//        income.addProperty(hasUnitProperty, dollars);
//        debt.addLiteral(hasValueProperty, monthlyDebt);
//        debt.addProperty(hasUnitProperty, dollars);
//        creditScore.addLiteral(hasValueProperty, creditScoreValue);
//        creditScore.addProperty(hasUnitProperty, scoreUnit);
//
//        // Add properties linking applicant to income, debt, and credit score
//        applicant.addProperty(model.createProperty(incomeURI), income);
//        applicant.addProperty(model.createProperty(debtURI), debt);
//        applicant.addProperty(creditScoreProperty, creditScore);
//
//        return model;
//    }

    public static Model getLoanEligibilityBaseModel() {
        // Creating the model for loan eligibility with Person, Income, Debt, CreditScore
        Model model = com.hp.hpl.jena.rdf.model.ModelFactory.createDefaultModel();

        // Add namespace prefixes
        model.setNsPrefix("schema", "http://schema.org/");  // Add schema.org prefix
        model.setNsPrefix("ex", "http://example.com/");
        model.setNsPrefix("foaf", "http://xmlns.com/foaf/0.1/");

        // Create the resources
        Resource applicant = model.createResource(personURI);
        Resource income = model.createResource(incomeURI);
        Resource debt = model.createResource(debtURI);
        Resource creditScore = model.createResource(creditScoreURI);

        // Create properties
        Property hasValueProperty = model.createProperty(valueURI);
        Property hasUnitProperty = model.createProperty(unitURI);
        Property creditScoreProperty = model.createProperty(creditScoreURI);
        Property typeProperty = model.createProperty(rdfURI + "type");
        Property nameProperty = model.createProperty(nameURI); // Property for applicant's name

        // Create literals
        Literal monthlyIncome = model.createTypedLiteral(new BigDecimal(5000)); // Monthly income in dollars
        Literal monthlyDebt = model.createTypedLiteral(new BigDecimal(1500)); // Monthly debt in dollars
        Literal creditScoreValue = model.createTypedLiteral(new Integer(700)); // Credit score as integer
        String dollars = "USD"; // Unit of currency (dollars)
        String scoreUnit = "integer"; // Unit for credit score
        String applicantName = "Alex"; // Name "Alex"

        // Add statements
        applicant.addProperty(typeProperty, applicant);
        applicant.addProperty(nameProperty, applicantName); // Add name property with value "Alex" to the applicant
        income.addLiteral(hasValueProperty, monthlyIncome);
        income.addProperty(hasUnitProperty, dollars);
        debt.addLiteral(hasValueProperty, monthlyDebt);
        debt.addProperty(hasUnitProperty, dollars);
        creditScore.addLiteral(hasValueProperty, creditScoreValue);
        creditScore.addProperty(hasUnitProperty, scoreUnit);

        // Add properties linking applicant to income, debt, and credit score
        applicant.addProperty(model.createProperty(incomeURI), income);
        applicant.addProperty(model.createProperty(debtURI), debt);
        applicant.addProperty(creditScoreProperty, creditScore);

        return model;
    }


    public static String getAIMERules() {
        String rule1 = "[rule1: ";
        rule1 += "( ?var schema:weight ?weight ) ";
        rule1 += "( ?var schema:variableMeasured ?foodstuff ) ";
        rule1 += "( ?foodstuff usda:sugar ?sugarsPer100g ) ";
        rule1 += "quotient(?weight, '100.0'^^http://www.w3.org/2001/XMLSchema#float, ?scaledWeight) ";
        rule1 += "product(?scaledWeight, ?sugarsPer100g, ?sugars) ";
        rule1 += "-> (?var ex:sugars ?sugars)";
        rule1 += "]";
        String rule2 = "[rule2: ";
        rule2 += "( ?user rdf:type foaf:Person) ";
        rule2 += "( ?user ex:ate ?food) ";
        rule2 += "( ?food ex:sugars ?sugar) ";
        rule2 += "sum(?sugar, '0.0'^^http://www.w3.org/2001/XMLSchema#float, ?totalSugars) ";
        rule2 += "-> ( ?user ex:totalSugars ?totalSugars ) ";
        rule2 += "]";

        return rule1 + " " + rule2;
    }

    public static String getAIMERulesPrefix() {
        // Define prefixes as constants to avoid errors
        String schemaPrefix = "http://schema.org/";
        String usdaPrefix = "http://example.com/usda#";
        String exPrefix = "http://example.com/ex#";
        String rdfPrefix = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
        String foafPrefix = "http://xmlns.com/foaf/0.1/";

        String rule1 = "[rule1: ";
        rule1 += "( ?var " + schemaPrefix + "weight ?weight ) ";
        rule1 += "( ?var " + schemaPrefix + "variableMeasured ?foodstuff ) ";
        rule1 += "( ?foodstuff " + usdaPrefix + "sugar ?sugarsPer100g ) ";
        rule1 += "quotient(?weight, '100.0'^^xsd:float, ?scaledWeight) ";
        rule1 += "product(?scaledWeight, ?sugarsPer100g, ?sugars) ";
        rule1 += "-> (?var " + exPrefix + "sugars ?sugars)";
        rule1 += "]";

        String rule2 = "[rule2: ";
        rule2 += "( ?user " + rdfPrefix + "type " + foafPrefix + "Person) ";
        rule2 += "( ?user " + exPrefix + "ate ?food) ";
        rule2 += "( ?food " + exPrefix + "sugars ?sugar) ";
        rule2 += "sum(?sugar, '0.0'^^xsd:float, ?totalSugars) ";
        rule2 += "-> ( ?user " + exPrefix + "totalSugars ?totalSugars ) ";
        rule2 += "]";

        return rule1 + " " + rule2;
    }

    public static String getLoanEligibilityRules() {
        String rule1 = "[rule1: ";
        rule1 += "( ?applicant <http://schema.org/income> ?monthlyIncome ) ";
        rule1 += "( ?applicant <http://schema.org/debt> ?monthlyDebt ) ";
        rule1 += "quotient(?monthlyDebt, ?monthlyIncome, ?debtToIncomeRatio) ";
        rule1 += "-> (?applicant <http://example.com/debtToIncomeRatio> ?debtToIncomeRatio) ";
        rule1 += "]";

        String rule2 = "[rule2: ";
        rule2 += "( ?applicant <http://schema.org/creditScore> ?creditScore ) ";
        rule2 += "( ?applicant <http://example.com/debtToIncomeRatio> ?debtToIncomeRatio ) ";
        rule2 += "lessThanOrEqual(?debtToIncomeRatio, '0.35'^^xsd:float) ";
        rule2 += "greaterThanOrEqual(?creditScore, '620'^^xsd:integer) ";
        rule2 += "-> (?applicant <http://example.com/loanEligibility> \"Eligible\")";
        rule2 += "]";

        String rule3 = "[rule3: ";
        rule3 += "( ?applicant <http://example.com/debtToIncomeRatio> ?debtToIncomeRatio ) ";
        rule3 += "( ?applicant <http://schema.org/creditScore> ?creditScore ) ";
        rule3 += "not(lessThanOrEqual(?debtToIncomeRatio, '0.35'^^xsd:float)) ";
        rule3 += "or(not(greaterThanOrEqual(?creditScore, '620'^^xsd:integer))) ";
        rule3 += "-> (?applicant <http://example.com/loanEligibility> \"Not Eligible\")";
        rule3 += "]";

        return rule1 + " " + rule2 + " " + rule3;
    }


    public static InfModel getAIMEInfModel() {
        Model baseModel = getAIMEBaseModel();

        // Create the ruleset from AIME tutorial
        PrintUtil.registerPrefix("schema", schemaURI);
        PrintUtil.registerPrefix("usda", usdaURI);
        PrintUtil.registerPrefix("rdf", rdfURI);
        PrintUtil.registerPrefix("ex", ex);
        PrintUtil.registerPrefix("foaf", foaf);

        String rule1 = "[rule1: ";
        rule1 += "( ?var schema:weight ?weight ) ";
        rule1 += "( ?var schema:variableMeasured ?foodstuff ) ";
        rule1 += "( ?foodstuff usda:sugar ?sugarsPer100g ) ";
        rule1 += "quotient(?weight, '100.0'^^http://www.w3.org/2001/XMLSchema#float, ?scaledWeight) ";
        rule1 += "product(?scaledWeight, ?sugarsPer100g, ?sugars) ";
        rule1 += "-> (?var ex:sugars ?sugars)";
        rule1 += "]";
        String rule2 = "[rule2: ";
        rule2 += "( ?user rdf:type foaf:Person) ";
        rule2 += "( ?user ex:ate ?food) ";
        rule2 += "( ?food ex:sugars ?sugar) ";
        rule2 += "sum(?sugar, '0.0'^^http://www.w3.org/2001/XMLSchema#float, ?totalSugars) ";
        rule2 += "-> ( ?user ex:totalSugars ?totalSugars ) ";
        rule2 += "]";

        String rules = rule1 + " " + rule2;

        Reasoner reasoner = new GenericRuleReasoner(Rule.parseRules(rules));

        reasoner.setDerivationLogging(true);
        return com.hp.hpl.jena.rdf.model.ModelFactory.createInfModel(reasoner, baseModel);
    }

    public static InfModel getLoanEligibilityInfModel() {
        // Get the base model that defines the applicant's data (income, debt, credit score)
        Model baseModel = getLoanEligibilityBaseModel();

        // Register prefixes for easier output
        PrintUtil.registerPrefix("schema", schemaURI);
        PrintUtil.registerPrefix("rdf", rdfURI);
        PrintUtil.registerPrefix("ex", ex);

        // Define the loan eligibility rules
        String rule1 = "[rule1: ";
        rule1 += "( ?applicant schema:income ?monthlyIncome ) ";
        rule1 += "( ?applicant schema:debt ?monthlyDebt ) ";
        rule1 += "quotient(?monthlyDebt, ?monthlyIncome, ?debtToIncomeRatio) ";
        rule1 += "-> (?applicant ex:debtToIncomeRatio ?debtToIncomeRatio) ";
        rule1 += "]";

        String rule2 = "[rule2: ";
        rule2 += "( ?applicant schema:creditScore ?creditScore ) ";
        rule2 += "( ?applicant ex:debtToIncomeRatio ?debtToIncomeRatio ) ";
        rule2 += "lessThanOrEqual(?debtToIncomeRatio, '0.35'^^http://www.w3.org/2001/XMLSchema#float) ";
        rule2 += "greaterThanOrEqual(?creditScore, '620'^^http://www.w3.org/2001/XMLSchema#integer) ";
        rule2 += "-> (?applicant ex:loanEligibility \"Eligible\")";
        rule2 += "]";

        String rule3 = "[rule3: ";
        rule3 += "( ?applicant ex:debtToIncomeRatio ?debtToIncomeRatio ) ";
        rule3 += "( ?applicant schema:creditScore ?creditScore ) ";
        rule3 += "not(lessThanOrEqual(?debtToIncomeRatio, '0.35'^^http://www.w3.org/2001/XMLSchema#float)) ";
        rule3 += "or(not(greaterThanOrEqual(?creditScore, '620'^^http://www.w3.org/2001/XMLSchema#integer))) ";
        rule3 += "-> (?applicant ex:loanEligibility \"Not Eligible\")";
        rule3 += "]";

        // Combine the rules
        String rules = rule1 + " " + rule2 + " " + rule3;

        // Create a reasoner using the rules and apply it to the base model
        Reasoner reasoner = new GenericRuleReasoner(Rule.parseRules(rules));
        reasoner.setDerivationLogging(true);

        // Return the inference model
        return com.hp.hpl.jena.rdf.model.ModelFactory.createInfModel(reasoner, baseModel);
    }


    public static Model getAIMEBaseModelBanana() {
        // creating the model used in AIME tutorial with Person, Observe:eat usda:Apple
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

    public static Model getLoanEligibilityBaseModelSecondType() {
        // Creating the model for loan eligibility (second type) with Person, Income, Debt, CreditScore
        Model model = com.hp.hpl.jena.rdf.model.ModelFactory.createDefaultModel();

        // Create the resources
        Resource applicant = model.createResource(personURI);
        Resource income = model.createResource(incomeURI);
        Resource debt = model.createResource(debtURI);
        Resource creditScore = model.createResource(creditScoreURI);

        // Create literals
        Literal monthlyIncome = model.createTypedLiteral(new BigDecimal(4000)); // Monthly income in dollars for second type
        Literal monthlyDebt = model.createTypedLiteral(new BigDecimal(1200)); // Monthly debt in dollars for second type
        Literal creditScoreValue = model.createTypedLiteral(new Integer(650)); // Credit score as integer for second type
        String dollars = "USD"; // Unit of currency (dollars)
        String scoreUnit = "integer"; // Unit for credit score
        String applicantName = "Jordan"; // Name "Jordan" for the second type

        // Add statements
        applicant.addProperty(model.createProperty(rdfURI + "type"), applicant);
        applicant.addProperty(model.createProperty(nameURI), applicantName); // Add name property for "Jordan"
        income.addLiteral(model.createProperty(valueURI), monthlyIncome);
        income.addProperty(model.createProperty(unitURI), dollars);
        debt.addLiteral(model.createProperty(valueURI), monthlyDebt);
        debt.addProperty(model.createProperty(unitURI), dollars);
        creditScore.addLiteral(model.createProperty(valueURI), creditScoreValue);
        creditScore.addProperty(model.createProperty(unitURI), scoreUnit);

        // Link the applicant to income, debt, and credit score
        applicant.addProperty(model.createProperty(incomeURI), income);
        applicant.addProperty(model.createProperty(debtURI), debt);
        applicant.addProperty(model.createProperty(creditScoreURI), creditScore);

        // Set prefix for better printing
        model.setNsPrefix("schema", schemaURI);
        model.setNsPrefix("ex", ex);
        model.setNsPrefix("rdf", rdfURI);

        return model;
    }


    public static InfModel getAIMEInfModelBanana() {
        Model baseModel = getAIMEBaseModelBanana();

        // Create the ruleset from AIME tutorial
        PrintUtil.registerPrefix("schema", schemaURI);
        PrintUtil.registerPrefix("usda", usdaURI);
        PrintUtil.registerPrefix("rdf", rdfURI);
        PrintUtil.registerPrefix("ex", ex);
        PrintUtil.registerPrefix("foaf", foaf);

        // https://jena.apache.org/documentation/inference/#RULEsyntax for specifics on rule syntax
        String rule1 = "[rule1: ";
        rule1 += "( ?var schema:variableMeasured ?foodstuff ) ";
        rule1 += "( ?foodstuff schema:weight ?weight ) ";
        rule1 += "( ?foodstuff usda:sugar ?sugarsPer100g ) ";
        rule1 += "quotient(?weight, '100.0'^^http://www.w3.org/2001/XMLSchema#float, ?scaledWeight) ";
        rule1 += "product(?scaledWeight, ?sugarsPer100g, ?sugars) ";
        rule1 += "-> (?var ex:sugars ?sugars)";
        rule1 += "]";
        String rule2 = "[rule2: ";
        rule2 += "( ?user rdf:type foaf:Person) ";
        rule2 += "( ?user ex:ate ?food) ";
        rule2 += "( ?food ex:sugars ?sugar) ";
        rule2 += "sum(?sugar, '0.0'^^http://www.w3.org/2001/XMLSchema#float, ?totalSugars) ";
        rule2 += "-> ( ?user ex:totalSugars ?totalSugars ) ";
        rule2 += "]";

        String rules = rule1 + " " + rule2;

        Reasoner reasoner = new GenericRuleReasoner(Rule.parseRules(rules));

        reasoner.setDerivationLogging(true);
        InfModel infModel = com.hp.hpl.jena.rdf.model.ModelFactory.createInfModel(reasoner, baseModel);
        return infModel;
    }

    public static InfModel getLoanEligibilityInfModelSecondType() {
        Model baseModel = getLoanEligibilityBaseModelSecondType();

        // Register prefixes for easier output
        PrintUtil.registerPrefix("schema", schemaURI);
        PrintUtil.registerPrefix("rdf", rdfURI);
        PrintUtil.registerPrefix("ex", ex);

        // Define the loan eligibility rules for the second type
        String rule1 = "[rule1: ";
        rule1 += "( ?applicant schema:income ?monthlyIncome ) ";
        rule1 += "( ?applicant schema:debt ?monthlyDebt ) ";
        rule1 += "quotient(?monthlyDebt, ?monthlyIncome, ?debtToIncomeRatio) ";
        rule1 += "-> (?applicant ex:debtToIncomeRatio ?debtToIncomeRatio) ";
        rule1 += "]";

        String rule2 = "[rule2: ";
        rule2 += "( ?applicant schema:creditScore ?creditScore ) ";
        rule2 += "( ?applicant ex:debtToIncomeRatio ?debtToIncomeRatio ) ";
        rule2 += "lessThanOrEqual(?debtToIncomeRatio, '0.35'^^http://www.w3.org/2001/XMLSchema#float) ";
        rule2 += "greaterThanOrEqual(?creditScore, '620'^^http://www.w3.org/2001/XMLSchema#integer) ";
        rule2 += "-> (?applicant ex:loanEligibility \"Eligible\")";
        rule2 += "]";

        String rule3 = "[rule3: ";
        rule3 += "( ?applicant ex:debtToIncomeRatio ?debtToIncomeRatio ) ";
        rule3 += "( ?applicant schema:creditScore ?creditScore ) ";
        rule3 += "not(lessThanOrEqual(?debtToIncomeRatio, '0.35'^^http://www.w3.org/2001/XMLSchema#float)) ";
        rule3 += "or(not(greaterThanOrEqual(?creditScore, '620'^^http://www.w3.org/2001/XMLSchema#integer))) ";
        rule3 += "-> (?applicant ex:loanEligibility \"Not Eligible\")";
        rule3 += "]";

        // Combine the rules
        String rules = rule1 + " " + rule2 + " " + rule3;

        // Create a reasoner using the rules and apply it to the base model
        Reasoner reasoner = new GenericRuleReasoner(Rule.parseRules(rules));
        reasoner.setDerivationLogging(true);

        // Return the inference model
        return com.hp.hpl.jena.rdf.model.ModelFactory.createInfModel(reasoner, baseModel);
    }

}


