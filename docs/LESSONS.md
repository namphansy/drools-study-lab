# Bài tập Drools Study Lab

Tài liệu này chia project thành các bài nhỏ, tăng dần độ khó. Dev mới nên làm tuần tự, mỗi bài chỉ cần hiểu một khái niệm chính.

## Bài 1: Chạy luồng evaluate đầu tiên

Mục tiêu:

- Chạy được app bằng Docker.
- Gửi request vào `/api/evaluate`.
- Đọc response và hiểu `approved`, `firedRules`, `violations`.

Việc cần làm:

1. Chạy `docker compose up --build`.
2. Gửi request hồ sơ vay dưới 22 tuổi.
3. Đổi `age` lên 25 và quan sát rule nào còn match.

Câu hỏi tự kiểm tra:

- `firedRules` nghĩa là gì?
- Vì sao `status HTTP 200` nhưng hồ sơ vẫn bị từ chối?

## Bài 2: Đọc fact object

Mục tiêu:

- Hiểu `LoanApplicationFact` là dữ liệu được đưa vào Drools.
- Hiểu rule thay đổi object bằng method `reject(...)`.

Việc cần làm:

1. Mở `LoanApplicationFact`.
2. Tìm các field được rule sử dụng: `age`, `creditScore`, `loanAmount`, `monthlyIncome`, `hasExistingBadDebt`.
3. Thêm field mới `employmentMonths`.

Gợi ý:

```java
private int employmentMonths;
```

## Bài 3: Viết rule DRL mới

Mục tiêu:

- Biết thêm rule trong file `.drl`.
- Hiểu `when` và `then`.

Việc cần làm:

1. Mở `src/main/resources/rules/loan-approval.drl`.
2. Thêm rule: từ chối nếu `employmentMonths < 6`.
3. Gửi request có `employmentMonths = 3`.

Rule gợi ý:

```drl
rule "Reject short employment history"
when
    $loan : LoanApplicationFact(employmentMonths < 6)
then
    $loan.reject("SHORT_EMPLOYMENT: employment history must be at least 6 months");
end
```

## Bài 4: Hiểu salience

Mục tiêu:

- Biết rule nào chạy trước khi nhiều rule cùng match.

Việc cần làm:

1. Đổi `salience` của rule `LOW_CREDIT_SCORE`.
2. Gửi request vi phạm nhiều rule.
3. Quan sát thứ tự `violations`.

Câu hỏi tự kiểm tra:

- Salience cao hơn chạy trước hay sau?
- Nếu không có salience, thứ tự có nên được business phụ thuộc không?

## Bài 5: Redis cache kết quả

Mục tiêu:

- Hiểu Redis trong project demo đang lưu kết quả evaluate gần nhất.

Việc cần làm:

1. Gửi request với `requestId = REQ-100`.
2. Gọi `GET /api/evaluate/REQ-100/last`.
3. Tắt app, giữ Redis, bật app lại và gọi lại endpoint trên.

Câu hỏi tự kiểm tra:

- Key Redis đang có prefix gì?
- TTL cache hiện tại là bao lâu?

## Bài 6: Tách rule theo nhóm

Mục tiêu:

- Bắt đầu nghĩ theo cấu hình rule thay vì một file cố định.

Việc cần làm:

1. Tạo thêm file `rules/loan-high-risk.drl`.
2. Thêm query param hoặc field request `ruleSet`.
3. Cho service chọn rule file theo `ruleSet`.

Gợi ý thiết kế:

```text
STANDARD -> rules/loan-approval.drl
HIGH_RISK -> rules/loan-high-risk.drl
```

## Bài 7: Reload rule không restart app

Mục tiêu:

- Hiểu vì sao production rule engine cần cache invalidation.

Việc cần làm:

1. Thêm endpoint `POST /api/rules/reload`.
2. Trong `DroolsRuleEngine`, clear `cachedContainer`.
3. Gọi evaluate lại sau khi sửa rule.

Câu hỏi tự kiểm tra:

- Vì sao compile rule mỗi request là không tốt?
- Khi có nhiều instance app, cần invalidate cache qua đâu?

## Bài 8: Dùng ANTLR để parse expression mini

Mục tiêu:

- Làm quen ANTLR ở mức rất nhỏ.
- Parse expression dạng `age >= 22 AND creditScore >= 600`.

Việc cần làm:

1. Mở `src/main/antlr/StudyRule.g4`.
2. Chạy `gradle generateGrammarSource`.
3. Viết service nhận expression string và parse thử.

Ví dụ expression:

```text
age >= 22 AND creditScore >= 600
```

Kết quả mong muốn:

```text
Parse OK
```

## Bài 9: Convert expression sang DRL

Mục tiêu:

- Hiểu bước quan trọng của rule engine cấu hình: expression -> DRL.

Việc cần làm:

1. Dùng parse tree từ ANTLR.
2. Convert expression thành đoạn condition trong DRL.
3. Generate rule hoàn chỉnh.

Ví dụ:

```text
age >= 22 AND creditScore >= 600
```

thành:

```drl
$loan : LoanApplicationFact(age >= 22 && creditScore >= 600)
```

## Bài 10: Lưu rule vào Redis

Mục tiêu:

- Biến demo từ rule file tĩnh thành rule cấu hình đơn giản.

Việc cần làm:

1. Tạo endpoint `POST /api/rules`.
2. Nhận `ruleName`, `salience`, `expression`, `message`.
3. Lưu JSON rule vào Redis.
4. Khi evaluate, load rule từ Redis và generate DRL.

Đây là bước nối project study với tư duy của hệ thống CIC Rule Engine thật.
