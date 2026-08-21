package vn.gov.cic.study.drools.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ExpressionParserServiceTest {
    private ExpressionParserService parserService;

    @BeforeEach
    public void setUp() {
        parserService = new ExpressionParserService();
    }

    @Test
    public void testParse_ValidExpression_Success() {
        String expression = "age >= 22 AND creditScore >= 600";

        vn.gov.cic.study.drools.parser.StudyRuleParser.RuleExpressionContext context = parserService.parse(expression);

        assertNotNull(context);
        System.out.println("Parse OK: " + expression);
        System.out.println("Parse Tree String: " + context.toStringTree());
    }

    @Test
    public void testParse_MultipleConditions_Success() {
        String expression = "loanAmount <= 500000000 AND employmentMonths > 6 OR hasExistingBadDebt == \"NO\"";

        vn.gov.cic.study.drools.parser.StudyRuleParser.RuleExpressionContext context = parserService.parse(expression);

        assertNotNull(context);
        System.out.println("Parse OK: " + expression);
    }

    @Test
    public void testParse_InvalidExpression_ThrowsException() {
        // Biểu thức sai cú pháp: thiếu toán tử hoặc toán tử sai (>>)
        String invalidExpression = "age >> 22 AND creditScore";

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> parserService.parse(invalidExpression)
        );

        System.out.println("Bắt lỗi thành công: " + exception.getMessage());
    }
}
