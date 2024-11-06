public class Contrastive implements ExplanationType {
    @Override
    public String explain(ExplanationContext context) {
        Statement statement = context.getInfModel().createStatement(
            context.getSubject(), 
            context.getPredicate(), 
            context.getObject()
        );
        return generateContrastiveExplanation(statement, context);
    }

    private String generateContrastiveExplanation(Statement statement, ExplanationContext context) {
        // TODO
    }
}