# Diagram ส่วนการจอง — กิติญาดา

เอกสารอธิบาย State Diagram, Sequence Diagram และ Activity Diagram ของระบบ PetsHotel โดยอ้างอิงชื่อคลาส เมธอด และตัวแปรจากโค้ดส่วนการจอง

## 1. State Diagram: สถานะการจอง

ต้นฉบับ: [state-booking.puml](diagrams/state-booking.puml)

เส้นทางหลักของการจอง:

`PENDING → CONFIRMED → CHECKED_IN → CHECKED_OUT`

การจองสถานะ `PENDING` หรือ `CONFIRMED` สามารถเปลี่ยนเป็น `CANCELLED` ได้เมื่อยังไม่ได้ชำระเงิน

| การทำงาน | สถานะก่อน | สถานะหลัง | เงื่อนไข |
|---|---|---|---|
| สร้างการจอง | — | `PENDING` | ข้อมูลและความพร้อมของห้องและสัตว์เลี้ยงผ่านการตรวจสอบ |
| ยืนยันการจอง | `PENDING` | `CONFIRMED` | ดำเนินการโดยพนักงานหรือผู้ดูแลระบบ |
| เช็กอิน | `CONFIRMED` | `CHECKED_IN` | `checkInDate <= currentDate < checkOutDate` |
| เช็กเอาต์ | `CHECKED_IN` | `CHECKED_OUT` | `currentDate >= checkOutDate` |
| ยกเลิกการจอง | `PENDING`, `CONFIRMED` | `CANCELLED` | เจ้าของการจองหรือพนักงาน/ผู้ดูแลระบบ และ `paymentStatus != PAID` |

- `currentDate` ใช้เขตเวลา `Asia/Bangkok`
- `BookingStatus` และ `PaymentStatus` เป็นสถานะแยกกัน โค้ดปัจจุบันไม่ได้บังคับให้ชำระเงินก่อนเช็กเอาต์
- `PENDING`, `CONFIRMED` และ `CHECKED_IN` เป็นสถานะที่นับว่าครองห้องและสัตว์เลี้ยง
- `CHECKED_OUT` และ `CANCELLED` เป็นสถานะสิ้นสุด โดยยังเก็บข้อมูลการจองไว้ในฐานข้อมูล

![Booking State Diagram](../img/diagrams/state-booking.png)

## 2. Sequence Diagram: สร้างการจอง

ต้นฉบับ: [sequence-create-booking.puml](diagrams/sequence-create-booking.puml)

แสดงการส่งฟอร์มผ่าน `POST /bookings` หลังลูกค้าเข้าสู่ระบบและกรอกข้อมูลการจอง

1. `BookingController` ดึงผู้ใช้ผ่าน `CurrentUserServiceImpl.getByEmail(principal.getName())`
2. กำหนด `form.setUserId(user.getId())` จากบัญชีที่เข้าสู่ระบบ และตรวจข้อมูลฟอร์ม
3. เรียก `BookingServiceImpl.createBooking(form)` ซึ่งใช้ `prepareBooking(request, true)`
4. ตรวจข้อมูล โหลดผู้ใช้ ล็อกห้องและสัตว์เลี้ยง แล้วตรวจสถานะห้อง เจ้าของสัตว์ ความพร้อมใช้ และความจุ
5. ตรวจการจองสัตว์เลี้ยงซ้อนช่วงวันที่ ก่อนตรวจห้องว่างผ่าน `AvailabilityServiceImpl.checkRoomAvailable(...)`
6. สร้าง `Booking` และ `BookingPet` พร้อมตรวจบริการเสริมและโปรโมชันที่เลือก
7. คำนวณราคาผ่าน `PricingServiceImpl.calculate(PricingContext)` และเก็บราคา ณ เวลาจอง
8. บันทึกผ่าน `BookingRepository.save(...)` โดยใช้ cascade บันทึก `BookingPet` และ `BookingExtraService`
9. แปลงผลลัพธ์ผ่าน `BookingMapper.toResponse(...)` และกลับไปหน้ารายละเอียดการจองหลัง transaction สำเร็จ

เมื่อข้อมูลหรือเงื่อนไขไม่ถูกต้อง ระบบหยุดขั้นตอนที่เหลือ ไม่บันทึกการจอง และกลับไปแสดงฟอร์มพร้อมข้อผิดพลาด

`Lookup Repositories` เป็นกลุ่มที่รวม `UserRepository`, `RoomRepository`, `PetRepository`, `BookingPetRepository`, `ExtraServiceRepository` และ `PromotionRepository` เพื่อให้อ่านภาพง่าย ไม่ใช่คลาสใหม่ในระบบ

![Create Booking Sequence](../img/diagrams/sequence-create-booking.png)

## 3. Activity Diagram: กระบวนการจอง

ต้นฉบับ: [activity-booking.puml](diagrams/activity-booking.puml)

แสดงกระบวนการตั้งแต่เลือกห้องจนสร้างการจอง โดยแบ่งหน้าที่ระหว่าง Customer และ PetsHotel System

1. ลูกค้าเลือกห้อง ช่วงวันเข้าพัก สัตว์เลี้ยง และเลือกบริการเสริมหรือโปรโมชันได้
2. ระบบตรวจข้อมูล แสดงสัตว์ที่ไม่ติดจอง และคำนวณราคาโดยประมาณ
3. ลูกค้าตรวจรายละเอียดและยืนยันการจอง
4. ระบบตรวจข้อมูลและความพร้อมของห้องกับสัตว์เลี้ยงอีกครั้ง ก่อนคำนวณราคาและบันทึก
5. ลูกค้าเห็นรายละเอียดการจองสถานะ `PENDING` และสถานะชำระเงิน `UNPAID`

การคำนวณราคาโดยประมาณใช้ `prepareBooking(request, false)` จึงไม่บันทึก ไม่ล็อกข้อมูล และไม่ตรวจความพร้อมขั้นสุดท้าย ส่วนการสร้างจริงใช้ `prepareBooking(request, true)` เพื่อตรวจซ้ำภายใน transaction

หากพบข้อผิดพลาด ระบบแสดงข้อความให้ลูกค้าแก้ข้อมูลและลองใหม่ การยืนยันการจอง เช็กอิน เช็กเอาต์ และยกเลิกหลังสร้างการจองแสดงใน State Diagram

![Booking Activity Diagram](../img/diagrams/activity-booking.png)

## ตัวแปรที่ใช้ใน Diagram

| ตัวแปร | ชนิด / ความหมาย | แหล่งอ้างอิง |
|---|---|---|
| `userId` | `Long` — กำหนดจากบัญชีที่เข้าสู่ระบบ | `CreateBookingRequest`, `BookingController` |
| `roomId` | `Long` — ห้องที่เลือก | `CreateBookingRequest` |
| `petIds` | `List<Long>` — สัตว์เลี้ยงที่เข้าพัก ต้องไม่ซ้ำและเป็นของผู้จอง | `CreateBookingRequest`, `BookingServiceImpl` |
| `checkInDate`, `checkOutDate` | `LocalDate` — วันเช็กเอาต์ต้องอยู่หลังวันเช็กอิน | `CreateBookingRequest`, `Booking` |
| `servicePetIds` | `Map<Long, List<Long>>` — จับคู่รหัสบริการกับรหัสสัตว์ที่รับบริการ | `CreateBookingRequest` |
| `promotionId` | `Long` — โปรโมชันที่เลือก ไม่บังคับระบุ | `CreateBookingRequest` |
| `bookingPets`, `extraServices` | รายการสัตว์และบริการเสริมของการจอง บันทึกผ่าน cascade | `Booking` |
| `bookingPetsByPetId` | `Map<Long, BookingPet>` — ใช้ผูกบริการกับสัตว์ในการจอง | `BookingServiceImpl` |
| `roomAmount`, `serviceAmount`, `surchargeAmount`, `discountAmount`, `totalPrice` | `BigDecimal` — ราคาแต่ละส่วนและยอดรวม ณ เวลาจอง | `Booking` |
| `promotionName` | `String` — ชื่อโปรโมชัน ณ เวลาจอง | `Booking` |
| `status`, `paymentStatus` | `BookingStatus`, `PaymentStatus` — สถานะการจองและการชำระเงิน | `Booking` |
| `PET_RESERVING_STATUSES`, `ACTIVE_STATUSES` | กลุ่มสถานะ `PENDING`, `CONFIRMED`, `CHECKED_IN` ที่นับว่าครองห้องหรือสัตว์ | `BookingServiceImpl`, `AvailabilityServiceImpl` |

## โค้ดที่เกี่ยวข้อง

- [Booking.java](../code/src/main/java/com/example/petshotel/domain/entity/Booking.java)
- [CreateBookingRequest.java](../code/src/main/java/com/example/petshotel/dto/request/CreateBookingRequest.java)
- [BookingController.java](../code/src/main/java/com/example/petshotel/controller/web/BookingController.java)
- [BookingServiceImpl.java](../code/src/main/java/com/example/petshotel/service/impl/BookingServiceImpl.java)
- [AvailabilityServiceImpl.java](../code/src/main/java/com/example/petshotel/service/impl/AvailabilityServiceImpl.java)
- [State implementations](../code/src/main/java/com/example/petshotel/state)