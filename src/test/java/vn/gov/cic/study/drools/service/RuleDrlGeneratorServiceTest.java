package vn.gov.cic.study.drools.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import vn.gov.cic.study.drools.domain.DynamicRuleDefinition;

import static org.junit.jupiter.api.Assertions.*;

public class RuleDrlGeneratorServiceTest {
    private RuleDrlGeneratorService generatorService;

    @BeforeEach
    public void setUp() {
        ExpressionParserService parserService = new ExpressionParserService();
        generatorService = new RuleDrlGeneratorService(parserService);
    }

    @Test
    public void testConvertToDrlCondition() {
        String expression = "age >= 22 AND creditScore >= 600";
        String drlCondition = generatorService.convertToDrlCondition(expression);
        assertEquals("age >= 22 && creditScore >= 600", drlCondition);
        System.out.println("DRL Condition: " + drlCondition);
    }

    @Test
    public void testGenerateCompleteDrl() {
        DynamicRuleDefinition ruleDef = DynamicRuleDefinition.builder()
                .ruleName("Reject Young or Low Score")
                .salience(50)
                .expression("age < 22 OR creditScore < 600")
                .rejectMessage("CRITERIA_NOT_MET: age must >= 22 and score >= 600")
                .build();
        String drl = generatorService.generateCompleteDrl(ruleDef);
        assertNotNull(drl);
        assertTrue(drl.contains("$loan : LoanApplicationFact(age < 22 || creditScore < 600)"));
        assertTrue(drl.contains("salience 50"));

        System.out.println("=== Generated DRL ===\n" + drl);
    }
}
