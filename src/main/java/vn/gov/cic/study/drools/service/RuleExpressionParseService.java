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
        List<String> errors = new ArrayList<>();

        StudyRuleLexer lexer = new StudyRuleLexer(CharStreams.fromString(expression));
        lexer.removeErrorListeners();
        lexer.addErrorListener(new CollectingErrorListener(errors));

        StudyRuleParser parser = new StudyRuleParser(new CommonTokenStream(lexer));
        parser.removeErrorListeners();
        parser.addErrorListener(new CollectingErrorListener(errors));
        parser.ruleExpression();

        if (!errors.isEmpty()) {
            throw new IllegalArgumentException("Parse failed: " + String.join("; ", errors));
        }
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
