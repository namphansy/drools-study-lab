package vn.gov.cic.study.drools.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DynamicRuleDefinition {
    private String ruleName;
    private int salience;
    private String expression;
    private String rejectMessage;
}
