# Data Dictionary: bookings

ตาราง `bookings` เก็บข้อมูลการจองห้องพักของ PetsHotel ได้แก่ ผู้จอง ห้อง ช่วงวันเข้าพัก สถานะการจอง โปรโมชัน ราคา ณ เวลาจอง และข้อมูลการชำระเงิน

ชนิดข้อมูล PostgreSQL อ้างอิงจาก Entity `Booking` และ ER Diagram ปัจจุบันของโปรเจกต์

| คอลัมน์ | ชนิดข้อมูล PostgreSQL | NULL ได้ | คีย์ / ข้อกำหนด | ความหมาย |
|---|---|---|---|---|
| `id` | BIGINT | ไม่ได้ | Primary Key, สร้างค่าแบบ IDENTITY | รหัสการจอง |
| `user_id` | BIGINT | ไม่ได้ | Foreign Key → `users.id` | ผู้ใช้ที่เป็นเจ้าของการจอง |
| `room_id` | BIGINT | ไม่ได้ | Foreign Key → `rooms.id` | ห้องพักที่จอง |
| `promotion_id` | BIGINT | ได้ | Foreign Key → `promotions.id` | โปรโมชันที่เลือกใช้ ไม่มีโปรโมชันเป็น `NULL` |
| `check_in_date` | DATE | ไม่ได้ | ตรวจช่วงวันที่ใน Service | วันเริ่มเข้าพัก |
| `check_out_date` | DATE | ไม่ได้ | ต้องหลัง `check_in_date` ตรวจใน Service | วันสิ้นสุดการเข้าพัก |
| `status` | VARCHAR(255) | ไม่ได้ | เก็บ `BookingStatus` เป็นข้อความ | สถานะการจอง |
| `room_amount` | NUMERIC(10,2) | ได้ | กำหนด precision 10, scale 2 | ค่าห้องพัก ณ เวลาจอง หน่วยเป็นบาท |
| `service_amount` | NUMERIC(10,2) | ได้ | กำหนด precision 10, scale 2 | ค่าบริการเสริมรวม ณ เวลาจอง หน่วยเป็นบาท |
| `surcharge_amount` | NUMERIC(10,2) | ได้ | กำหนด precision 10, scale 2 | ค่าธรรมเนียมเพิ่มเติม เช่น ค่าบริการวันหยุด หน่วยเป็นบาท |
| `discount_amount` | NUMERIC(10,2) | ได้ | กำหนด precision 10, scale 2 | ส่วนลดที่เลือกใช้ในการจอง หน่วยเป็นบาท |
| `total_price` | NUMERIC(38,2) | ได้ | ชนิดตาม ER Diagram; Entity ไม่กำหนด precision/scale เอง | ยอดสุทธิของการจอง หน่วยเป็นบาท |
| `promotion_name` | VARCHAR(255) | ได้ | `updatable = false` ใน JPA mapping | ชื่อโปรโมชัน ณ เวลาจอง ไม่มีโปรโมชันเป็น `NULL` |
| `payment_status` | VARCHAR(255) | ไม่ได้ | เก็บ `PaymentStatus` เป็นข้อความ | สถานะการชำระเงิน |
| `paid_amount` | NUMERIC(10,2) | ได้ | กำหนด precision 10, scale 2 | ยอดเงินที่ยืนยันรับชำระ ยังไม่รับชำระเป็น `NULL` |
| `paid_at` | TIMESTAMP | ได้ | กำหนดเมื่อยืนยันรับชำระเงิน | วันและเวลารับชำระเงิน ยังไม่รับชำระเป็น `NULL` |
| `created_at` | TIMESTAMP | ไม่ได้ | กำหนดใน `@PrePersist` หากยังไม่มีค่า | วันและเวลาที่สร้างการจอง |

## ค่าสถานะ

### สถานะการจอง: `status`

| ค่า | ความหมาย |
|---|---|
| `PENDING` | รอพนักงานยืนยันการจอง |
| `CONFIRMED` | ยืนยันการจองแล้ว |
| `CHECKED_IN` | สัตว์เลี้ยงเข้าพักแล้ว |
| `CHECKED_OUT` | สิ้นสุดการเข้าพักแล้ว |
| `CANCELLED` | ยกเลิกการจองแล้ว |

### สถานะการชำระเงิน: `payment_status`

| ค่า | ความหมาย |
|---|---|
| `UNPAID` | ยังไม่ได้ยืนยันรับชำระเงิน |
| `PAID` | ยืนยันรับชำระเงินแล้ว |

`status` และ `payment_status` เป็นคนละข้อมูล การเปลี่ยนสถานะการจองไม่ได้เปลี่ยนสถานะการชำระเงินโดยอัตโนมัติ

## ความสัมพันธ์

- ผู้ใช้หนึ่งคนมีการจองได้หลายรายการ: `bookings.user_id` → `users.id`
- ห้องหนึ่งห้องมีการจองได้หลายรายการในประวัติ: `bookings.room_id` → `rooms.id` แต่ระบบไม่อนุญาตให้มีการจองที่ยังครองห้องทับช่วงวันกัน
- การจองหนึ่งรายการเลือกโปรโมชันได้ไม่เกินหนึ่งรายการ: `bookings.promotion_id` → `promotions.id` โดยไม่บังคับเลือก
- การจองหนึ่งรายการมีสัตว์เลี้ยงผ่านตาราง `booking_pets`: `booking_pets.booking_id` → `bookings.id`
- การจองหนึ่งรายการมีบริการเสริมได้หลายรายการ: `booking_extra_services.booking_id` → `bookings.id`
- การจองหนึ่งรายการมีใบเสร็จได้ไม่เกินหนึ่งใบ: `receipts.booking_id` → `bookings.id` โดย `receipts.booking_id` มี Unique constraint
- ใน Entity ความสัมพันธ์ `user`, `room` และ `promotion` เป็น `ManyToOne` และโหลดแบบ `LAZY`
- `Booking.bookingPets` และ `Booking.extraServices` เป็น `OneToMany` โดยกำหนด `cascade = ALL` และ `orphanRemoval = true`
- `bookingPets` และ `extraServices` เป็นรายการความสัมพันธ์ใน Java ไม่ใช่คอลัมน์ของตาราง `bookings`

## กฎการทำงานที่เกี่ยวข้อง

### การสร้างการจอง

- Controller กำหนดผู้จองจากบัญชีที่ล็อกอิน ไม่ยอมให้ลูกค้าระบุผู้จองเป็นบัญชีอื่น
- ต้องระบุวันเข้าและวันออก โดย `check_out_date` ต้องหลัง `check_in_date`
- ต้องเลือกสัตว์เลี้ยงอย่างน้อยหนึ่งตัว รหัสสัตว์ต้องไม่ซ้ำ สัตว์ต้องยังใช้งานอยู่และเป็นของผู้จอง
- ห้องต้องมีสถานะ `ACTIVE` และจำนวนสัตว์ต้องไม่เกินความจุห้อง
- ก่อนบันทึก ระบบตรวจว่าสัตว์และห้องไม่มีการจองซ้อนในช่วงวันที่เลือก
- สถานะที่นับว่าสงวนห้องและสัตว์คือ `PENDING`, `CONFIRMED` และ `CHECKED_IN`
- การจองใหม่ที่สร้างผ่าน `BookingServiceImpl` มีสถานะ `PENDING` และสถานะชำระเงิน `UNPAID`
- `Booking.prePersist()` กำหนด `status`, `paymentStatus` และ `createdAt` เมื่อค่านั้นยังเป็น `NULL` โดย `paymentStatus` มีค่าเริ่มต้น `UNPAID` ใน Entity ด้วย
- ค่าเริ่มต้นเหล่านี้กำหนดในโค้ด Java ไม่ได้ประกาศเป็น SQL `DEFAULT` ใน Entity

### ราคาและโปรโมชัน

- `BookingServiceImpl` เรียก `PricingService.calculate()` แล้วเก็บค่าห้อง บริการเสริม ค่าธรรมเนียม ส่วนลด และยอดสุทธิลงในการจอง
- ยอดสุทธิคำนวณจากค่าห้อง + ค่าบริการเสริม + ค่าธรรมเนียม − ส่วนลดที่เลือกใช้
- ราคาที่บันทึกเป็นราคา ณ เวลาจอง การแก้ราคาห้อง บริการ หรือโปรโมชันภายหลังไม่คำนวณราคาการจองเดิมใหม่โดยอัตโนมัติ
- หากเลือกโปรโมชัน ระบบตรวจว่าโปรโมชันมีอยู่ เปิดใช้งาน และใช้ได้ในวันทำการจองตามเขตเวลา `Asia/Bangkok`
- ระบบเก็บทั้ง `promotion_id` และ `promotion_name` เพื่ออ้างอิงโปรโมชันและเก็บชื่อ ณ เวลาจอง
- `promotion_name` กำหนด `updatable = false` เพื่อไม่ให้ JPA อัปเดตคอลัมน์นี้หลังสร้างรายการ
- แม้คอลัมน์ราคาใน Entity อนุญาตให้เป็น `NULL` แต่ขั้นตอนสร้างการจองปัจจุบันคำนวณและกำหนดราคาก่อนบันทึก

### การเปลี่ยนสถานะและการชำระเงิน

- การยืนยัน เช็กอิน เช็กเอาต์ และยกเลิกใช้ State Pattern ผ่าน `BookingServiceImpl`
- การแก้สถานะโหลดการจองผ่าน `findByIdForUpdate()` ที่ใช้ `PESSIMISTIC_WRITE` lock ภายใน transaction
- ยกเลิกได้เฉพาะ `PENDING` หรือ `CONFIRMED` ที่ยังไม่ชำระเงิน
- การยกเลิกเปลี่ยนสถานะเป็น `CANCELLED` โดยไม่ลบข้อมูลการจองและรายการที่เกี่ยวข้อง
- การยืนยันรับชำระเงินครั้งแรกทำได้เมื่อสถานะเป็น `CONFIRMED`, `CHECKED_IN` หรือ `CHECKED_OUT` และยอดสุทธิไม่เป็น `NULL` หรือค่าติดลบ
- `PaymentServiceImpl.confirmPayment()` กำหนด `payment_status = PAID`, `paid_amount = total_price` และ `paid_at` เป็นเวลาปัจจุบันในเขตเวลา `Asia/Bangkok` พร้อมประสานการสร้างใบเสร็จ
- `paid_at` และ `created_at` ใช้ `LocalDateTime` จึงไม่ได้เก็บชื่อเขตเวลาหรือ UTC offset ไว้ในตัวค่า โดย `created_at` กำหนดด้วย `LocalDateTime.now()` ตามเวลาของระบบ
- กฎช่วงวันที่ การตรวจห้อง/สัตว์ซ้อน และเงื่อนไขการทำรายการข้างต้นตรวจในโค้ด ไม่ได้ประกาศเป็น `CHECK` constraint ของกฎเหล่านี้ใน Entity

## แหล่งอ้างอิงในโค้ด

`Booking`, `BookingStatus`, `PaymentStatus`, `BookingPet`, `Receipt`,
`BookingController`, `BookingRestController`, `BookingServiceImpl`,
`AvailabilityServiceImpl`, `PaymentServiceImpl`, `BookingRepository`,
`PricingServiceImpl`, `BookingMapper`

ชนิดคอลัมน์ประกอบการอ้างอิงจาก `doc/diagrams/er-diagram.puml`