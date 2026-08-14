package vn.gov.cic.study.drools.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.gov.cic.study.drools.cache.EvaluationCache;
import vn.gov.cic.study.drools.domain.EvaluationResult;
import vn.gov.cic.study.drools.domain.LoanApplicationFact;
import vn.gov.cic.study.drools.rules.DroolsRuleEngine;

@Service
@RequiredArgsConstructor
public class LoanEvaluationService {

    private final DroolsRuleEngine ruleEngine;
    private final EvaluationCache cache;

    public EvaluationResult evaluate(LoanApplicationFact request) {
        int firedRules = ruleEngine.evaluate(request, request.getRuleSet());
        EvaluationResult result = EvaluationResult.from(request, firedRules);
        cache.save(result);
        return result;
    }

    public EvaluationResult getLastResult(String requestId) {
        return cache.get(requestId);
    }

    public int reloadRules() {
        return ruleEngine.reloadRules();
    }
}
