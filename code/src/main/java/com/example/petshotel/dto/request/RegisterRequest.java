package com.example.petshotel.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "กรุณากรอกชื่อ")
    private String firstName;

    @NotBlank(message = "กรุณากรอกนามสกุล")
    private String lastName;

    @NotBlank(message = "กรุณากรอกอีเมล")
    @Email(message = "รูปแบบอีเมลไม่ถูกต้อง")
    private String email;

    @NotBlank(message = "กรุณากรอกเบอร์โทร")
    @Pattern(regexp = "^0\\d{8,9}$", message = "เบอร์โทรต้องขึ้นต้นด้วย 0 และมี 9–10 หลัก")
    private String phoneNumber;

    @NotBlank(message = "กรุณากรอกรหัสผ่าน")
    @Size(min = 8, message = "รหัสผ่านต้องมีอย่างน้อย 8 ตัวอักษร")
    private String password;

    @NotBlank(message = "กรุณายืนยันรหัสผ่าน")
    private String confirmPassword;
}