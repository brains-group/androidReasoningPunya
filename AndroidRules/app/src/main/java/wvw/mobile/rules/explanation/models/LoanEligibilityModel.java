package wvw.mobile.rules.explanation;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.reasoner.Reasoner;
import com.hp.hpl.jena.reasoner.rulesys.GenericRuleReasoner;
import com.hp.hpl.jena.reasoner.rulesys.Rule;
import java.math.BigDecimal;

public class LoanEligibilityModel extends BaseRDFModel {
    private static final String INCOME_URI = EX_URI + "income";
    private static final String DEBT_URI = EX_URI + "debt";
    private static final String CREDIT_SCORE_URI = EX_URI + "creditScore";
    private static final String VALUE_URI = SCHEMA_URI + "value";
    private static final String LOAN_ELIGIBILITY_URI = EX_URI + "loanEligibility";
    private static final String NAME_URI = EX_URI + "name";

    @Override
    public Model createBaseModel() {
        model = ModelFactory.createDefaultModel();
        
        // Create the resources
        Resource applicant = model.createResource(FOAF_URI + "Person");
        Resource income = model.createResource(INCOME_URI);
        Resource debt = model.createResource(DEBT_URI);
        Resource creditScore = model.createResource(CREDIT_SCORE_URI);

        // Create literals
        Literal monthlyIncome = model.createTypedLiteral(new BigDecimal(5000));
        Literal monthlyDebt = model.createTypedLiteral(new BigDecimal(1500));
        Literal creditScoreValue = model.createTypedLiteral(new Integer(700));
        String dollars = "USD";
        String scoreUnit = "integer";
        String applicantName = "John";

        // Add properties and statements
        applicant.addProperty(model.createProperty(RDF_URI + "type"), applicant);
        applicant.addProperty(model.createProperty(NAME_URI), applicantName);
        income.addLiteral(model.createProperty(VALUE_URI), monthlyIncome);
        income.addProperty(model.createProperty(SCHEMA_URI + "unitText"), dollars);
        debt.addLiteral(model.createProperty(VALUE_URI), monthlyDebt);
        debt.addProperty(model.createProperty(SCHEMA_URI + "unitText"), dollars);
        creditScore.addLiteral(model.createProperty(VALUE_URI), creditScoreValue);
        creditScore.addProperty(model.createProperty(SCHEMA_URI + "unitText"), scoreUnit);

        applicant.addProperty(model.createProperty(INCOME_URI), income);
        applicant.addProperty(model.createProperty(DEBT_URI), debt);
        applicant.addProperty(model.createProperty(CREDIT_SCORE_URI), creditScore);

        return model;
    }

    @Override
    public String createRules() {
        StringBuilder rulesStr = new StringBuilder();
        
        // Rule 1: Calculate debt-to-income ratio
        rulesStr.append("[rule1: (?applicant ex:income ?income) (?income schema:value ?incomeValue) ")
                .append("(?applicant ex:debt ?debt) (?debt schema:value ?debtValue) ")
                .append("quotient(?debtValue, ?incomeValue, ?ratio) ")
                .append("-> (?applicant ex:debtToIncomeRatio ?ratio)]");

        // Rule 2: Determine loan eligibility based on debt-to-income ratio
        rulesStr.append("[rule2: (?applicant ex:debtToIncomeRatio ?ratio) ")
                .append("lessThan(?ratio, 0.43) ")
                .append("-> (?applicant ex:loanEligibility \"ELIGIBLE\")]");

        // Rule 3: Check if ineligible due to high debt-to-income ratio
        rulesStr.append("[rule3: (?applicant ex:debtToIncomeRatio ?ratio) ")
                .append("greaterThan(?ratio, 0.43) ")
                .append("-> (?applicant ex:loanEligibility \"INELIGIBLE\")]");

        rules = rulesStr.toString();
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
        
        // Similar to createBaseModel but with different values
        Resource applicant = altModel.createResource(FOAF_URI + "Person");
        Resource income = altModel.createResource(INCOME_URI);
        Resource debt = altModel.createResource(DEBT_URI);
        Resource creditScore = altModel.createResource(CREDIT_SCORE_URI);

        // Different values for counterfactual comparison
        Literal monthlyIncome = altModel.createTypedLiteral(new BigDecimal(4000));
        Literal monthlyDebt = altModel.createTypedLiteral(new BigDecimal(1200));
        Literal creditScoreValue = altModel.createTypedLiteral(new Integer(650));
        String dollars = "USD";
        String scoreUnit = "integer";
        String applicantName = "Jordan";

        // Add properties and statements similar to createBaseModel()
        applicant.addProperty(altModel.createProperty(RDF_URI + "type"), applicant);
        applicant.addProperty(altModel.createProperty(NAME_URI), applicantName);
        income.addLiteral(altModel.createProperty(VALUE_URI), monthlyIncome);
        income.addProperty(altModel.createProperty(SCHEMA_URI + "unitText"), dollars);
        debt.addLiteral(altModel.createProperty(VALUE_URI), monthlyDebt);
        debt.addProperty(altModel.createProperty(SCHEMA_URI + "unitText"), dollars);
        creditScore.addLiteral(altModel.createProperty(VALUE_URI), creditScoreValue);
        creditScore.addProperty(altModel.createProperty(SCHEMA_URI + "unitText"), scoreUnit);

        applicant.addProperty(altModel.createProperty(INCOME_URI), income);
        applicant.addProperty(altModel.createProperty(DEBT_URI), debt);
        applicant.addProperty(altModel.createProperty(CREDIT_SCORE_URI), creditScore);

        return altModel;
    }
} 