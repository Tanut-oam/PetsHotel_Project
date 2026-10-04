# Data Dictionary: daily_care_reports

ตาราง `daily_care_reports` เก็บรายงานการดูแลสัตว์เลี้ยงรายวันของสัตว์ในการจอง แต่ละรายงานระบุวันที่และผู้บันทึก

| คอลัมน์ | ชนิดข้อมูล PostgreSQL | NULL ได้ | คีย์ / ข้อกำหนด | ความหมาย |
|---|---|---|---|---|
| `id` | BIGINT | ไม่ได้ | Primary Key, สร้างค่าแบบ IDENTITY | รหัสรายงาน |
| `booking_pet_id` | BIGINT | ไม่ได้ | Foreign Key → `booking_pets.id`; เป็นส่วนหนึ่งของ Unique Constraint | สัตว์เลี้ยงในการจองที่ได้รับการดูแล |
| `recorded_by_id` | BIGINT | ไม่ได้ | Foreign Key → `users.id` | ผู้บันทึกรายงาน |
| `report_date` | DATE | ไม่ได้ | เป็นส่วนหนึ่งของ Unique Constraint | วันที่รายงานการดูแล |
| `feeding_morning` | VARCHAR(255) | ได้ | — | อาหารที่ให้ช่วงเช้า |
| `feeding_evening` | VARCHAR(255) | ได้ | — | อาหารที่ให้ช่วงเย็น |
| `walking_minutes` | INTEGER | ได้ | — | ระยะเวลาเดินเล่นเป็นนาที |
| `grooming` | VARCHAR(255) | ได้ | — | การดูแลความสะอาด |
| `mood` | VARCHAR(255) | ได้ | — | อารมณ์ของสัตว์เลี้ยง |
| `health_note` | VARCHAR(255) | ได้ | — | ข้อมูลสุขภาพประจำวัน |
| `general_note` | VARCHAR(255) | ได้ | — | หมายเหตุเพิ่มเติม |
| `created_at` | TIMESTAMP | ไม่ได้ | ไม่อัปเดตหลังสร้างผ่าน Entity | เวลาที่สร้างรายงาน |

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