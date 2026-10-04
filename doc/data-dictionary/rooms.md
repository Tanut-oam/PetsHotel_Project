# Data Dictionary: rooms

ตาราง `rooms` เก็บข้อมูลห้องพักสัตว์เลี้ยง ได้แก่ ความจุ ราคา สถานะการเปิดให้จอง และรูปห้อง

| คอลัมน์ | ชนิดข้อมูล PostgreSQL | NULL ได้ | คีย์ / ข้อกำหนด | ความหมาย |
|---|---|---|---|---|
| `id` | BIGINT | ไม่ได้ | Primary Key, สร้างค่าแบบ IDENTITY | รหัสห้อง |
| `room_number` | VARCHAR(255) | ไม่ได้ | Unique | หมายเลขห้อง เช่น `101` |
| `name` | VARCHAR(255) | ไม่ได้ | — | ชื่อหรือประเภทห้อง เช่น `Deluxe` |
| `description` | VARCHAR(255) | ได้ | — | รายละเอียดห้อง |
| `capacity` | INTEGER | ไม่ได้ | อย่างน้อย 1 (ตรวจที่ชั้นคำขอ) | จำนวนสัตว์เลี้ยงสูงสุดที่ห้องรองรับ |
| `price_per_pet_per_night` | NUMERIC(10,2) | ไม่ได้ | ไม่ติดลบ (ตรวจที่ชั้นคำขอ) | ราคาต่อสัตว์ 1 ตัวต่อ 1 คืน (บาท) |
| `status` | VARCHAR(255) | ไม่ได้ | เก็บค่า enum เป็นข้อความ | สถานะห้อง: `ACTIVE` = เปิดให้จอง, `MAINTENANCE` = ปิดซ่อม, `INACTIVE` = ปิดใช้งาน |
| `image_url` | VARCHAR(500) | ได้ | — | path รูปห้อง เช่น `/uploads/rooms/<uuid>.jpg` ไม่มีรูปเป็น `NULL` |

## ความสัมพันธ์

- ห้องหนึ่งห้องมีการจองได้หลายรายการ: `bookings.room_id` → `rooms.id`
- ตาราง `rooms` ไม่มี Foreign Key ไปตารางอื่น

## กฎการทำงานที่เกี่ยวข้อง

- ห้องใหม่ที่สร้างผ่าน `RoomServiceImpl.createRoom()` มีสถานะ `ACTIVE` เสมอ
- `RoomServiceImpl` ตรวจหมายเลขห้องซ้ำก่อนบันทึกทั้งตอนสร้างและแก้ไข ซ้ำแล้วตอบ 409 นอกจากนี้ฐานข้อมูลยังมี Unique constraint กันอีกชั้น
- การปิดใช้งานห้อง (`deactivateRoom()`) เปลี่ยน `status` เป็น `INACTIVE` **ไม่ลบแถว** การจองเก่าที่อ้างถึงห้องจึงยังอยู่
- เฉพาะห้อง `ACTIVE` เท่านั้นที่แสดงให้ลูกค้าเห็น (`getActiveRooms()`, `/api/rooms/search`), ค้นหาห้องว่างได้ และจองได้ (`BookingServiceImpl` ปฏิเสธการจองห้องที่ไม่ใช่ `ACTIVE`)
- `CreateRoomRequest` และ `UpdateRoomRequest` ตรวจว่า `capacity` อย่างน้อย 1 และราคาไม่ติดลบ ทศนิยมไม่เกิน 2 ตำแหน่ง ข้อกำหนดนี้ตรวจที่ชั้นคำขอ ไม่ได้ประกาศเป็น `CHECK` constraint
- `image_url` กำหนดโดย `LocalFileStorageService` เท่านั้น รับเฉพาะไฟล์ JPG/PNG ขนาดไม่เกิน 5 MB ตั้งชื่อไฟล์ใหม่เป็น UUID และลบไฟล์เก่าเมื่อเปลี่ยนหรือลบรูป
- ราคาในตารางนี้คือราคาปัจจุบัน เมื่อจองแล้ว ราคา ณ เวลาที่จองจะถูกบันทึกไว้ใน `bookings.room_amount` ถ้าแก้ราคาห้องภายหลัง การจองเก่าจึงไม่เปลี่ยน

## แหล่งอ้างอิงในโค้ด

`Room`, `RoomStatus`, `RoomRepository`, `RoomServiceImpl`, `RoomMapper`, `CreateRoomRequest`, `UpdateRoomRequest`, `UpdateStatusRequest`, `LocalFileStorageService`