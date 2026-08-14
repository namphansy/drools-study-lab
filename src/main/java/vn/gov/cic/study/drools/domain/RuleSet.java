package vn.gov.cic.study.drools.domain;

public enum RuleSet {
    STANDARD("rules/loan-approval.drl"),
    HIGH_RISK("rules/loan-high-risk.drl");

    private final String rulePath;

    RuleSet(String rulePath) {
        this.rulePath = rulePath;
    }

    public String getRulePath() {
        return rulePath;
    }
}
