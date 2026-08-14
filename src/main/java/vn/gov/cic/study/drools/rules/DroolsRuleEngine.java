package vn.gov.cic.study.drools.rules;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.kie.api.KieServices;
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

    private KieContainer cachedContainer;

    public synchronized int evaluate(LoanApplicationFact fact) {
        KieSession session = getContainer().newKieSession();
        try {
            session.insert(fact);
            return session.fireAllRules();
        } finally {
            session.dispose();
        }
    }

    private KieContainer getContainer() {
        if (cachedContainer == null) {
            cachedContainer = compileRule();
        }
        return cachedContainer;
    }

    private KieContainer compileRule() {
        try {
            KieServices kieServices = KieServices.Factory.get();
            KieFileSystem fileSystem = kieServices.newKieFileSystem();
            fileSystem.write("src/main/resources/rules/loan-approval.drl", loadRuleContent());

            KieBuilder builder = kieServices.newKieBuilder(fileSystem).buildAll();
            Results results = builder.getResults();
            if (results.hasMessages(Message.Level.ERROR)) {
                throw new IllegalStateException(results.getMessages(Message.Level.ERROR).toString());
            }

            return kieServices.newKieContainer(kieServices.getRepository().getDefaultReleaseId());
        } catch (IOException ex) {
            throw new IllegalStateException("Cannot load rule file: " + rulePath, ex);
        }
    }

    private String loadRuleContent() throws IOException {
        return new ClassPathResource(rulePath).getContentAsString(StandardCharsets.UTF_8);
    }
}
