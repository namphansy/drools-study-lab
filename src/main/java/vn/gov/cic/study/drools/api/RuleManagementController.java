package vn.gov.cic.study.drools.api;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import vn.gov.cic.study.drools.cache.DynamicRuleCache;
import vn.gov.cic.study.drools.domain.DynamicRuleDefinition;
import vn.gov.cic.study.drools.service.RuleDrlGeneratorService;

import java.util.List;

@RestController
@RequestMapping("/api/rules")
@RequiredArgsConstructor
public class RuleManagementController {
    private final RuleDrlGeneratorService drlGeneratorService;
    private final DynamicRuleCache dynamicRuleCache;

    @PostMapping
    public String createOrUpdateRule(@RequestBody DynamicRuleDefinition rule) {
        // Validate expression trước khi lưu bằng cách parse thử
        drlGeneratorService.convertToDrlCondition(rule.getExpression());

        // Lưu vào Redis
        dynamicRuleCache.save(rule);
        return "Rule '" + rule.getRuleName() + "' đã được lưu thành công vào Redis";
    }

    @GetMapping
    public List<DynamicRuleDefinition> getAllRules() {
        return dynamicRuleCache.getAll();
    }

    @DeleteMapping("/{ruleName}")
    public String deleteRule(@PathVariable String ruleName) {
        dynamicRuleCache.delete(ruleName);
        return "Đã xóa rule: " + ruleName;
    }

}
