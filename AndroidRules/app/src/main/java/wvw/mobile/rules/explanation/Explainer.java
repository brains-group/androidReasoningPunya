package wvw.mobile.rules.explanation;

// Java Standard Libraries
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

// Apache Jena Core Libraries
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

// Apache Jena Reasoner Libraries
import com.hp.hpl.jena.reasoner.Derivation;
import com.hp.hpl.jena.reasoner.Reasoner;
import com.hp.hpl.jena.reasoner.rulesys.GenericRuleReasoner;
import com.hp.hpl.jena.reasoner.rulesys.Rule;
import com.hp.hpl.jena.reasoner.rulesys.RuleDerivation;
import com.hp.hpl.jena.util.PrintUtil;

// Android Libraries
import android.util.Log;


/**
 * The <code>Explainer</code> component produces user-friendly
 * explanations of recommendations derived from the <code>Reasoner</code>
 */
/*
@DesignerComponent(version = PunyaVersion.EXPLAINER_COMPONENT_VERSION,
    nonVisible = true,
    category = ComponentCategory.LINKEDDATA,
)
@SimpleObject
 */
public class Explainer {

    // ------------------------------------------------------------
    // Properties
    // ------------------------------------------------------------

    // The base knowledge-graph created by the user. Prior reasoning is not required.
    private Model baseModel;

    // The rules used by the reasoner to make additional assertions on the baseModel.
    private String rules;

    /**
     * Creates a new Explainer component.
     */
    public Explainer(){}

    public Model Model(){
        return this.baseModel;
    }

    public void Model(Model model) {
        this.baseModel = model;
    }

    public String Rules(){
        return this.rules;
    }

    public void Rules(String rules){
        this.rules = rules;
    }

    // ------------------------------------------------------------
    // Logging
    // ------------------------------------------------------------
    

    private static final String TAG = "Explanation-Runner";
    
    // Helper method for consistent logging
    private static void logSection(String title) {
        Log.d(TAG, "\n" + "=".repeat(75));
        Log.d(TAG, title);
        Log.d(TAG, "=".repeat(75));
    }
    
    private static void logSubSection(String title) {
        Log.d(TAG, "\n" + "-".repeat(50));
        Log.d(TAG, title);
        Log.d(TAG, "-".repeat(50));
    }
    
    private static void logDetail(String message) {
        for (String line : message.split("\n")) {
            Log.d(TAG, "\t" + line);
        }
    }

    public static void print(String message) {
        Log.d("Explanation-Runner", message);
    }



    // ------------------------------------------------------------
    // Trace-Based Explanations
    // ------------------------------------------------------------

    /**
     * Produces a single-sentence contextual explanation as to how the inputted statement
     * was derived by a reasoner.
     * @param subject: The statement's subject. Must be a Resource, or null as a wildcard.
     * @param property: The statement's property. Must be a Property, or null as a wildcard.
     * @param object: The statement's object. Can be a Literal, a Resource, or null as a wildcard
     * @return The traced-base explanation string
     */
    public String GetFullTraceBasedExplanation(Object subject, Object property, Object object) {
        StringBuilder explanation = new StringBuilder("");

        InfModel model = generateInfModel(baseModel);
        logSubSection("Inference Model Contents");
        StmtIterator infStmts = model.listStatements();
        while(infStmts.hasNext()) {
            logDetail(infStmts.next().toString());
        }

        explanation.append(generateTraceBasedExplanation(this.baseModel, model, (Resource)subject,
                (Property) property, (RDFNode) object));

        return explanation.toString();
    }


    // Use the generated inf model, to provide a deep trace for a
    // triple (subject : predicate : object). The base model (containing
    // triples not generated by the reasoner) is needed to check whether
    // a statement was generated by the reasoner or inputted by the user.
    private String generateTraceBasedExplanation(Model baseModel, InfModel inf,
                                                 Resource subject, Property predicate, RDFNode object) {
        StringBuilder answer = new StringBuilder();
        StmtIterator stmtItr = inf.listStatements(subject, predicate, object);

        while (stmtItr.hasNext()) {
            Statement s = stmtItr.next();
            Iterator<Derivation> derivItr = inf.getDerivation(s);
            
            while (derivItr.hasNext()) {
                RuleDerivation d = (RuleDerivation) derivItr.next();
                
                // Start with the main conclusion
                answer.append("Conclusion: ").append(formatTripleNicely(s))
                      .append(" used the following matches: \n");
                
                // Show matches and their derivations immediately after
                for (Triple match : d.getMatches()) {
                    Statement matchStatement = generateStatement(match);
                    answer.append(" Match: ").append(formatTripleNicely(matchStatement)).append("\n");
                    
                    // If this match was derived, show its derivation immediately
                    if (!baseModel.contains(matchStatement)) {
                        Iterator<Derivation> matchDerivItr = inf.getDerivation(matchStatement);
                        while (matchDerivItr.hasNext()) {
                            RuleDerivation matchDeriv = (RuleDerivation) matchDerivItr.next();
                            answer.append(tabOffset(1)) // Indent the nested explanation
                                  .append(formatTraceBasedExplanation(matchStatement, 
                                                                    matchDeriv.getMatches(), 
                                                                    matchDeriv.getRule(), 
                                                                    1));
                        }
                    }
                }
                
                // Show the rule at the end
                answer.append("And paired them with the following rule: \n")
                      .append("[ ").append(formatRuleNicely(d.getRule())).append(" ]\n")
                      .append("to reach this conclusion.\n\n");
            }
        }
        return answer.toString();
    }


    // A recursive function that traces through the infModel to determine how the statement was generated
    // by a reasoner, if at all. infModel contains the full RDF model including the triples generated by
    // the reasoner, the baseModel just contains the triples inputted by the user. The statement is the
    // triple that we are tracing back. Tabs specifies the formatting, and can be thought of as the "level"
    // in our model that we're in.
    private String traceDerivation(InfModel infModel, Model baseModel, Statement statement, int tabs) {
        StringBuilder results = new StringBuilder();
        
        // Header for this conclusion
        results.append("\n").append("=".repeat(50)).append("\n");
        results.append("CONCLUSION: ").append(formatTriple(statement)).append("\n");
        results.append("-".repeat(50)).append("\n");
        
        // Find derivations
        Iterator<Derivation> derivItr = infModel.getDerivation(statement);
        
        if (!derivItr.hasNext()) {
            results.append("This is a base fact that was directly provided.\n");
            return results.toString();
        }
        
        while (derivItr.hasNext()) {
            RuleDerivation d = (RuleDerivation) derivItr.next();
            results.append("\nDerived using:\n");
            
            // Show matches that led to this conclusion
            for (Triple match : d.getMatches()) {
                Statement matchStatement = generateStatement(match);
                
                // Recursively trace this match if it was derived
                if (!baseModel.contains(matchStatement)) {
                    results.append("\n→ Used derived fact:\n");
                    results.append(traceDerivation(infModel, baseModel, matchStatement, tabs + 1));
                } else {
                    results.append("• ").append(formatTriple(matchStatement)).append("\n");
                }
            }
            
            // Show the rule that was applied
            results.append("\nBy applying rule:\n");
            results.append("\"").append(formatRuleNicely(d.getRule())).append("\"\n");
        }
        
        return results.toString();
    }

    private String formatTriple(Statement stmt) {
        String subject = cleanValue(stmt.getSubject().toString());
        String predicate = formatPropertyName(cleanValue(stmt.getPredicate().getLocalName()));
        String object = cleanValue(stmt.getObject().toString());
        
        return String.format("%s has %s: %s", subject, predicate, object);
    }


    // ------------------------------------------------------------
    // Contextual Explanations
    // ------------------------------------------------------------

    /**
     * Produces a brief user-readable contextual explanation of how the inputted statement was
     * concluded. Based on the Contextual Ontology:
     * https://tetherless-world.github.io/explanation-ontology/modeling/#casebased/
     * @param resource The resource of the statement.
     * @param property The property of the statement.
     * @param object The object of the statement.
     * @return a shallow trace through the derivations of a statement,
     * formatted in a contextual explanation.
     */
    public String GetShallowContextualExplanation(Object resource, Object property, Object object) {
        logSubSection("Generating Shallow Contextual Explanation");
        logDetail("Triple: (" + resource + ", " + property + ", " + object + ")");

        StringBuilder explanation = new StringBuilder();
        InfModel model = generateInfModel(baseModel);
        
        explanation.append("Shallow Explanation:\n");
        StmtIterator itr = model.listStatements((Resource)resource, (Property) property, (RDFNode) object);
        while(itr.hasNext()) {
            explanation.append(generateShallowTrace(itr.next(), model));
        }

        // Add simple contextual explanation
        explanation.append("\nSimple Explanation:\n");
        explanation.append(GetSimpleContextualExplanation(resource, property, object));

        return explanation.toString();
    }


    /**
     * Generates a shallow Contextual explanation.
     * @param s The statement being derived
     * @param model The InfModel containing the user-set and reasoner-derived knowledge graph.
     * @return A shallow contextual explanation.
     */
    private String generateShallowTrace(Statement s, InfModel model) {
        StringBuilder explanation = new StringBuilder();
        
        Iterator<Derivation> itr = model.getDerivation(s);
        
        if (!itr.hasNext()) {
            explanation.append("This is a base fact: ").append(formatTripleNicely(s)).append("\n");
            return explanation.toString();
        }
        
        while(itr.hasNext()) {
            RuleDerivation derivation = (RuleDerivation) itr.next();
            
            // Format the main conclusion
            explanation.append("Conclusion: ").append(formatTripleNicely(s)).append("\n");
            
            // Format the rule
            explanation.append("Based on rule: ").append(formatRuleNicely(derivation.getRule())).append("\n");
            
            // Format the supporting facts
            explanation.append("Using the following facts:\n");
            for (Triple match : derivation.getMatches()) {
                Statement matchStmt = generateStatement(match);
                explanation.append("  • ").append(formatTripleNicely(matchStmt)).append("\n");
            }
        }
        
        return explanation.toString();
    }


    /**
     * Produces a single-sentence contextual explanation as to how the inputted statement
     * was derived by a reasoner.
     * @param resource The resource of the statement.
     * @param property The property of the statement.
     * @param object The object of the statement.
     * @return
     */
    public String GetSimpleContextualExplanation(Object resource, Object property, Object object) {
        logSubSection("Generating Simple Contextual Explanation");
        logDetail("Triple: (" + resource + ", " + property + ", " + object + ")");

        StringBuilder explanation = new StringBuilder();

        InfModel model = generateInfModel(baseModel);
        logDetail("Generated Inference Model: " + model);

        StmtIterator itr = model.listStatements((Resource)resource, (Property) property, (RDFNode) object);

        while(itr.hasNext()) {
            explanation.append(generateSimpleContextualExplanation(itr.next(), model));
            explanation.append("\n\n");
        }
        return explanation.toString();
    }


    /**
     * Generates a simple contextual explanation for a statement, given the
     * model containing the derivations.
     * @param s
     * @param model
     * @return
     */
    private String generateSimpleContextualExplanation(Statement s, InfModel model) {
        StringBuilder explanation = new StringBuilder();

        Iterator<Derivation> itr = model.getDerivation(s);

        while(itr.hasNext()) {
            RuleDerivation derivation = (RuleDerivation) itr.next();
            explanation.append(formatTripleNicely(s))
                      .append(" because ");

            List<Triple> matches = derivation.getMatches();
            int matchIndex = 0;
            for (Triple match : matches) {
                Statement binding = generateStatement(match);
                explanation.append(formatTripleNicely(binding));
                if (matchIndex < matches.size()-1) {
                    explanation.append(" and ");
                }
                matchIndex++;
            }
        }
        explanation.append(".");
        return explanation.toString();
    }



    // ------------------------------------------------------------
    // Contrastive Explanations
    // ------------------------------------------------------------

    /**
     * Generate counterfactual explanation for statement by comparing how this.baseModel reached the conclusion
     * compared to how otherBaseModel differs (or match) the conclusion by using the same ruleSet, this.rules.
     * Highlight the difference
     * @param statement the statement (conclusion) to generate explanation
     * @param otherBaseModel the other baseModel to compare this.baseModel to after apply this.rule to both
     * @return The InfModel derived from the reasoner.
     */
    public String GetFullContrastiveExplanation_B(Statement statement, Model otherBaseModel) {
        logDetail("Starting contrastive explanation generation");
        
        InfModel thisInfModel = generateInfModel(baseModel);
        InfModel otherInfModel = generateInfModel(otherBaseModel);
        
        StringBuilder results = new StringBuilder();
        
        // Get all relevant facts about this applicant
        Resource subject = statement.getSubject();
        StmtIterator thisApplicantFacts = thisInfModel.listStatements(subject, null, (RDFNode)null);
        StmtIterator otherApplicantFacts = otherInfModel.listStatements(subject, null, (RDFNode)null);
        
        // Collect relevant facts (excluding type and name)
        Map<String, Statement> thisFactMap = new HashMap<>();
        Map<String, Statement> otherFactMap = new HashMap<>();
        
        while (thisApplicantFacts.hasNext()) {
            Statement fact = thisApplicantFacts.next();
            String predicate = fact.getPredicate().getLocalName().toLowerCase();
            if (!predicate.equals("type") && !predicate.equals("name")) {
                thisFactMap.put(predicate, fact);
            }
        }
        
        while (otherApplicantFacts.hasNext()) {
            Statement fact = otherApplicantFacts.next();
            String predicate = fact.getPredicate().getLocalName().toLowerCase();
            if (!predicate.equals("type") && !predicate.equals("name")) {
                otherFactMap.put(predicate, fact);
            }
        }
        
        // Compare the facts
        results.append("Similarities:\n");
        for (String predicate : thisFactMap.keySet()) {
            Statement thisFact = thisFactMap.get(predicate);
            Statement otherFact = otherFactMap.get(predicate);
            
            if (otherFact != null && thisFact.getObject().equals(otherFact.getObject())) {
                results.append("  • ").append(formatTripleNicely(thisFact)).append("\n");
            }
        }
        
        results.append("\nDifferences:\n");
        for (String predicate : thisFactMap.keySet()) {
            Statement thisFact = thisFactMap.get(predicate);
            Statement otherFact = otherFactMap.get(predicate);
            
            if (otherFact != null && !thisFact.getObject().equals(otherFact.getObject())) {
                String thisValue = getLiteralValue(thisFact.getObject());
                String otherValue = getLiteralValue(otherFact.getObject());
                
                results.append("  • For ").append(formatPropertyName(predicate)).append(": ")
                       .append("this model has ").append(thisValue)
                       .append(" while alternate model has ").append(otherValue)
                       .append("\n");
            }
        }
        
        return results.toString();
    }

    private String getLiteralValue(RDFNode node) {
        if (node.isLiteral()) {
            Literal literal = (Literal) node;
            String value = literal.getString();
            
            try {
                // Handle different numeric types
                if (literal.getDatatype() != null) {
                    if (value.contains(".")) {
                        // Format decimal numbers (like DTI ratio) to 2 decimal places
                        double numValue = Double.parseDouble(value);
                        return String.format("%.2f", numValue);
                    } else {
                        // Format integers (like credit score) without decimals
                        int numValue = Integer.parseInt(value);
                        return String.format("%d", numValue);
                    }
                }
            } catch (NumberFormatException e) {
                // If it's not a number, return the raw value
                return value;
            }
            return value;
        }
        return node.toString();
    }


    // Helper method to describe a triple
    private String describeTriple(Triple triple) {
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


    // Helper method to describe a statement
    private String describeStatement(Statement statement) {
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
  


    // ------------------------------------------------------------
    // Counterfactual Explanation
    // ------------------------------------------------------------

    /**
     * Generates a counterfactual explanation by comparing an instance to other instances
     * with a desired outcome.
     * 
     * @param targetInstance The instance to generate explanation for
     * @param outcomeProperty The property indicating the outcome (e.g., eligibility)
     * @param feature1Property First feature to compare (e.g., credit score)
     * @param feature2Property Second feature to compare (e.g., DTI ratio)
     * @return A string explanation of what changes are needed
     */
    public String GetCounterfactualExplanation(Statement statement) {
        InfModel infModel = generateInfModel(baseModel);
        StringBuilder explanation = new StringBuilder();
        
        Resource targetInstance = statement.getSubject();
        Property outcomeProperty = statement.getPredicate();
        RDFNode currentOutcome = statement.getObject();
        
        Statement outcome = getStatement(infModel, targetInstance, outcomeProperty);
        if (outcome == null) {
            logDetail("Error: Could not find outcome property for target instance");
            return "Error: Could not find outcome property for target instance";
        }
        
        explanation.append("To change the outcome for ").append(formatTripleNicely(outcome))
                  .append(", you could look at these examples:\n\n");
        
        // Find instances with different outcomes
        StmtIterator otherInstances = infModel.listStatements(null, outcomeProperty, (RDFNode) null);
        
        while (otherInstances.hasNext()) {
            Statement otherStatement = otherInstances.next();
            Resource otherInstance = otherStatement.getSubject();
            
            // Skip if it's the target instance
            if (otherInstance.equals(targetInstance)) continue;
            
            // If this instance has a different outcome, analyze why
            if (!otherStatement.getObject().equals(currentOutcome)) {
                explanation.append(formatTripleNicely(otherStatement))
                          .append(" because:\n");
                
                // List properties that are different
                StmtIterator properties = infModel.listStatements(targetInstance, null, (RDFNode) null);
                while (properties.hasNext()) {
                    Statement prop = properties.next();
                    Statement otherProp = getStatement(infModel, otherInstance, prop.getPredicate());
                    
                    // Skip certain properties and those with same values
                    if (otherProp != null && 
                        !prop.getPredicate().equals(outcomeProperty) &&
                        !prop.getObject().equals(otherProp.getObject()) &&
                        !prop.getPredicate().getLocalName().equals("type") &&
                        !prop.getPredicate().getLocalName().equals("name")) {
                        
                        explanation.append("- Their ").append(formatTripleNicely(otherProp))
                                  .append(" while yours is ").append(formatTripleNicely(prop))
                                  .append("\n");
                    }
                }
                explanation.append("\n");
            }
        }
        
        return explanation.toString();
    }

    private String formatTripleNicely(Statement stmt) {
        String subject = cleanValue(stmt.getSubject().toString());
        String predicate = formatPropertyName(cleanValue(stmt.getPredicate().getLocalName()));
        
        // Special handling for object values
        String object;
        RDFNode objectNode = stmt.getObject();
        if (objectNode.isLiteral()) {
            // Get the actual value from the literal
            Literal literal = (Literal) objectNode;
            object = literal.getString(); // Get the raw value
            logDetail("Literal value: " + object);
        } else {
            object = cleanValue(objectNode.toString());
        }
        
        return String.format("%s has %s: %s", subject, predicate, object);
    }

    private String cleanValue(String value) {
        logDetail("Cleaning value: " + value);
        
        // Remove URI prefixes
        if (value.contains("#")) {
            value = value.substring(value.indexOf("#") + 1);
        } else if (value.contains("/")) {
            value = value.substring(value.lastIndexOf("/") + 1);
        }
        
        // Handle numeric values with data types
        if (value.contains("^^")) {
            String rawValue = value.substring(0, value.indexOf("^^"));
            logDetail("Raw numeric value: " + rawValue);
            
            // Try to parse and format numbers
            try {
                if (rawValue.matches("\\d*\\.?\\d+")) {
                    double number = Double.parseDouble(rawValue);
                    if (number < 1) { // Likely a ratio
                        return String.format("%.2f", number);
                    }
                    return String.format("%.0f", number); // Integer format for other numbers
                }
            } catch (NumberFormatException e) {
                logDetail("Failed to parse number: " + e.getMessage());
            }
            
            return rawValue; // Return the raw value if parsing fails
        }
        
        // Remove quotes if present
        if (value.startsWith("\"") && value.endsWith("\"")) {
            value = value.substring(1, value.length() - 1);
        }
        
        logDetail("Cleaned value: " + value);
        return value;
    }

    /**
     * Formats a property name from snake_case or camelCase to Title Case
     * e.g., "credit_score" -> "Credit Score", "dtiRatio" -> "DTI Ratio"
     */
    private String formatPropertyName(String propertyName) {
        logDetail("Formatting property name: " + propertyName);
        
        // Handle camelCase
        propertyName = propertyName.replaceAll("([a-z])([A-Z])", "$1_$2");
        
        String[] words = propertyName.split("_");
        StringBuilder formatted = new StringBuilder();
        
        for (String word : words) {
            if (word.length() > 0) {
                // Special case for abbreviations like DTI
                if (word.equalsIgnoreCase("dti")) {
                    formatted.append("DTI");
                } else if (word.length() <= 3 && word.toUpperCase().equals(word)) {
                    formatted.append(word); // Keep other abbreviations as-is
                } else {
                    formatted.append(Character.toUpperCase(word.charAt(0)))
                            .append(word.substring(1).toLowerCase());
                }
                formatted.append(" ");
            }
        }
        
        String result = formatted.toString().trim();
        logDetail("Formatted property name: " + result);
        return result;
    }



    // ------------------------------------------------------------
    // Utility Methods
    // ------------------------------------------------------------

    /**
     * Runs a reasoner on the Linked Data. Guarantees derivations are
     * stored.
     * @return The InfModel derived from the reasoner.
     */
    private InfModel generateInfModel(Model baseModel){
        // Register prefixes before creating the reasoner
        PrintUtil.registerPrefix("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        PrintUtil.registerPrefix("schema", "http://schema.org/");
        PrintUtil.registerPrefix("ex", "http://example.com/");
        PrintUtil.registerPrefix("foaf", "http://xmlns.com/foaf/0.1/");
        PrintUtil.registerPrefix("xsd", "http://www.w3.org/2001/XMLSchema#");
        
        Reasoner reasoner = new GenericRuleReasoner(Rule.parseRules(rules));
        reasoner.setDerivationLogging(true);
        return com.hp.hpl.jena.rdf.model.ModelFactory.createInfModel(reasoner, baseModel);
    }

    /**
     * Generates a statement using the URIS present in the triple.
     * @param triple
     * @return A basic statement
     */
    private Statement generateStatement(Triple triple){
        Resource subject = ResourceFactory.createResource(triple.getSubject().getURI());
        Property property = ResourceFactory.createProperty(triple.getPredicate().getURI());
        Node obj = triple.getObject();
        if (obj.isLiteral()){
            Literal l = ResourceFactory.createTypedLiteral(obj.getLiteralValue().toString(), obj.getLiteralDatatype());
            return ResourceFactory.createStatement(subject, property, l);
        }
        if (!obj.isLiteral()){
            Resource matchObject = ResourceFactory.createResource(triple.getObject().getURI());
            return ResourceFactory.createStatement(subject, property, matchObject);
        }
        return null;
    }

    private Statement getStatement(InfModel model, Resource subject, Property predicate) {
        StmtIterator itr = model.listStatements(subject, predicate, (RDFNode)null);
        if (itr.hasNext()) {
            return itr.next();
        }
        return null;
    }

    // returns a string with num tabs in it
    private String tabOffset(int num) {
        String tab = "";
        for (int i=0; i < num; i++) {
            tab += ("\t");
        }
        return tab;
    }

    private String formatTraceBasedExplanation(Statement conclusion, List<Triple> matches, Rule rule, int depth) {
        StringBuilder explanation = new StringBuilder();
        String indent = tabOffset(depth);
        
        // Format conclusion
        explanation.append(indent).append("Conclusion: ").append(formatTripleNicely(conclusion))
                  .append(" used the following matches: \n");
        
        // Format supporting evidence
        for (Triple match : matches) {
            Statement matchStmt = generateStatement(match);
            explanation.append(indent).append(" Match: ").append(formatTripleNicely(matchStmt)).append("\n");
        }
        
        // Format rule used
        if (rule != null) {
            explanation.append(indent).append("And paired them with the following rule: \n");
            explanation.append(indent).append("[ ").append(formatRuleNicely(rule)).append(" ]\n");
            explanation.append(indent).append("to reach this conclusion.\n\n");
        }
        
        return explanation.toString();
    }

    private String formatRuleNicely(Rule rule) {
        // Remove technical syntax and make more readable
        String ruleBody = rule.toString()
            .replaceAll("\\[.*?\\]:", "") // Remove rule name
            .replaceAll("\\^\\^.*?\\s", " ") // Remove data types
            .replaceAll("ex:", "")
            .replaceAll("rdf:", "")
            .replaceAll("foaf:", "")
            .replaceAll("schema:", "")
            .trim();
            
        return ruleBody;
    }

}
