package vn.gov.cic.study.drools.service;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.springframework.stereotype.Service;

@Service
public class ExpressionParserService {
    public vn.gov.cic.study.drools.parser.StudyRuleParser.RuleExpressionContext parse(String expression) {
        if (expression == null || expression.isBlank()) {
            throw new IllegalArgumentException("Biểu thức không được để trống");
        }

        // 1. Chuyển chuỗi đầu vào thành CharStream
        CharStream charStream = CharStreams.fromString(expression);

        // 2. Lexer: Phân tích từ vựng (tách các token: IDENTIFIER, NUMBER, operator...)
        vn.gov.cic.study.drools.parser.StudyRuleLexer lexer = new vn.gov.cic.study.drools.parser.StudyRuleLexer(charStream);
        lexer.removeErrorListeners();
        lexer.addErrorListener(ThrowingErrorListener.INSTANCE);

        // 3. Gom token thành stream
        CommonTokenStream tokens = new CommonTokenStream(lexer);

        // 4. Parser: Xây dựng cây cú pháp theo file ngữ pháp StudyRule.g4
        vn.gov.cic.study.drools.parser.StudyRuleParser parser = new vn.gov.cic.study.drools.parser.StudyRuleParser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(ThrowingErrorListener.INSTANCE);

        return parser.ruleExpression();
    }
}
