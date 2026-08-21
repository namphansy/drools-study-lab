package vn.gov.cic.study.drools.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.gov.cic.study.drools.cache.DynamicRuleCache;
import vn.gov.cic.study.drools.cache.EvaluationCache;
import vn.gov.cic.study.drools.domain.DynamicRuleDefinition;
import vn.gov.cic.study.drools.domain.EvaluationResult;
import vn.gov.cic.study.drools.domain.LoanApplicationFact;
import vn.gov.cic.study.drools.rules.DroolsRuleEngine;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LoanEvaluationService {

    private final DroolsRuleEngine ruleEngine;
    private final EvaluationCache cache;
    private final DynamicRuleCache dynamicRuleCache;
    private final RuleDrlGeneratorService drlGeneratorService;

    public EvaluationResult evaluate(LoanApplicationFact request, String ruleSet) {
        int firedRules;
        if ("DYNAMIC".equalsIgnoreCase(ruleSet)) {
            List<DynamicRuleDefinition> dynamicRules = dynamicRuleCache.getAll();
            if (dynamicRules.isEmpty()) {
                firedRules = 0;
            } else {
                String dynamicDrl = drlGeneratorService.generateCombinedDrl(dynamicRules);
                firedRules = ruleEngine.evaluateWithDynamicDrl(request, dynamicDrl);
            }
        } else {
            firedRules = ruleEngine.evaluate(request, ruleSet);
        }

        EvaluationResult result = EvaluationResult.from(request, firedRules);
        cache.save(result);

        return result;
    }

    public EvaluationResult getLastResult(String requestId) {
        return cache.get(requestId);
    }

    public void reloadRules() {
        ruleEngine.reload();
    }
}
