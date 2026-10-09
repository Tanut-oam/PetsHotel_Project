# PetsHotel — ระบบจัดการโรงแรมรับฝากสัตว์เลี้ยง


เว็บแอปพลิเคชันสำหรับจองห้องพักสัตว์เลี้ยง ลูกค้าค้นหาห้องว่าง จองห้อง เลือกบริการเสริม และดูรายงานการดูแลสัตว์รายวันได้ ส่วนพนักงานและผู้ดูแลระบบใช้จัดการห้อง การจอง การเช็คอิน/เช็คเอาท์ การชำระเงิน โปรโมชัน และรายงานได้

รายวิชา CP353002 Principles of Software Design and Development

**Live demo:** https://petshotel.onrender.com
(ใช้ Render แบบฟรี ถ้าไม่มีคนเข้า 15 นาที server จะหลับ ครั้งแรกที่เปิดอาจต้องรอประมาณ 1 นาที)

## สมาชิก

| ลำดับ | ชื่อ | รหัสนักศึกษา | Section | Branch | หน้าที่ |
| :---: | :--- | :---: | :---: | :--- | :--- |
| 1 | นายกิตติธัช คำโท | 673380029-0 | sec 1 | `kittithat_673380029-0_01` | Room & Admin, User, Notification (Observer), Security, Flyway, Docker / CI/CD / Deploy |
| 2 | นายธนัช พนมเริงไชย | 673380041-0 | sec 1 | `tanut_673380041-0_01` | Availability & Room Search, Global Exception Handler |
| 3 | นางสาวรติมา สวัสดิ์นที | 673380055-9 | sec 1 | `ratima_673380055-9_01` | Pet & Daily Care Report, Test Report |
| 4 | นางสาวกิติญาดา กองคำ | 673380509-6 | sec 1 | `kitiyada_673380509-6_01` | Booking & State Pattern |
| 5 | นางสาวนาเดีย คิดอ่าน | 673380513-5 | sec 1 | `nadia_673380513-5_01` | Pricing (Strategy), Promotion, Extra Service, Dashboard |

## Tech Stack

| ส่วน | เทคโนโลยี |
| :--- | :--- |
| Language | Java 26 |
| Framework | Spring Boot 4.1.1 (Web MVC, Data JPA, Security, Validation, Mail) |
| View | Thymeleaf |
| Database | PostgreSQL |
| Migration | Flyway |
| API Docs | springdoc-openapi (Swagger UI) |
| Testing | JUnit 5, Mockito, Spring Boot Test, JaCoCo |
| Build | Maven (Maven Wrapper) |
| Container | Docker, Docker Compose |
| CI/CD | GitHub Actions |
| Deployment | Render (web service) + Neon (PostgreSQL) |

## Architecture

ระบบใช้ **Layered Architecture**

```
Controller (Web / REST)  →  Service  →  Repository  →  Database
        ↑                      ↑
       DTO  ←── Mapper ──→  Entity
```

- **Controller** รับ request แยกเป็น `controller/web` (หน้าเว็บ Thymeleaf) และ `controller/api` (REST API) ไม่เรียก Repository ตรง
- **Service** เก็บ business logic เรียกผ่าน interface
- **Repository** ใช้ Spring Data JPA
- **DTO + Mapper** แยกข้อมูลที่รับ/ส่งออกจาก Entity
- ทุกชั้นใช้ **Constructor Injection**

Design Patterns ที่ใช้

| Pattern | ใช้ที่ |
| :--- | :--- |
| State | สถานะการจอง (PENDING → CONFIRMED → CHECKED_IN → CHECKED_OUT / CANCELLED) |
| Strategy | การคำนวณราคา (ค่าห้อง, บริการเสริม, ส่วนเพิ่มวันหยุด, ส่วนลดพักยาว, ส่วนลดโปรโมชัน) |
| Observer | ส่งอีเมลยืนยันหลังการจองได้รับการยืนยัน (`BookingConfirmedEvent` + `@TransactionalEventListener`) |
| Enterprise | Layered Architecture, MVC, Repository, Service Layer, DTO + Mapper, Dependency Injection |

รายละเอียดอยู่ใน [doc/design-patterns.md](doc/design-patterns.md) และ [doc/solid-analysis](doc/solid-analysis)

## Project Structure

```
PetsHotel_Project/
├── .github/workflows/         # GitHub Actions (CI/CD)
├── code/                      # Spring Boot project
│   ├── src/main/java/com/example/petshotel/
│   │   ├── config/            # Security, Swagger, Web config
│   │   ├── controller/api     # REST controllers
│   │   ├── controller/web     # Web (Thymeleaf) controllers
│   │   ├── domain/            # Entities และ Enums
│   │   ├── dto/               # Request / Response DTO
│   │   ├── exception/         # Custom exceptions และ GlobalExceptionHandler
│   │   ├── mapper/            # Entity ↔ DTO
│   │   ├── notification/      # Observer: event, listener, email
│   │   ├── pricing/           # Strategy: การคำนวณราคา
│   │   ├── repository/        # Spring Data JPA
│   │   ├── service/           # Business logic
│   │   └── state/             # State: สถานะการจอง
│   ├── src/main/resources/
│   │   ├── db/migration/      # Flyway V1–V3
│   │   ├── templates/         # Thymeleaf
│   │   └── static/            # CSS, JS, รูปภาพ
│   ├── src/test/java/         # Unit / integration tests
│   ├── Dockerfile
│   └── docker-compose.yml
├── doc/                       # เอกสาร (diagram, data dictionary, SOLID, use case, slide)
├── img/                       # รูป diagram และหลักฐานผลการทดสอบ
└── test/                      # Test report และ coverage report
```

## Database (ER Diagram)

![ER Diagram](img/diagrams/er-diagram.png)

มีทั้งหมด 10 ตาราง: `users`, `pets`, `rooms`, `bookings`, `booking_pets`, `extra_services`, `booking_extra_services`, `promotions`, `receipts`, `daily_care_reports`

| ความสัมพันธ์ | ตัวอย่าง |
| :--- | :--- |
| One-to-One | `bookings` — `receipts` (1 การจองมีใบเสร็จได้ 1 ใบ) |
| One-to-Many | `users` — `pets`, `rooms` — `bookings`, `users` — `bookings` |
| Many-to-Many | `bookings` — `pets` ผ่าน `booking_pets`, `bookings` — `extra_services` ผ่าน `booking_extra_services` |

มี Foreign Key, CHECK constraint, Index สำหรับ query ที่ใช้บ่อย และ `ON DELETE CASCADE` ในตารางเชื่อม คำอธิบายแต่ละคอลัมน์และเหตุผลของ FK / Index / Fetch Type อยู่ใน [doc/data-dictionary](doc/data-dictionary)

ตารางสร้างด้วย Flyway จาก `code/src/main/resources/db/migration` (Hibernate ตั้งเป็น `ddl-auto=validate` ตรวจอย่างเดียว ไม่สร้างตารางเอง)

| Migration | ทำอะไร |
| :--- | :--- |
| `V1__create_tables.sql` | สร้าง 10 ตาราง พร้อม constraint และ index |
| `V2__insert_sample_data.sql` | บัญชี admin / staff, ห้อง 4 ห้อง, บริการเสริม และโปรโมชันตัวอย่าง |
| `V3__add_receipt_customer_name.sql` | เพิ่มคอลัมน์ `customer_name` ให้ใบเสร็จเก็บชื่อลูกค้า ณ วันที่ออก |

## Installation / How to Run

### วิธีที่ 1: Docker (แนะนำ)

ต้องมี Docker Desktop

```bash
cd code
docker compose up --build
```

| URL | ใช้ทำอะไร |
| :--- | :--- |
| http://localhost:8080 | เว็บ PetsHotel |
| http://localhost:8080/swagger-ui.html | Swagger UI |
| http://localhost:8025 | Mailpit (ดูอีเมลที่ระบบส่ง) |

หยุดระบบ: `docker compose down` (ถ้าต้องการลบข้อมูลใน database ด้วย ใช้ `docker compose down -v`)

### วิธีที่ 2: รันบนเครื่อง

ต้องมี JDK 26 และ PostgreSQL

1. สร้าง database ชื่อ `pethotel`
2. ตั้งรหัสผ่าน database ผ่าน environment variable

   Windows (cmd)
   ```bat
   set "SPRING_DATASOURCE_PASSWORD=รหัสผ่าน PostgreSQL ของคุณ"
   ```
   macOS / Linux
   ```bash
   export SPRING_DATASOURCE_PASSWORD=รหัสผ่าน PostgreSQL ของคุณ
   ```
3. รันโปรแกรม (Flyway จะสร้างตารางและใส่ข้อมูลตัวอย่างให้อัตโนมัติ)
   ```bash
   cd code
   ./mvnw spring-boot:run
   ```
   Windows ใช้ `mvnw.cmd spring-boot:run`
4. เปิด http://localhost:8080

ถ้าต้องการทดสอบการส่งอีเมล ให้เปิด Mailpit ไว้ที่ `localhost:1025` (หรือใช้วิธีที่ 1)

### บัญชีทดสอบ (local / Docker)

| Role | Email | Password |
| :--- | :--- | :--- |
| ADMIN | admin@petshotel.test | PetsHotel@2026 |
| STAFF | staff@petshotel.test | PetsHotel@2026 |

บัญชีลูกค้า (CUSTOMER) สมัครเองได้ที่หน้า Register

บน Live demo ใช้รหัสผ่านคนละชุดกับข้างบน

## API Documentation

เปิด Swagger UI ที่ `/swagger-ui.html` (เช่น http://localhost:8080/swagger-ui.html หรือ https://petshotel.onrender.com/swagger-ui.html)

| Resource | Endpoint หลัก |
| :--- | :--- |
| Rooms | `GET/POST /api/rooms`, `GET/PUT /api/rooms/{id}`, `PATCH /api/rooms/{id}/status`, `PATCH /api/rooms/{id}/deactivate` |
| Room search (Pagination & Sorting) | `GET /api/rooms/search?keyword=&minCapacity=&maxPrice=&page=0&size=10&sort=roomNumber,asc` |
| Room availability | `GET /api/rooms/available`, `GET /api/rooms/{roomId}/availability-calendar` |
| Bookings | `/api/bookings` (สร้าง, ดู, ยืนยัน, เช็คอิน, เช็คเอาท์, ยกเลิก) |
| Payment | `POST /api/bookings/{id}/payment/confirm` |
| Pets | `/api/owners/{ownerId}/pets` |
| Daily care reports | `/api/care-reports` |
| Extra services | `/api/extra-services` |
| Promotions | `/api/promotions` |

- ใช้ HTTP status ตามความหมาย: `200` สำเร็จ, `201` สร้างสำเร็จ, `204` ลบสำเร็จ, `400` ข้อมูลไม่ถูกต้อง, `401` ยังไม่ login, `403` ไม่มีสิทธิ์, `404` ไม่พบข้อมูล, `409` ข้อมูลซ้ำหรือสถานะไม่ถูกต้อง
- Error ทุก API ตอบเป็นรูปแบบเดียวกัน (`ErrorResponse`) จาก `GlobalExceptionHandler` และมี `fieldErrors` เมื่อข้อมูลไม่ผ่าน validation
- API ที่แก้ข้อมูลต้อง login ก่อน และต้องแนบ CSRF token (Swagger UI แนบให้อัตโนมัติ)

## Testing

```bash
cd code
./mvnw test
```

Repository test ใช้ PostgreSQL จริง ต้องตั้ง `TEST_DB_PASSWORD` ก่อนรัน

| รายการ | ผล |
| :--- | :---: |
| Tests ทั้งหมด | 445 (ผ่านทั้งหมด) |
| Line coverage | 84.7% |
| Branch coverage | 70.8% |

รายละเอียดและวิธีสร้างรายงานใหม่อยู่ใน [test/README.md](test/README.md)

## CI/CD

ใช้ GitHub Actions ([.github/workflows/ci.yml](.github/workflows/ci.yml))

```
push / pull request เข้า develop หรือ main
   └─ build-test
        ├─ เปิด PostgreSQL ชั่วคราว
        ├─ build + รัน test ทั้งหมด (mvnw package)
        ├─ เก็บ test report และ coverage report เป็น artifact
        └─ build Docker image
push เข้า main และ build-test ผ่าน
   └─ deploy → เรียก Render Deploy Hook → Render build และ deploy เวอร์ชันใหม่
```

URL ของ Deploy Hook เก็บเป็น GitHub secret (`RENDER_DEPLOY_HOOK_URL`) ไม่ได้อยู่ใน repository

## Deployment

| ส่วน | บริการ |
| :--- | :--- |
| Web application | Render (Docker, build จาก `code/Dockerfile`) |
| Database | Neon PostgreSQL (Singapore) |

ค่าที่เป็นความลับ (รหัสผ่าน database, remember-me key, hash รหัสผ่านบัญชีเดโม) ตั้งเป็น environment variable บน Render ไม่ได้เก็บใน repository

![Deployment Diagram](img/diagrams/deployment-diagram.png)

## ข้อจำกัดของระบบ

| ข้อจำกัด | แนวทางพัฒนาต่อ |
| :--- | :--- |
| ไม่มีระบบชำระเงินออนไลน์ (พนักงานยืนยันการรับเงิน) | เชื่อม PromptPay QR หรือ payment gateway |
| Render แบบฟรีปิดพอร์ต SMTP จึงส่งอีเมลจาก Live demo ไม่ได้ (ทดสอบอีเมลได้ผ่าน Docker + Mailpit) | ส่งผ่าน HTTP API เช่น Brevo |
| รูปที่อัปโหลดเก็บบน disk ของ server และหายเมื่อ Render deploy ใหม่ | เก็บบน cloud storage เช่น Cloudinary หรือ S3 |
| วันหยุดสำหรับคิดส่วนเพิ่มกำหนดไว้ในโค้ด | ทำตารางวันหยุดให้แอดมินจัดการ |
| ไม่มีการรีเซ็ตรหัสผ่านทางอีเมล | เพิ่มขั้นตอน reset password |

## เอกสารประกอบ

| เอกสาร | ที่อยู่ |
| :--- | :--- |
| Use Case Diagram | [img/diagrams/use-case.png](img/diagrams/use-case.png) |
| Use Case Description | [doc/use-case-description.md](doc/use-case-description.md) |
| Domain Model | [img/diagrams/domain-model.png](img/diagrams/domain-model.png) |
| Class Diagram | [img/diagrams/class-diagram.png](img/diagrams/class-diagram.png) |
| Sequence Diagrams | [ค้นหาห้อง](img/diagrams/sequence-search-room.png), [สร้างการจอง](img/diagrams/sequence-create-booking.png), [Check-out](img/diagrams/sequence-checkout.png), [Daily Care](img/diagrams/sequence-daily-care.png) |
| Activity Diagram | [img/diagrams/activity-booking.png](img/diagrams/activity-booking.png) |
| State Diagram | [img/diagrams/state-booking.png](img/diagrams/state-booking.png) |
| ER Diagram | [img/diagrams/er-diagram.png](img/diagrams/er-diagram.png) |
| Component Diagram | [img/diagrams/component-diagram.png](img/diagrams/component-diagram.png) |
| Deployment Diagram | [img/diagrams/deployment-diagram.png](img/diagrams/deployment-diagram.png) |
| Data Dictionary | [doc/data-dictionary](doc/data-dictionary) |
| Design Patterns | [doc/design-patterns.md](doc/design-patterns.md) |
| SOLID Analysis | [doc/solid-analysis](doc/solid-analysis) |
| Diagram source (PlantUML) | [doc/diagrams](doc/diagrams) |
| Test Report | [test/README.md](test/README.md) |
| Slides | [doc/slide/PetsHotel Slide.pdf](doc/slide/PetsHotel%20Slide.pdf) |