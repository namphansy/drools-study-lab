package vn.gov.cic.study.drools.rules;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import vn.gov.cic.study.drools.domain.LoanApplicationFact;

class DroolsRuleEngineTests {

    @Test
    void evaluatesRulesFromDecisionTable() {
        DroolsRuleEngine ruleEngine = new DroolsRuleEngine();
        ReflectionTestUtils.setField(ruleEngine, "rulePath", "rules/loan-approval.drl");
        ReflectionTestUtils.setField(ruleEngine, "ruleDirectory", "");

        LoanApplicationFact fact = new LoanApplicationFact();
        fact.setRequestId("REQ-DT-001");
        fact.setApplicantName("Decision Table Applicant");
        fact.setAge(21);
        fact.setMonthlyIncome(BigDecimal.valueOf(8_000_000));
        fact.setLoanAmount(BigDecimal.valueOf(300_000_000));
        fact.setCreditScore(540);
        fact.setHasExistingBadDebt(true);

        int firedRules = ruleEngine.evaluate(fact, "DECISION_TABLE");

        assertThat(firedRules).isEqualTo(4);
        assertThat(fact.getViolations())
                .containsExactly(
                        "DT_AGE_TOO_LOW: applicant must be at least 22 years old",
                        "DT_BAD_DEBT: applicant has existing bad debt",
                        "DT_LOW_CREDIT_SCORE: credit score must be at least 600",
                        "DT_LOAN_TOO_HIGH: loan amount is greater than 24 months of income"
                );
    }
}
