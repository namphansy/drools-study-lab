package vn.gov.cic.study.drools.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import vn.gov.cic.study.drools.domain.EvaluationResult;

@Component
@RequiredArgsConstructor
public class EvaluationCache {

    private static final Duration TTL = Duration.ofMinutes(30);
    private static final String PREFIX = "study:last-evaluation:";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public void save(EvaluationResult result) {
        try {
            redisTemplate.opsForValue().set(PREFIX + result.requestId(), objectMapper.writeValueAsString(result), TTL);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Cannot serialize evaluation result", ex);
        }
    }

    public EvaluationResult get(String requestId) {
        String json = redisTemplate.opsForValue().get(PREFIX + requestId);
        if (json == null) {
            return null;
        }
        try {
            return objectMapper.readValue(json, EvaluationResult.class);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Cannot deserialize evaluation result", ex);
        }
    }
}
