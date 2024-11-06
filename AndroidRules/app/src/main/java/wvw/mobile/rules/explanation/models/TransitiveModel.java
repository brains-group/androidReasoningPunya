package wvw.mobile.rules.explanation.models;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.RDF;
import wvw.mobile.rules.explanation.RDFKnowledgeBase;
import wvw.mobile.rules.explanation.utils.URIConstants;

public class TransitiveModel extends RDFKnowledgeBase {
    
    @Override
    protected Model createBaseModel() {
        Model model = ModelFactory.createDefaultModel();
        
        // Create resources for the transitive chain: A -> B -> C -> D
        Resource resourceA = model.createResource("A");
        Resource resourceB = model.createResource("B");
        Resource resourceC = model.createResource("C");
        Resource resourceD = model.createResource("D");
        
        // Create the 'equals' property
        Property equals = model.createProperty(URIConstants.EX_URI + "equals");
        
        // Add the direct relationships
        model.add(resourceA, equals, resourceB);
        model.add(resourceB, equals, resourceC);
        model.add(resourceC, equals, resourceD);
        
        return model;
    }

    @Override
    protected String createRules() {
        // Create a transitive rule that states:
        // If A equals B and B equals C, then A equals C
        return "[transitiveRule: (?a ex:equals ?b) (?b ex:equals ?c) -> (?a ex:equals ?c)]";
    }

    @Override
    protected Model createAlternativeModel() {
        Model model = ModelFactory.createDefaultModel();
        
        // Create resources for a different transitive chain: A -> B -> E -> D
        Resource resourceA = model.createResource("A");
        Resource resourceB = model.createResource("B");
        Resource resourceE = model.createResource("E"); // Different middle node
        Resource resourceD = model.createResource("D");
        
        // Create the 'equals' property
        Property equals = model.createProperty(URIConstants.EX_URI + "equals");
        
        // Add the direct relationships for the alternative path
        model.add(resourceA, equals, resourceB);
        model.add(resourceB, equals, resourceE);
        model.add(resourceE, equals, resourceD);
        
        return model;
    }

    /**
     * Helper method to get a specific node in the transitive chain
     * @param nodeName The name of the node (A, B, C, D, or E)
     * @return The Resource representing that node
     */
    public Resource getNode(String nodeName) {
        validateModel();
        return model.createResource(nodeName);
    }

    /**
     * Helper method to get the equals property
     * @return The Property representing the equals relationship
     */
    public Property getEqualsProperty() {
        validateModel();
        return model.createProperty(URIConstants.EX_URI + "equals");
    }

    /**
     * Helper method to check if two nodes are equal (either directly or through inference)
     * @param node1 The first node
     * @param node2 The second node
     * @return true if the nodes are equal, false otherwise
     */
    public boolean areNodesEqual(Resource node1, Resource node2) {
        validateInfModel();
        Property equals = getEqualsProperty();
        return infModel.contains(node1, equals, node2);
    }

    /**
     * Get all nodes that are equal to the given node
     * @param node The node to check
     * @return A StmtIterator containing all equal relationships
     */
    public StmtIterator getEqualNodes(Resource node) {
        validateInfModel();
        Property equals = getEqualsProperty();
        return infModel.listStatements(node, equals, (RDFNode)null);
    }
} 