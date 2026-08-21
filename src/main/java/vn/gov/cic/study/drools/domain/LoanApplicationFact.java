package vn.gov.cic.study.drools.domain;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class LoanApplicationFact {

    @NotBlank
    private String requestId;

    @NotBlank
    private String applicantName;

    @Min(0)
    private int age;

    @Min(0)
    private BigDecimal monthlyIncome = BigDecimal.ZERO;

    @Min(0)
    private BigDecimal loanAmount = BigDecimal.ZERO;

    @Min(0)
    private int creditScore;

    private boolean hasExistingBadDebt;

    private int employmentMonths;

    public int getEmploymentMonths() {
        return employmentMonths;
    }

    public void setEmploymentMonths(int employmentMonths) {
        this.employmentMonths = employmentMonths;
    }

    private final List<String> violations = new ArrayList<>();

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getApplicantName() {
        return applicantName;
    }

    public void setApplicantName(String applicantName) {
        this.applicantName = applicantName;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public BigDecimal getMonthlyIncome() {
        return monthlyIncome;
    }

    public void setMonthlyIncome(BigDecimal monthlyIncome) {
        this.monthlyIncome = monthlyIncome;
    }

    public BigDecimal getLoanAmount() {
        return loanAmount;
    }

    public void setLoanAmount(BigDecimal loanAmount) {
        this.loanAmount = loanAmount;
    }

    public int getCreditScore() {
        return creditScore;
    }

    public void setCreditScore(int creditScore) {
        this.creditScore = creditScore;
    }

    public boolean isHasExistingBadDebt() {
        return hasExistingBadDebt;
    }

    public void setHasExistingBadDebt(boolean hasExistingBadDebt) {
        this.hasExistingBadDebt = hasExistingBadDebt;
    }

    public List<String> getViolations() {
        return violations;
    }

    public void reject(String reason) {
        violations.add(reason);
    }

    public BigDecimal getMaxAffordableLoan() {
        return monthlyIncome.multiply(BigDecimal.valueOf(24));
    }

    public BigDecimal getHighRiskMaxAffordableLoan() {
        return monthlyIncome.multiply(BigDecimal.valueOf(12));
    }
}
