# Câu trả lời tự kiểm tra Bài 1 đến Bài 7

File này tổng hợp câu trả lời cho các mục **Câu hỏi tự kiểm tra** trong `LESSONS.md` từ Bài 1 đến Bài 7. Các bài không có mục câu hỏi tự kiểm tra được bỏ qua.

## Bài 1: Chạy luồng evaluate đầu tiên

### `firedRules` nghĩa là gì?

`firedRules` là số lượng rule Drools đã match điều kiện trong phần `when` và đã được thực thi phần `then` trong lần evaluate đó.

Ví dụ nếu hồ sơ vi phạm 3 điều kiện:

- tuổi dưới 22
- điểm tín dụng dưới 600
- khoản vay vượt ngưỡng thu nhập

thì Drools có thể fire 3 rule, và response sẽ có:

```json
"firedRules": 3
```

Trong project này, mỗi rule khi chạy thường gọi:

```java
$loan.reject("...");
```

Vì vậy `firedRules` giúp biết có bao nhiêu rule đã tham gia đánh giá hồ sơ.

### Vì sao `status HTTP 200` nhưng hồ sơ vẫn bị từ chối?

`HTTP 200` chỉ có nghĩa là request hợp lệ về mặt kỹ thuật và server xử lý thành công. Nó không có nghĩa là hồ sơ vay được duyệt.

Kết quả nghiệp vụ nằm trong body response, cụ thể là field:

```json
"approved": false
```

Nếu `approved = false`, hồ sơ bị từ chối vì vi phạm rule nghiệp vụ. Các lý do từ chối nằm trong:

```json
"violations": [...]
```

Nói cách khác:

```text
HTTP status = kết quả xử lý API
approved    = kết quả nghiệp vụ của hồ sơ vay
```

## Bài 4: Hiểu salience

### Salience cao hơn chạy trước hay sau?

Salience cao hơn chạy trước.

Ví dụ:

```drl
salience 30
```

sẽ chạy trước:

```drl
salience 20
```

và `salience 20` sẽ chạy trước `salience 10`.

Rule không khai báo `salience` có giá trị mặc định là `0`, nên thường chạy sau các rule có salience dương.

### Nếu không có salience, thứ tự có nên được business phụ thuộc không?

Không nên.

Nếu business logic phụ thuộc vào thứ tự chạy rule, thứ tự đó cần được khai báo rõ ràng bằng `salience` hoặc bằng cơ chế điều phối rule phù hợp như agenda group/rule flow.

Nếu không khai báo rõ, thứ tự chạy có thể phụ thuộc vào cách Drools build agenda, thứ tự rule trong file, điều kiện match, hoặc thay đổi khi rule được chỉnh sửa sau này. Business không nên phụ thuộc vào thứ tự ngầm vì dễ gây lỗi khó đoán.

## Bài 5: Redis cache kết quả

### Key Redis đang có prefix gì?

Prefix Redis hiện tại là:

```text
study:last-evaluation:
```

Trong code, prefix này nằm ở `EvaluationCache`:

```java
private static final String PREFIX = "study:last-evaluation:";
```

Khi lưu kết quả cho request:

```text
REQ-100
```

key đầy đủ trong Redis sẽ là:

```text
study:last-evaluation:REQ-100
```

### TTL cache hiện tại là bao lâu?

TTL hiện tại là 30 phút.

Trong code:

```java
private static final Duration TTL = Duration.ofMinutes(30);
```

Điều này có nghĩa là kết quả evaluate gần nhất của một `requestId` sẽ được lưu trong Redis tối đa 30 phút. Sau thời gian đó, key sẽ tự hết hạn nếu không được ghi lại.

## Bài 7: Reload rule không restart app

### Vì sao compile rule mỗi request là không tốt?

Compile rule mỗi request không tốt vì compile DRL là thao tác tốn tài nguyên hơn nhiều so với chạy rule đã compile.

Nếu request nào cũng compile lại rule:

- latency của API tăng
- CPU bị dùng nhiều hơn
- memory và GC bị áp lực hơn
- throughput giảm khi traffic tăng
- hệ thống khó ổn định trong production

Cách hợp lý hơn là:

```text
Compile rule một lần
-> cache KieContainer
-> mỗi request tạo KieSession từ cache
-> chỉ clear cache và compile lại khi rule thay đổi
```

Đó là mục đích của endpoint:

```http
POST /api/rules/reload
```

Endpoint này clear cache rule container. Lần evaluate tiếp theo sẽ compile lại rule từ file `.drl`.

### Khi có nhiều instance app, cần invalidate cache qua đâu?

Khi có nhiều instance app, không thể chỉ reload cache trên một instance.

Ví dụ:

```text
app-1
app-2
app-3
```

Nếu chỉ gọi:

```http
POST /api/rules/reload
```

vào `app-1`, thì `app-1` dùng rule mới nhưng `app-2` và `app-3` vẫn có thể đang giữ rule cũ trong memory.

Trong production cần một cơ chế invalidate cache chung để tất cả instance cùng biết rule đã thay đổi, ví dụ:

- Redis Pub/Sub
- Kafka hoặc RabbitMQ
- lưu rule version trong Redis/Database để instance tự kiểm tra
- rule management service phát sự kiện `RULE_CHANGED`
- cơ chế deployment/orchestration gọi reload đến toàn bộ instance

Với project này đã có Redis, hướng tự nhiên nhất là dùng Redis Pub/Sub:

```text
Rule thay đổi
-> publish event RULE_CHANGED lên Redis
-> mọi app instance subscribe event
-> mỗi instance tự clear rule cache của mình
```
