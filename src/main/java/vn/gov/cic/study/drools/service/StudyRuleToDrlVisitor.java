package vn.gov.cic.study.drools.service;

public class StudyRuleToDrlVisitor extends vn.gov.cic.study.drools.parser.StudyRuleBaseVisitor<String> {

    public String visitLogicalOperator(vn.gov.cic.study.drools.parser.StudyRuleParser.LogicalOperatorContext ctx) {
        String op = ctx.getText();
        if ("AND".equalsIgnoreCase(op)) {
            return "&&";
        } else if ("OR".equalsIgnoreCase(op)) {
            return "||";
        }
        return op;
    }

    public String visitRuleExpression(vn.gov.cic.study.drools.parser.StudyRuleParser.RuleExpressionContext ctx) {
        StringBuilder sb = new StringBuilder();

        sb.append(visit(ctx.condition(0)));

        for (int i = 0; i < ctx.logicalOperator().size(); i++) {
            sb.append(" ");
            sb.append(visit(ctx.logicalOperator(i)));
            sb.append(" ");
            sb.append(visit(ctx.condition(i + 1)));
        }

        return sb.toString();
    }

    public String visitCondition(vn.gov.cic.study.drools.parser.StudyRuleParser.ConditionContext ctx) {
        String identifier = ctx.IDENTIFIER().getText();
        String operator = ctx.operator().getText();
        String value = ctx.value().getText();
        return String.format("%s %s %s", identifier, operator, value);
    }
}
