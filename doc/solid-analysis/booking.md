# SOLID Analysis

## กิติญาดา: Booking และ State Pattern

เลขบรรทัดอ้างอิงจากโค้ด commit `61e633f` โดยพาธของโค้ดหลักในตารางเริ่มจาก `code/src/main/java/com/example/petshotel/`

| หลักการ | ไฟล์ : บรรทัด | สิ่งที่เห็นในโค้ด | เหตุผล |
|---|---|---|---|
| **S** Single Responsibility | `controller/web/BookingController.java:99-130`, `service/impl/BookingServiceImpl.java:135-138`, `mapper/BookingMapper.java:15-26` | Controller รับฟอร์ม ตรวจบัญชีผู้ใช้ และเลือกหน้าหรือ redirect ส่วน Service ประสานกฎธุรกิจและการบันทึก ขณะที่ Mapper แปลง Booking เป็น BookingResponse | แยกเหตุผลในการแก้ไขแต่ละส่วน เช่น หากเปลี่ยนข้อมูลใน Response จุดหลักที่ต้องแก้คือ Mapper หากเปลี่ยนกฎการจอง จุดหลักอยู่ที่ Service |
| **O** Open/Closed | `controller/web/BookingController.java:45`, `controller/web/BookingController.java:113-114`, `service/BookingService.java:11-35`, `service/impl/BookingServiceImpl.java:561-579` | Controller เรียกการจองผ่าน interface `BookingService` โดยไม่ตรวจชนิด implementation ส่วน Service เลือก Concrete State ผ่าน `getState()` ซึ่งยังใช้ switch และ new | สามารถเพิ่ม implementation ของ BookingService ที่รักษาสัญญาเดิมและกำหนดให้ Spring ใช้งาน โดยไม่เปลี่ยนจุดเรียกใน Controller แต่การเพิ่มสถานะใหม่ยังต้องปรับ BookingStatus และ getState() จึงยังมีข้อจำกัดด้าน OCP ในการสร้าง State |
| **L** Liskov Substitution | `state/BookingState.java:5-13`, `state/PendingState.java:28-29`, `state/ConfirmedState.java:28-29`, `service/impl/BookingServiceImpl.java:449-454` | PendingState และ ConfirmedState ใช้สัญญา `cancel(Booking)` เดียวกัน และเปลี่ยนเป็น CANCELLED เมื่ออนุญาตให้ยกเลิก ผู้เรียกใช้ตัวแปรชนิด BookingState | ในเส้นทางยกเลิกที่อนุญาต ทั้งสอง implementation ให้ผลตามความหมายของคำสั่งเดียวกัน โดยผู้เรียกไม่ต้องตรวจชนิดคลาส อย่างไรก็ตาม การ implements interface เพียงอย่างเดียวไม่ได้พิสูจน์ LSP ทุกกรณี ต้องพิจารณาเงื่อนไขและข้อผิดพลาดตามสถานะด้วย |
| **I** Interface Segregation | `state/BookingState.java:5-13`, `service/BookingService.java:19-35` | BookingState มีเฉพาะ confirm, checkIn, checkOut และ cancel ส่วนการสร้างการจอง ค้นข้อมูล และ preview ราคาอยู่ใน BookingService | แยกสัญญาของพฤติกรรมตามสถานะออกจากบริการการจอง Concrete State จึงไม่ต้อง implements การคำนวณราคา การค้นฐานข้อมูล หรือการออกใบเสร็จ |
| **D** Dependency Inversion | `controller/web/BookingController.java:42-50`, `service/impl/BookingServiceImpl.java:66-86` | Controller รับ BookingService ผ่าน constructor injection ส่วน BookingServiceImpl รับ PricingService, AvailabilityService และ Repository เป็น dependency โดยใช้ private final และ RequiredArgsConstructor | งานระดับบนพึ่ง interface ของบริการที่ใช้ และให้ Spring ส่ง implementation เข้ามา จึงเปลี่ยน dependency หรือใช้ mock ในการทดสอบได้ ทั้งนี้การสร้าง Concrete State ใน getState() ยังผูกกับคลาสจริง |

## ตัวอย่างการประยุกต์

### S — แยกการแปลงข้อมูลออกจากกฎธุรกิจ

`BookingServiceImpl.createBooking()` เตรียมการจองและบันทึกผ่าน Repository แล้วเรียก `BookingMapper.toResponse()` เพื่อแปลงผลลัพธ์

ตัวอย่างเช่น หากต้องเพิ่มข้อมูลที่แสดงใน BookingResponse สามารถปรับ DTO และ Mapper โดยไม่ต้องนำ logic แปลงข้อมูลไปใส่ใน PendingState หรือ ConfirmedState

Concrete State รับผิดชอบการเปลี่ยนสถานะ ส่วนการโหลดข้อมูล การบันทึก และการประกาศเหตุการณ์อยู่ใน Service

### O — เรียกบริการผ่าน interface และระบุข้อจำกัดของ State

`BookingController` เรียก `bookingService.createBooking(form)` ผ่าน BookingService จึงไม่จำเป็นต้องรู้ว่าคลาสใดเป็น implementation

หากเพิ่ม implementation ใหม่ ต้องรักษาความหมายของเมธอดและกำหนดให้ Spring เลือก bean ที่ต้องการ จุดเรียกใน Controller จึงใช้ต่อได้

สำหรับ State Pattern ปัจจุบัน getState() ยังสร้าง PendingState, ConfirmedState, CheckedInState, CheckedOutState และ CancelledState โดยตรง การเพิ่มสถานะใหม่จึงต้องแก้จุดเลือก State ด้วย ไม่ควรอ้างว่าส่วนนี้รองรับการเพิ่มสถานะโดยไม่แก้โค้ดเดิมเลย

### L — เรียกพฤติกรรมผ่าน BookingState

ตัวอย่างจากเส้นทางยกเลิกการจอง:

```java
BookingState state = getState(booking.getStatus());
state.cancel(booking);
```

เมื่อเป็น PENDING หรือ CONFIRMED และผ่านเงื่อนไขการชำระเงินแล้ว State ที่เลือกจะเปลี่ยนสถานะเป็น CANCELLED

ส่วน State ที่ไม่อนุญาต เช่น CheckedInState จะโยน IllegalStateException และไม่เปลี่ยนสถานะ การวิเคราะห์ LSP จึงต้องพิจารณาสัญญาว่าคำสั่งอาจถูกปฏิเสธตามสถานะ ไม่ใช่คาดหวังว่าทุก State ต้องทำทุกคำสั่งสำเร็จ

ปัจจุบัน interface ไม่ได้เขียนเงื่อนไขของแต่ละคำสั่งเป็น JavaDoc อย่างชัดเจน จึงควรใช้ตารางสถานะและชุดทดสอบประกอบการอธิบายพฤติกรรม

### I — จำกัดหน้าที่ของ interface State

BookingState กำหนดเฉพาะคำสั่งในวงจรสถานะการจอง ส่วนงานอื่นใช้สัญญาหรือคลาสที่รับผิดชอบแยกกัน เช่น:

- BookingService: สร้าง ค้นหา และประสานการทำรายการจอง
- PricingService: คำนวณราคา
- BookingRepository: อ่านและบันทึกข้อมูล
- BookingMapper: แปลงผลลัพธ์เป็น DTO

การที่บาง State ปฏิเสธคำสั่งด้วย IllegalStateException เป็นกฎของสถานะนั้น ไม่ใช่หลักฐานโดยลำพังว่า interface ละเมิดหรือผ่าน ISP

### D — ส่ง dependency ผ่าน constructor

BookingServiceImpl รับบริการและ Repository ผ่าน constructor ที่ Lombok สร้างจาก private final fields โดย Spring เป็นผู้ส่ง bean เข้ามา

ตัวอย่างใน BookingServiceTest.setUp() สร้าง mock ของ Repository, PricingService, AvailabilityService และ ApplicationEventPublisher แล้วส่งเข้า constructor ของ BookingServiceImpl จึงตรวจพฤติกรรม Service ได้โดยไม่ต้องให้ dependency เหล่านั้นทำงานจริง

อย่างไรก็ตาม BookingServiceImpl ยังพึ่ง BookingMapper ซึ่งเป็นคลาสจริง และสร้าง Concrete State ด้วย new จึงควรอธิบาย DIP เฉพาะส่วนที่พึ่ง abstraction ไม่อ้างว่าทุก dependency ในคลาสเป็น interface

## หลักฐานเพิ่มเติมจากไฟล์ทดสอบ

ไฟล์ `code/src/test/java/com/example/petshotel/service/BookingServiceTest.java` มีตัวอย่างที่เกี่ยวข้อง:

| บรรทัดเริ่มต้น | เมธอดทดสอบ | พฤติกรรมที่ตรวจ |
|---|---|---|
| 618 | `confirmPendingBookingShouldPublishEvent()` | ยืนยัน PENDING เป็น CONFIRMED บันทึก และประกาศ BookingConfirmedEvent |
| 635 | `confirmCancelledBookingShouldRejectWithoutEvent()` | ปฏิเสธการยืนยันรายการที่ยกเลิกแล้ว โดยไม่ประกาศเหตุการณ์ |
| 649 | `cancelUnpaidConfirmedBookingShouldSucceed()` | ยกเลิก CONFIRMED ที่ยังไม่ชำระเงินเป็น CANCELLED |
| 662 | `cancelPaidBookingShouldKeepOriginalData()` | ปฏิเสธการยกเลิกหลังชำระเงิน และรักษาข้อมูลเดิม |
| 772 | `pendingBookingCannotCheckIn()` | ปฏิเสธการเช็กอินขณะยังเป็น PENDING |

รายการนี้อ้างอิงกรณีทดสอบที่มีอยู่ในโค้ด ไม่ใช่รายงานผลการรันทดสอบครั้งใหม่ และไม่ได้พิสูจน์ SOLID ครบทุกสถานการณ์

## ขอบเขตของการวิเคราะห์

- การแบ่ง Controller, Service, State และ Mapper ช่วยแยกหน้าที่ แต่ BookingServiceImpl ยังประสานงานหลายขั้นตอน จึงต้องรักษาขอบเขตของแต่ละส่วนเมื่อเพิ่มความสามารถ
- การเพิ่มสถานะใหม่ต้องตรวจ getState(), BookingStatus, กฎวันที่ การตรวจห้อง/สัตว์ซ้อน และส่วนแสดงผลที่ใช้สถานะ
- BookingService รวมทั้งงานอ่าน สร้าง และเปลี่ยนสถานะ หากขอบเขตระบบขยายมากขึ้น อาจพิจารณาแยก interface ตามผู้เรียกและหน้าที่
- ข้อสังเกตเหล่านี้เป็นการวิเคราะห์โค้ดปัจจุบัน ไม่ได้หมายความว่ามีการแก้โค้ดตามข้อเสนอแล้ว