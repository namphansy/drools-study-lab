package vn.gov.cic.study.drools.api;

import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.gov.cic.study.drools.domain.EvaluationResult;
import vn.gov.cic.study.drools.domain.LoanApplicationFact;
import vn.gov.cic.study.drools.service.LoanEvaluationService;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class LoanEvaluationController {

    private final LoanEvaluationService evaluationService;

    @PostMapping("/evaluate")
    public EvaluationResult evaluate(@Valid @RequestBody LoanApplicationFact request) {
        return evaluationService.evaluate(request);
    }

    @GetMapping("/evaluate/{requestId}/last")
    public EvaluationResult getLastResult(@PathVariable String requestId) {
        return evaluationService.getLastResult(requestId);
    }

    @PostMapping("/rules/reload")
    public Map<String, Object> reloadRules() {
        int clearedRuleSets = evaluationService.reloadRules();
        return Map.of(
                "reloaded", true,
                "clearedRuleSets", clearedRuleSets
        );
    }
}
