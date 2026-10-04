# Data Dictionary: booking_extra_services

ตาราง `booking_extra_services` เก็บรายการบริการเสริมที่เลือกในการจองของ petshotel โดยเชื่อมการจอง บริการเสริม และสัตว์เลี้ยงผู้รับบริการ พร้อมเก็บจำนวนและราคา ณ เวลาจอง

| คอลัมน์ | ชนิดข้อมูล PostgreSQL | NULL ได้ | คีย์ / ข้อกำหนด | ความหมาย | ตัวอย่าง |
|---|---|---|---|---|---|
| `id` | BIGINT | ไม่ได้ | Primary Key, สร้างค่าแบบ IDENTITY | รหัสรายการบริการเสริมที่จอง | `1` |
| `booking_id` | BIGINT | ไม่ได้ | Foreign Key → `bookings.id` | การจองที่รายการบริการนี้เป็นส่วนหนึ่ง | `101` |
| `extra_service_id` | BIGINT | ไม่ได้ | Foreign Key → `extra_services.id` | บริการเสริมที่เลือก | `1` หมายถึงบริการอาบน้ำ |
| `booking_pet_id` | BIGINT | ได้ | Foreign Key → `booking_pets.id` | รายการสัตว์เลี้ยงในการจองที่เป็นผู้รับบริการ | `201` |
| `quantity` | INTEGER | ไม่ได้ | — | จำนวนหน่วยบริการในรายการนี้ | `1` |
| `unit_price` | NUMERIC(10,2) | ไม่ได้ | ทศนิยม 2 ตำแหน่ง | ราคาต่อหน่วย ณ เวลาจอง หน่วยเป็นบาท | `250.00` |
| `total_price` | NUMERIC(10,2) | ไม่ได้ | ทศนิยม 2 ตำแหน่ง | ราคารวมของรายการบริการ หน่วยเป็นบาท | `250.00` |

## ความสัมพันธ์

- รายการบริการเสริมหลายรายการเป็นส่วนหนึ่งของการจองหนึ่งรายการ: `booking_extra_services.booking_id` อ้างถึง `bookings.id`
- รายการบริการเสริมหลายรายการสามารถเลือกบริการเดียวกันได้: `booking_extra_services.extra_service_id` อ้างถึง `extra_services.id`
- รายการบริการเสริมหลายรายการสามารถมีสัตว์เลี้ยงผู้รับบริการตัวเดียวกันได้: `booking_extra_services.booking_pet_id` อ้างถึง `booking_pets.id`
- `booking_pet_id` อ้างถึงรายการสัตว์เลี้ยงในการจอง ไม่ได้อ้างถึง `pets.id` โดยตรง
- ใน Entity ความสัมพันธ์ `booking`, `extraService` และ `bookingPet` เป็น `ManyToOne` และโหลดข้อมูลแบบ `LAZY`
- `Booking.extraServices` เป็น `OneToMany` โดยกำหนด `cascade = ALL` และ `orphanRemoval = true`

## กฎการทำงานที่เกี่ยวข้อง

- `BookingServiceImpl.addSelectedServices()` สร้างรายการบริการแยกตามสัตว์เลี้ยงผู้รับบริการ
- สัตว์เลี้ยงผู้รับบริการต้องอยู่ในการจองนั้น และไม่สามารถเลือกสัตว์ตัวเดิมซ้ำสำหรับบริการเดียวกันในคำขอสร้างการจองได้
- แม้ Entity อนุญาตให้ `booking_pet_id` เป็น `NULL` แต่ขั้นตอนสร้างการจองปัจจุบันกำหนดผู้รับบริการให้ทุกรายการที่สร้าง
- บริการที่เลือกต้องมีอยู่ เปิดใช้งาน มีราคา และราคาไม่ติดลบ
- ขั้นตอนสร้างการจองปัจจุบันกำหนด `quantity = 1` และคัดลอก `ExtraService.price` ไปเป็น `unit_price` และ `total_price`
- `Booking.addExtraService()` เพิ่มรายการลงใน `extraServices` และกำหนดการจองให้รายการนั้นด้วย `service.setBooking(this)`
- ราคาที่บันทึกเป็นราคา ณ เวลาจอง การแก้ราคาบริการใน `extra_services` ภายหลังจึงไม่เปลี่ยนราคาของรายการที่จองไว้โดยอัตโนมัติ
- `ExtraServicePricingStrategy` ตรวจว่า `unit_price` ไม่เป็น `NULL` และไม่ติดลบ ส่วน `quantity` ต้องไม่เป็น `NULL` และมากกว่า 0
- Strategy คำนวณราคาแต่ละรายการด้วย `unit_price × quantity` หากรายการมี `total_price` อยู่แล้ว ค่านั้นต้องเท่ากับผลคำนวณ
- Strategy รวมผลคำนวณของทุกรายการเป็นค่าบริการเสริมของการจอง
- `BookingExtraServiceRepository.findByBookingId()` ใช้ค้นหารายการบริการเสริมของการจองที่ระบุ
- กฎตรวจจำนวน ราคา และผู้รับบริการข้างต้นตรวจในโค้ด ไม่ได้ประกาศเป็น `CHECK` constraint ใน Entity และไม่ได้ประกาศ `UNIQUE` constraint สำหรับคู่บริการกับผู้รับบริการ

### ตัวอย่างรายการบริการที่จอง

สมมติว่าการจองรหัส 101 มีสัตว์เลี้ยง 2 ตัว โดยรายการใน `booking_pets` มีรหัส 201 และ 202 ทั้งสองตัวเลือกบริการอาบน้ำรหัส 1 ราคา 250.00 บาท ระบบจะสร้างรายการดังนี้

| `id` | `booking_id` | `extra_service_id` | `booking_pet_id` | `quantity` | `unit_price` | `total_price` |
|---|---|---|---|---|---|---|
| 1 | 101 | 1 | 201 | 1 | 250.00 | 250.00 |
| 2 | 101 | 1 | 202 | 1 | 250.00 | 250.00 |

- ค่าบริการเสริมรวม: `(250.00 × 1) + (250.00 × 1) = 500.00` บาท
- หากแก้ราคาบริการอาบน้ำเป็น 300.00 บาทภายหลัง สองรายการเดิมยังคงเก็บ `unit_price = 250.00` และ `total_price = 250.00`
- หากส่งสัตว์เลี้ยงที่ไม่ได้อยู่ในการจองรหัส 101 มาเป็นผู้รับบริการ ระบบจะปฏิเสธคำขอสร้างการจองนั้น

## แหล่งอ้างอิงในโค้ด

`BookingExtraService`, `Booking`, `BookingPet`, `ExtraService`,
`BookingServiceImpl`, `ExtraServicePricingStrategy`,
`BookingExtraServiceRepository`