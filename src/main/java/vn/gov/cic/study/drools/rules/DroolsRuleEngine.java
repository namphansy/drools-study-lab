package vn.gov.cic.study.drools.rules;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Message;
import org.kie.api.builder.ReleaseId;
import org.kie.api.builder.Results;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import vn.gov.cic.study.drools.domain.LoanApplicationFact;
import vn.gov.cic.study.drools.domain.RuleSet;

@Component
@RequiredArgsConstructor
public class DroolsRuleEngine {

    private final Map<RuleSet, KieContainer> cachedContainers = new EnumMap<>(RuleSet.class);

    public synchronized int evaluate(LoanApplicationFact fact, RuleSet ruleSet) {
        KieSession session = getContainer(ruleSet).newKieSession();
        try {
            session.insert(fact);
            return session.fireAllRules();
        } finally {
            session.dispose();
        }
    }

    private KieContainer getContainer(RuleSet ruleSet) {
        return cachedContainers.computeIfAbsent(ruleSet, this::compileRule);
    }

    private KieContainer compileRule(RuleSet ruleSet) {
        try {
            KieServices kieServices = KieServices.Factory.get();
            ReleaseId releaseId = kieServices.newReleaseId(
                    "vn.gov.cic.study",
                    "drools-study-lab-" + ruleSet.name().toLowerCase(),
                    "1.0.0"
            );
            KieFileSystem fileSystem = kieServices.newKieFileSystem();
            fileSystem.generateAndWritePomXML(releaseId);
            fileSystem.write("src/main/resources/" + ruleSet.getRulePath(), loadRuleContent(ruleSet));

            KieBuilder builder = kieServices.newKieBuilder(fileSystem).buildAll();
            Results results = builder.getResults();
            if (results.hasMessages(Message.Level.ERROR)) {
                throw new IllegalStateException(results.getMessages(Message.Level.ERROR).toString());
            }

            return kieServices.newKieContainer(releaseId);
        } catch (IOException ex) {
            throw new IllegalStateException("Cannot load rule file: " + ruleSet.getRulePath(), ex);
        }
    }

    private String loadRuleContent(RuleSet ruleSet) throws IOException {
        return new ClassPathResource(ruleSet.getRulePath()).getContentAsString(StandardCharsets.UTF_8);
    }
}
