package vn.gov.cic.study.drools.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.gov.cic.study.drools.domain.DynamicRuleDefinition;
import vn.gov.cic.study.drools.parser.StudyRuleParser;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RuleDrlGeneratorService {
    private final ExpressionParserService parserService;
    private final StudyRuleToDrlVisitor visitor = new StudyRuleToDrlVisitor();

    /**
     * Chuyển expression thành điều kiện bên trong Fact DRL
     */
    public String convertToDrlCondition(String expression) {
        StudyRuleParser.RuleExpressionContext tree = parserService.parse(expression);
        return visitor.visit(tree);
    }

    /**
     * Sinh ra toàn bộ nội dung file DRL hoàn chỉnh
     */
    public String generateCompleteDrl(DynamicRuleDefinition ruleDef) {
        String drlCondition = convertToDrlCondition(ruleDef.getExpression());
        return String.format(
                "package vn.gov.cic.study.drools.rules;\n\n" +
                        "import vn.gov.cic.study.drools.domain.LoanApplicationFact;\n\n" +
                        "rule \"%s\"\n" +
                        "    salience %d\n" +
                        "when\n" +
                        "    $loan : LoanApplicationFact(%s)\n" +
                        "then\n" +
                        "    $loan.reject(\"%s\");\n" +
                        "end\n",
                ruleDef.getRuleName(),
                ruleDef.getSalience(),
                drlCondition,
                ruleDef.getRejectMessage()
        );
    }

    /**
     * Sinh ra chuỗi DRL hoàn chỉnh từ danh sách DynamicRuleDefinition
     */
    public String generateCombinedDrl(List<DynamicRuleDefinition> rules) {
        StringBuilder sb = new StringBuilder();
        sb.append("package vn.gov.cic.study.drools.rules;\n\n");
        sb.append("import vn.gov.cic.study.drools.domain.LoanApplicationFact;\n\n");

        for (DynamicRuleDefinition rule : rules) {
            String drlCondition = convertToDrlCondition(rule.getExpression());
            sb.append(String.format(
                    "rule \"%s\"\n" +
                            "    salience %d\n" +
                            "when\n" +
                            "    $loan : LoanApplicationFact(%s)\n" +
                            "then\n" +
                            "    $loan.reject(\"%s\");\n" +
                            "end\n\n",
                    rule.getRuleName(),
                    rule.getSalience(),
                    drlCondition,
                    rule.getRejectMessage()
            ));
        }

        return sb.toString();
    }

}
