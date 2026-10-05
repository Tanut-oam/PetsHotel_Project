# Data Dictionary: daily_care_reports

ตาราง `daily_care_reports` เก็บรายงานการดูแลสัตว์เลี้ยงรายวันของสัตว์ในการจอง แต่ละรายงานระบุวันที่และผู้บันทึก

| คอลัมน์ | ชนิดข้อมูล PostgreSQL | NULL ได้ | คีย์ / ข้อกำหนด | ความหมาย | ตัวอย่างค่า |
|---|---|---|---|---|---|
| `id` | BIGINT | ไม่ได้ | Primary Key, IDENTITY BY DEFAULT | รหัสรายงาน | `101` |
| `booking_pet_id` | BIGINT | ไม่ได้ | Foreign Key → `booking_pets.id`; เป็นส่วนหนึ่งของ Unique Constraint | สัตว์เลี้ยงในการจองที่ได้รับการดูแล | `12` |
| `recorded_by_id` | BIGINT | ไม่ได้ | Foreign Key → `users.id` | ผู้บันทึกรายงาน | `3` |
| `report_date` | DATE | ไม่ได้ | เป็นส่วนหนึ่งของ Unique Constraint | วันที่รายงานการดูแล | `2026-10-03` |
| `feeding_morning` | VARCHAR(255) | ได้ | — | อาหารที่ให้ช่วงเช้า | `อาหารเม็ด 100 กรัม` |
| `feeding_evening` | VARCHAR(255) | ได้ | — | อาหารที่ให้ช่วงเย็น | `อาหารเปียก 1 ซอง` |
| `walking_minutes` | INTEGER | ได้ | — | ระยะเวลาเดินเล่นเป็นนาที | `15` |
| `grooming` | VARCHAR(255) | ได้ | — | การดูแลความสะอาด | `แปรงขน` |
| `mood` | VARCHAR(255) | ได้ | — | อารมณ์ของสัตว์เลี้ยง | `ร่าเริง` |
| `health_note` | VARCHAR(255) | ได้ | — | ข้อมูลสุขภาพประจำวัน | `สุขภาพปกติ` |
| `general_note` | VARCHAR(255) | ได้ | — | หมายเหตุเพิ่มเติม | `กินอาหารครบทั้งสองมื้อ` |
| `created_at` | TIMESTAMP(6) WITHOUT TIME ZONE | ไม่ได้ | ไม่อัปเดตหลังสร้างผ่าน Entity | เวลาที่สร้างรายงาน | `2026-10-03 18:30:00` |

> ตัวอย่างค่าเป็นข้อมูลสมมติเพื่อแสดงรูปแบบ ไม่ใช่ข้อมูลจริงหรือค่าเริ่มต้นของฐานข้อมูล คอลัมน์ที่อนุญาตให้เป็น NULL อาจไม่มีค่าได้

## ความสัมพันธ์

- รายงานหลายฉบับอ้างถึงสัตว์ในการจองหนึ่งรายการ: `booking_pet_id` → `booking_pets.id`
- ผู้ใช้หนึ่งคนบันทึกรายงานได้หลายฉบับ: `recorded_by_id` → `users.id`
- ความสัมพันธ์ทั้งสองใน Entity เป็น `ManyToOne` แบบ `LAZY` และต้องมีข้อมูลอ้างอิง

## ข้อกำหนดไม่ให้รายงานซ้ำ

Entity กำหนด Unique Constraint ชื่อ `uk_care_report_booking_pet_date` บนคอลัมน์ (`booking_pet_id`, `report_date`) จึงมีรายงานได้ไม่เกินหนึ่งฉบับต่อสัตว์ในการจองหนึ่งรายการต่อหนึ่งวัน

## กฎการทำงานที่เกี่ยวข้อง

- `DailyCareReportServiceImpl.createReport()` อนุญาตให้ `STAFF` หรือ `ADMIN` บันทึกรายงาน
- การจองต้องอยู่ในสถานะ `CHECKED_IN` หรือ `CHECKED_OUT`
- `report_date` ต้องอยู่ระหว่างวันเช็กอินและวันเช็กเอาต์ของการจอง
- `CreateDailyCareReportRequest` และ `UpdateDailyCareReportRequest` ตรวจให้ `walking_minutes` ไม่ติดลบ กฎนี้ไม่ได้ประกาศเป็น `CHECK` constraint ใน Entity
- เมื่อตารางได้รับรายงานใหม่ เมธอด `prePersist()` กำหนด `created_at` หากยังไม่มีค่า ค่าเวลานี้กำหนดจากแอปพลิเคชัน ไม่ใช่ค่าเริ่มต้นที่ประกาศในฐานข้อมูล

## แหล่งอ้างอิงในโค้ด

`DailyCareReport`, `BookingPet`, `DailyCareReportServiceImpl`, `DailyCareReportRepository`, `CreateDailyCareReportRequest`, `UpdateDailyCareReportRequest`