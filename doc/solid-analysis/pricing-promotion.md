# SOLID Analysis

## นาเดีย: Pricing และ Promotion

เลขบรรทัดอ้างอิงจากโค้ด commit `9990a12` โดยพาธของโค้ดหลักในตารางเริ่มจาก `code/src/main/java/com/example/petshotel/`

| หลักการ | ไฟล์ : บรรทัด | สิ่งที่เห็นในโค้ด | เหตุผล |
|---|---|---|---|
| **S** Single Responsibility | `service/impl/PromotionServiceImpl.java:55-65`, `mapper/PromotionMapper.java:11-23` | `PromotionServiceImpl` ตรวจเงื่อนไขและบันทึกโปรโมชัน ส่วน `PromotionMapper` แปลง Entity เป็น `PromotionResponse` | แยกงานจัดการโปรโมชันออกจากงานแปลงข้อมูล หากรูปแบบ Response เปลี่ยน จุดหลักที่ต้องแก้คือ Mapper โดยไม่ต้องเปลี่ยนกฎตรวจส่วนลด |
| **O** Open/Closed | `pricing/PricingStrategy.java:7-9`, `service/impl/PricingServiceImpl.java:56-71`, `service/impl/PricingServiceImpl.java:89-93`, `pricing/LongStayDiscountStrategy.java:7-8` | Service วนเรียก Strategy ผ่าน interface และจัดผลตาม `category()` โดยไม่ตรวจชื่อ Concrete Strategy แต่ละคลาสลงทะเบียนเป็น Spring bean | สามารถเพิ่มสูตรในหมวดเดิม เช่นส่วนลดอีกแบบ โดยเพิ่มคลาสที่ implements `PricingStrategy` และลงทะเบียนเป็น bean โดยไม่ต้องเพิ่มเงื่อนไขชื่อคลาสใน Service ทั้งนี้การเพิ่มหมวดราคาใหม่อาจต้องปรับการรวมผล |
| **L** Liskov Substitution | `pricing/PricingStrategy.java:7-9`, `pricing/LongStayDiscountStrategy.java:15-28`, `pricing/PromotionDiscountStrategy.java:15-48`, `service/impl/PricingServiceImpl.java:89-91` | ส่วนลดพักระยะยาวและส่วนลดโปรโมชัน implements `PricingStrategy` เหมือนกัน รับ Context กับยอดก่อนหักส่วนลด และคืนจำนวนเงินส่วนลด โดย Service เรียกผ่าน interface เดียวกัน | สำหรับข้อมูลที่ถูกต้อง ทั้งสองคลาสทำหน้าที่ตามสัญญาการคำนวณส่วนลดได้โดยผู้เรียกไม่ต้องรู้ชนิดคลาส การเพิ่ม implementation ใหม่ต้องรักษาเงื่อนไขการรับข้อมูลและความหมายของผลลัพธ์ร่วมกัน การ implements interface เพียงอย่างเดียวไม่ได้พิสูจน์ LSP ครบทุกกรณี |
| **I** Interface Segregation | `pricing/PricingStrategy.java:7-9`, `pricing/LongStayDiscountStrategy.java:15`, `pricing/LongStayDiscountStrategy.java:27`, `service/impl/PricingServiceImpl.java:57-58` | `PricingStrategy` มีเพียง `calculate()` และ `category()` ซึ่งเกี่ยวข้องกับการคำนวณราคา และ Service ใช้งานทั้งสองเมธอด | Strategy แต่ละคลาสไม่ต้อง implements เมธอดจัดการโปรโมชัน บันทึกฐานข้อมูล หรือออกใบเสร็จที่ไม่เกี่ยวข้องกับหน้าที่ของตน |
| **D** Dependency Inversion | `service/impl/PricingServiceImpl.java:19-23`, `pricing/PricingStrategy.java:7-9`, `pricing/LongStayDiscountStrategy.java:7-8` | `PricingServiceImpl` รับ `List<PricingStrategy>` ผ่าน constructor โดยพึ่ง interface และไม่ได้สร้าง Concrete Strategy ด้วย `new` เอง ส่วนคลาส Strategy ลงทะเบียนด้วย `@Component` | งานประสานการคำนวณพึ่ง abstraction ของกฎราคา โดย Spring ส่ง implementation เข้ามา ทำให้เปลี่ยนหรือเพิ่ม Strategy และจัดชุด dependency สำหรับทดสอบได้ |

## ตัวอย่างการประยุกต์

- **SRP:** หากเพิ่มข้อมูลใน `PromotionResponse` ให้ปรับการแปลงข้อมูลใน `PromotionMapper` ส่วนกฎตรวจมูลค่าส่วนลดยังคงอยู่ใน `PromotionServiceImpl`
- **OCP:** หากต้องการเพิ่มส่วนลดสมาชิก สามารถสร้าง Strategy ใหม่ในหมวด `DISCOUNT` โดยระบบจะนำผลไปเปรียบเทียบกับส่วนลดเดิมตามกติกาเลือกค่าสูงสุด
- **LSP:** Strategy ส่วนลดใหม่ควรรับ Context และยอดก่อนหักส่วนลดที่ถูกต้อง คืนจำนวนเงินส่วนลดเป็น `BigDecimal` และคืน 0 เมื่อไม่เข้าเงื่อนไขของส่วนลดนั้น
- **ISP:** คลาสคำนวณส่วนลดรับผิดชอบเฉพาะ `calculate()` และ `category()` ไม่ต้องมีเมธอดสร้างหรือแก้ไขโปรโมชัน
- **DIP:** ในการทดสอบสามารถส่งรายการ Strategy เข้า constructor ของ `PricingServiceImpl` ได้โดยตรง โดยไม่ต้องให้ Service สร้างคลาสเหล่านั้นเอง

## หลักฐานเพิ่มเติมจากไฟล์ทดสอบ

ไฟล์ `code/src/test/java/com/example/petshotel/service/PricingServiceTest.java` มีตัวอย่างที่เกี่ยวข้อง:

- บรรทัด 31–37 จัดชุด Concrete Strategy ทั้งห้าและส่งเข้า constructor ของ `PricingServiceImpl`
- บรรทัด 94–116 ตรวจกรณีเลือกส่วนลดโปรโมชันที่สูงกว่าส่วนลดพักระยะยาว
- บรรทัด 149–165 ตรวจกรณีใช้ส่วนลดพักระยะยาวเมื่อไม่มีโปรโมชัน

ตัวอย่างเหล่านี้แสดงการใช้งาน Strategy ผ่านกลไกเดียวกันและระบุผลลัพธ์ที่คาดหวังสำหรับบางกรณี แต่ไม่ได้พิสูจน์พฤติกรรมของทุก implementation สำหรับข้อมูลทุกแบบ