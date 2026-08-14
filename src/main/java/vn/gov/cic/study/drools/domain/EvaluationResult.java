package vn.gov.cic.study.drools.domain;

import java.util.List;

public record EvaluationResult(
        String requestId,
        String applicantName,
        boolean approved,
        int firedRules,
        List<String> violations
) {

    public static EvaluationResult from(LoanApplicationFact fact, int firedRules) {
        return new EvaluationResult(
                fact.getRequestId(),
                fact.getApplicantName(),
                fact.getViolations().isEmpty(),
                firedRules,
                List.copyOf(fact.getViolations())
        );
    }
}
