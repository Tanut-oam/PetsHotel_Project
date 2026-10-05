# Data Dictionary: users

ตาราง `users` เก็บบัญชีผู้ใช้ทุกบทบาท ได้แก่ ลูกค้า พนักงาน และผู้ดูแลระบบ ใช้ทั้งสำหรับเข้าสู่ระบบและอ้างอิงเจ้าของข้อมูลในตารางอื่น

| คอลัมน์ | ชนิดข้อมูล PostgreSQL | NULL ได้ | คีย์ / ข้อกำหนด | ความหมาย |
|---|---|---|---|---|
| `id` | BIGINT | ไม่ได้ | Primary Key, สร้างค่าแบบ IDENTITY | รหัสผู้ใช้ |
| `first_name` | VARCHAR(255) | ไม่ได้ | — | ชื่อ |
| `last_name` | VARCHAR(255) | ไม่ได้ | — | นามสกุล |
| `email` | VARCHAR(255) | ไม่ได้ | Unique (`uk_users_email`) | อีเมลสำหรับเข้าสู่ระบบ เก็บเป็นตัวพิมพ์เล็กเสมอ |
| `password` | VARCHAR(255) | ไม่ได้ | เก็บเป็น BCrypt hash | รหัสผ่านที่เข้ารหัสแล้ว ไม่เก็บรหัสจริง |
| `phone_number` | VARCHAR(255) | ไม่ได้ | — | เบอร์โทรศัพท์ |
| `role` | VARCHAR(255) | ไม่ได้ | เก็บค่า enum เป็นข้อความ, CHECK เฉพาะ 3 ค่า (`ck_users_role`) | บทบาท: `CUSTOMER`, `STAFF` หรือ `ADMIN` |
| `active` | BOOLEAN | ไม่ได้ | DEFAULT `TRUE` | `true` = ใช้งานได้, `false` = ถูกระงับ เข้าสู่ระบบไม่ได้ |
| `created_at` | TIMESTAMP | ไม่ได้ | กำหนดอัตโนมัติตอนสร้าง | เวลาที่สร้างบัญชี |

## ความสัมพันธ์

- ผู้ใช้หนึ่งคนเป็นเจ้าของสัตว์เลี้ยงได้หลายตัว: `pets.owner_id` → `users.id`
- ผู้ใช้หนึ่งคนมีการจองได้หลายรายการ: `bookings.user_id` → `users.id`
- ผู้ใช้หนึ่งคน (พนักงานหรือผู้ดูแลระบบ) บันทึกรายงานการดูแลได้หลายฉบับ: `daily_care_reports.recorded_by_id` → `users.id`
- ตาราง `users` ไม่มี Foreign Key ไปตารางอื่น

## Index

- `email` มี index อัตโนมัติจาก Unique constraint ใช้ตอนเข้าสู่ระบบ (`findByEmail`)
- ตารางที่อ้างถึง `users` มี index บน Foreign Key: `idx_pets_owner_id`, `idx_bookings_user_id`, `idx_care_reports_recorded_by`

## กฎการทำงานที่เกี่ยวข้อง

- เมื่อสมัครสมาชิก `AuthServiceImpl.register()` แปลงอีเมลเป็นตัวพิมพ์เล็ก ตรวจอีเมลซ้ำ เข้ารหัสรหัสผ่านด้วย BCrypt และกำหนด `role = CUSTOMER`, `active = true` ผู้สมัครเลือกบทบาทเองไม่ได้
- `User.prePersist()` กำหนด `created_at` และ `active = true` ถ้ายังไม่มีค่า
- ตอนเข้าสู่ระบบ `CustomUserDetailsService` ค้นหาด้วยอีเมลตัวพิมพ์เล็ก และปิดการใช้งานบัญชีที่ `active = false` ผู้ใช้ที่ถูกระงับจึงเข้าสู่ระบบไม่ได้
- ผู้ดูแลระบบแก้ไขชื่อ อีเมล เบอร์โทร บทบาท และสถานะได้ผ่าน `UserServiceImpl.updateUser()` แต่**เปลี่ยนบทบาท ระงับบัญชี หรือเปลี่ยนอีเมลของตัวเองไม่ได้** เพื่อป้องกันการล็อกตัวเองออกจากระบบ
- `RegisterRequest` และ `UpdateUserRequest` ตรวจรูปแบบอีเมล และเบอร์โทรต้องขึ้นต้นด้วย 0 ยาว 9–10 หลัก (`^0\d{8,9}$`) ข้อกำหนดนี้ตรวจที่ชั้นคำขอ ไม่ได้ประกาศเป็น CHECK constraint
- ไม่มีการลบแถวผู้ใช้ ใช้การระงับ (`active = false`) แทน ข้อมูลการจองและรายงานที่อ้างถึงผู้ใช้จึงไม่หาย

## ข้อมูลตัวอย่าง

`V2__insert_sample_data.sql` สร้างบัญชีเดโม 2 บัญชี คือ `admin@petshotel.test` (ADMIN) และ `staff@petshotel.test` (STAFF) รหัสผ่านมาจาก Flyway placeholder `demopasswordhash`
- เครื่องนักพัฒนาและ Docker ใช้ค่าใน `application.properties`
- เว็บที่ deploy ตั้งค่าแยกผ่าน environment variable `SPRING_FLYWAY_PLACEHOLDERS_DEMOPASSWORDHASH` รหัสผ่านจริงจึงไม่อยู่ใน repository

## แหล่งอ้างอิงในโค้ด

`User`, `UserRole`, `UserRepository`, `AuthServiceImpl`, `CustomUserDetailsService`, `UserServiceImpl`, `RegisterRequest`, `UpdateUserRequest`, `db/migration/V1__create_tables.sql`, `db/migration/V2__insert_sample_data.sql`