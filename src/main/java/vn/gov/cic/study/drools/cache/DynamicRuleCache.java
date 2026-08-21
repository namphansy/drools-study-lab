package vn.gov.cic.study.drools.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import vn.gov.cic.study.drools.domain.DynamicRuleDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class DynamicRuleCache {
    private static final String REDIS_KEY = "study:dynamic-rules";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public void save(DynamicRuleDefinition rule) {
        try {
            String json = objectMapper.writeValueAsString(rule);
            redisTemplate.opsForHash().put(REDIS_KEY, rule.getRuleName(), json);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Lỗi serialize DynamicRuleDefinition", e);
        }
    }

    // lấy toàn bộ danh sách rule lưu trong redis
    public List<DynamicRuleDefinition> getAll() {
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(REDIS_KEY);
        List<DynamicRuleDefinition> rules = new ArrayList<>();

        for (Object value : entries.values()) {
            try {
                DynamicRuleDefinition rule = objectMapper.readValue(value.toString(), DynamicRuleDefinition.class);
                rules.add(rule);
            } catch (JsonProcessingException e) {
                throw new IllegalStateException("Lỗi deserialize DynamicRuleDefinition", e);
            }
        }

        return rules;
    }

    public void delete(String ruleName) {
        redisTemplate.opsForHash().delete(REDIS_KEY, ruleName);
    }
}
