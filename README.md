# Drools Study Lab

Project study tối giản cho dev mới tiếp cận Spring Boot, Drools, Redis và ANTLR.

## Mục tiêu

- Hiểu luồng cơ bản: client gửi request -> build fact -> chạy Drools rule -> trả kết quả.
- Biết rule DRL được cấu hình ở đâu và được compile như thế nào.
- Thấy Redis được dùng để cache metadata/lần chạy gần nhất.
- Có grammar ANTLR mini để mở rộng dần sang rule expression language.

## Công nghệ

- Java 21
- Spring Boot 3.5.4
- Drools 9.44.0.Final
- Redis
- ANTLR4 4.13.2
- Docker Compose

## Chạy bằng Docker

```bash
docker compose up --build
```

App chạy tại:

```text
http://localhost:8080
```

## Test nhanh

```bash
curl -X POST http://localhost:8080/api/evaluate \
  -H "Content-Type: application/json" \
  -d @requests/evaluate-rejected.json
```

Kết quả sẽ có `approved=false` nếu hồ sơ vi phạm rule.

Lấy lại kết quả gần nhất từ Redis:

```bash
curl http://localhost:8080/api/evaluate/REQ-001/last
```

## Cấu trúc chính

```text
src/main/java/vn/gov/cic/study/drools
  api/              REST API
  domain/           Fact và model kết quả
  rules/            Load/compile Drools rule
  cache/            Redis cache demo
src/main/resources/rules/loan-approval.drl
src/main/antlr/StudyRule.g4
docs/LESSONS.md
```

## Luồng xử lý

```text
Client
  -> POST /api/evaluate
  -> LoanEvaluationController
  -> LoanEvaluationService
  -> DroolsRuleEngine
  -> KieSession.fireAllRules()
  -> EvaluationResult
  -> Redis lưu lần đánh giá gần nhất
  -> Client nhận response
```
