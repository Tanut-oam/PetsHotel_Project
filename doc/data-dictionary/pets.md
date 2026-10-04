# Data Dictionary: pets

ตาราง `pets` เก็บข้อมูลสัตว์เลี้ยง โดยสัตว์เลี้ยงแต่ละตัวมีเจ้าของหนึ่งบัญชี

| คอลัมน์ | ชนิดข้อมูล PostgreSQL | NULL ได้ | คีย์ / ข้อกำหนด | ความหมาย |
|---|---|---|---|---|
| `id` | BIGINT | ไม่ได้ | Primary Key, สร้างค่าแบบ IDENTITY | รหัสสัตว์เลี้ยง |
| `name` | VARCHAR(255) | ไม่ได้ | — | ชื่อสัตว์เลี้ยง |
| `type` | VARCHAR(255) | ไม่ได้ | เก็บค่า enum เป็นข้อความ | ประเภทสัตว์เลี้ยง: `DOG`, `CAT` หรือ `OTHER` |
| `breed` | VARCHAR(255) | ได้ | — | สายพันธุ์ |
| `age` | INTEGER | ได้ | — | อายุเป็นปี |
| `weight` | DOUBLE PRECISION | ได้ | — | น้ำหนักเป็นกิโลกรัม |
| `medical_note` | VARCHAR(255) | ได้ | — | ข้อมูลสุขภาพหรือโรคประจำตัว |
| `feeding_instruction` | VARCHAR(255) | ได้ | — | คำแนะนำเรื่องอาหาร |
| `special_note` | VARCHAR(255) | ได้ | — | ข้อควรดูแลเพิ่มเติม |
| `image_url` | VARCHAR(255) | ได้ | — | ตำแหน่งรูปภาพสัตว์เลี้ยง |
| `gender` | VARCHAR(255) | ได้ | — | เพศของสัตว์เลี้ยง |
| `owner_id` | BIGINT | ไม่ได้ | Foreign Key → `users.id` | บัญชีเจ้าของสัตว์เลี้ยง |
| `active` | BOOLEAN | ไม่ได้ | — | `true` = ยังใช้งาน, `false` = นำออกจากรายการที่ใช้งาน |

## ความสัมพันธ์

- สัตว์เลี้ยงหลายตัวเป็นของผู้ใช้หนึ่งคน: `pets.owner_id` อ้างถึง `users.id`
- ใน Entity ความสัมพันธ์ `Pet.owner` เป็น `ManyToOne` และโหลดข้อมูลแบบ `LAZY`

## กฎการทำงานที่เกี่ยวข้อง

- เมื่อเพิ่มสัตว์เลี้ยง `PetServiceImpl.createPet()` กำหนด `active = true`
- เมื่อลูกค้านำสัตว์เลี้ยงออก `PetServiceImpl.deactivatePet()` กำหนด `active = false` โดยไม่ได้ลบแถวข้อมูล
- การค้นหารายการสัตว์เลี้ยงของเจ้าของแสดงเฉพาะแถวที่ `active = true`
- `CreatePetRequest` และ `UpdatePetRequest` ตรวจให้อายุไม่ติดลบ และน้ำหนักมากกว่าศูนย์ ข้อกำหนดสองข้อนี้ตรวจที่ชั้นคำขอ ไม่ได้ประกาศเป็น `CHECK` constraint ใน Entity

## แหล่งอ้างอิงในโค้ด

`Pet`, `PetType`, `PetServiceImpl`, `CreatePetRequest`, `UpdatePetRequest`