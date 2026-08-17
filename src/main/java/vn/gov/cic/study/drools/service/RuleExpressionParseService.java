package vn.gov.cic.study.drools.service;

import java.util.ArrayList;
import java.util.List;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.springframework.stereotype.Service;
import vn.gov.cic.study.drools.parser.StudyRuleLexer;
import vn.gov.cic.study.drools.parser.StudyRuleParser;

@Service
public class RuleExpressionParseService {

    public void parse(String expression) {
        parseRuleExpression(expression);
    }

    public GeneratedDrl generateDrl(String ruleName, int salience, String expression, String message) {
        StudyRuleParser.RuleExpressionContext ruleExpression = parseRuleExpression(expression);
        String condition = toDrlCondition(ruleExpression);
        String rule = """
                rule "%s"
                    salience %d
                when
                    %s
                then
                    $loan.reject("%s");
                end
                """.formatted(ruleName, salience, condition, escapeDrlString(message));

        return new GeneratedDrl(condition, rule);
    }

    private StudyRuleParser.RuleExpressionContext parseRuleExpression(String expression) {
        List<String> errors = new ArrayList<>();

        StudyRuleLexer lexer = new StudyRuleLexer(CharStreams.fromString(expression));
        lexer.removeErrorListeners();
        lexer.addErrorListener(new CollectingErrorListener(errors));

        StudyRuleParser parser = new StudyRuleParser(new CommonTokenStream(lexer));
        parser.removeErrorListeners();
        parser.addErrorListener(new CollectingErrorListener(errors));
        StudyRuleParser.RuleExpressionContext ruleExpression = parser.ruleExpression();

        if (!errors.isEmpty()) {
            throw new IllegalArgumentException("Parse failed: " + String.join("; ", errors));
        }

        return ruleExpression;
    }

    private String toDrlCondition(StudyRuleParser.RuleExpressionContext ruleExpression) {
        StringBuilder expression = new StringBuilder();
        for (int i = 0; i < ruleExpression.condition().size(); i++) {
            if (i > 0) {
                expression.append(' ')
                        .append(toDrlLogicalOperator(ruleExpression.logicalOperator(i - 1)))
                        .append(' ');
            }
            expression.append(toDrlCondition(ruleExpression.condition(i)));
        }
        return "$loan : LoanApplicationFact(" + expression + ")";
    }

    private String toDrlCondition(StudyRuleParser.ConditionContext condition) {
        return condition.IDENTIFIER().getText()
                + " " + condition.operator().getText()
                + " " + condition.value().getText();
    }

    private String toDrlLogicalOperator(StudyRuleParser.LogicalOperatorContext operator) {
        return switch (operator.getText()) {
            case "AND" -> "&&";
            case "OR" -> "||";
            default -> throw new IllegalArgumentException("Unsupported logical operator: " + operator.getText());
        };
    }

    private String escapeDrlString(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    public record GeneratedDrl(
            String condition,
            String rule
    ) {
    }

    private static class CollectingErrorListener extends BaseErrorListener {

        private final List<String> errors;

        private CollectingErrorListener(List<String> errors) {
            this.errors = errors;
        }

        @Override
        public void syntaxError(
                Recognizer<?, ?> recognizer,
                Object offendingSymbol,
                int line,
                int charPositionInLine,
                String msg,
                RecognitionException ex
        ) {
            errors.add("line " + line + ":" + charPositionInLine + " " + msg);
        }
    }
}
