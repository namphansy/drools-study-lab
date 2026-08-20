package vn.gov.cic.study.drools.rules;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.drools.decisiontable.InputType;
import org.drools.decisiontable.SpreadsheetCompiler;
import org.kie.api.KieServices;
import org.kie.api.builder.ReleaseId;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Message;
import org.kie.api.builder.Results;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import vn.gov.cic.study.drools.domain.LoanApplicationFact;

@Component
@RequiredArgsConstructor
public class DroolsRuleEngine {

    @Value("${study.drools.rule-path}")
    private String rulePath;

    @Value("${study.drools.rule-directory:}")
    private String ruleDirectory;

    private final Map<String, KieContainer> cachedContainers = new ConcurrentHashMap<>();

    public int evaluate(LoanApplicationFact fact, String ruleSet) {
        String selectedRulePath = resolveRulePath(ruleSet);
        KieSession session = getContainer(selectedRulePath).newKieSession();
        try {
            session.insert(fact);
            return session.fireAllRules();
        } finally {
            session.dispose();
        }
    }

    /**
     * Đánh giá hồ sơ dựa trên chuỗi mã nguồn DRL truyền trực tiếp (dynamic DRL)
     */
    public int evaluateWithDynamicDrl(LoanApplicationFact fact, String drlContent) {
        KieServices kieServices = KieServices.Factory.get();
        ReleaseId releaseId = kieServices.newReleaseId("vn.gov.cic.study", "dynamic-rules", "1.0.0");

        KieFileSystem fileSystem = kieServices.newKieFileSystem();
        fileSystem.generateAndWritePomXML(releaseId);
        fileSystem.write("src/main/resources/generated/dynamic-rules.drl", drlContent);

        KieBuilder builder = kieServices.newKieBuilder(fileSystem).buildAll();
        Results results = builder.getResults();
        if (results.hasMessages(Message.Level.ERROR)) {
            throw new IllegalStateException("Lỗi compile Dynamic DRL: " + results.getMessages(Message.Level.ERROR));
        }

        KieContainer container = kieServices.newKieContainer(releaseId);
        KieSession session = container.newKieSession();
        try {
            session.insert(fact);
            return session.fireAllRules();
        } finally {
            session.dispose();
            container.dispose();
        }
    }

    private KieContainer getContainer(String selectedRulePath) {
        return cachedContainers.computeIfAbsent(selectedRulePath, this::compileRule);
    }

    public void reload() {
        cachedContainers.clear();
    }

    private KieContainer compileRule(String selectedRulePath) {
        try {
            KieServices kieServices = KieServices.Factory.get();
            ReleaseId releaseId = releaseIdFor(selectedRulePath, kieServices);
            KieFileSystem fileSystem = kieServices.newKieFileSystem();
            fileSystem.generateAndWritePomXML(releaseId);
            fileSystem.write(generatedDrlPath(selectedRulePath), loadDrlContent(selectedRulePath));

            KieBuilder builder = kieServices.newKieBuilder(fileSystem).buildAll();
            Results results = builder.getResults();
            if (results.hasMessages(Message.Level.ERROR)) {
                throw new IllegalStateException(results.getMessages(Message.Level.ERROR).toString());
            }

            return kieServices.newKieContainer(releaseId);
        } catch (IOException | UncheckedIOException ex) {
            throw new IllegalStateException("Cannot load rule file: " + selectedRulePath, ex);
        }
    }

    private String loadDrlContent(String selectedRulePath) throws IOException {
        if (isDecisionTable(selectedRulePath)) {
            try (InputStream inputStream = openRuleInputStream(selectedRulePath)) {
                return new SpreadsheetCompiler().compile(inputStream, InputType.XLS);
            }
        }
        return loadTextRuleContent(selectedRulePath);
    }

    private String loadTextRuleContent(String selectedRulePath) throws IOException {
        if (!ruleDirectory.isBlank()) {
            return Files.readString(externalRulePath(selectedRulePath), StandardCharsets.UTF_8);
        }
        return new ClassPathResource(selectedRulePath).getContentAsString(StandardCharsets.UTF_8);
    }

    private InputStream openRuleInputStream(String selectedRulePath) throws IOException {
        if (!ruleDirectory.isBlank()) {
            return Files.newInputStream(externalRulePath(selectedRulePath));
        }
        return new ClassPathResource(selectedRulePath).getInputStream();
    }

    private String resolveRulePath(String ruleSet) {
        return switch (ruleSet.toUpperCase(Locale.ROOT)) {
            case "STANDARD" -> rulePath;
            case "HIGH_RISK" -> "rules/loan-high-risk.drl";
            case "DECISION_TABLE" -> "rules/loan-decision-table.xlsx";
            default -> throw new IllegalArgumentException("Unsupported ruleSet: " + ruleSet);
        };
    }

    private ReleaseId releaseIdFor(String selectedRulePath, KieServices kieServices) {
        String artifactId = selectedRulePath.replaceAll("[^A-Za-z0-9]+", "-");
        return kieServices.newReleaseId("vn.gov.cic.study", artifactId, "1.0.0");
    }

    private Path externalRulePath(String selectedRulePath) {
        String fileName = Path.of(selectedRulePath).getFileName().toString();
        return Path.of(ruleDirectory).resolve(fileName);
    }

    private boolean isDecisionTable(String selectedRulePath) {
        String lowerPath = selectedRulePath.toLowerCase(Locale.ROOT);
        return lowerPath.endsWith(".xls") || lowerPath.endsWith(".xlsx");
    }

    private String generatedDrlPath(String selectedRulePath) {
        String fileName = Path.of(selectedRulePath).getFileName().toString();
        return "src/main/resources/generated/" + fileName.replaceAll("\\.[^.]+$", ".drl");
    }
}
