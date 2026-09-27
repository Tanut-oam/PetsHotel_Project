package com.example.petshotel.notification;

import java.time.format.DateTimeFormatter;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import com.example.petshotel.domain.entity.Booking;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailNotificationService {
    private final JavaMailSender mailSender;
    
    public void sendBookingConfirmedEmail(Booking booking) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            String toEmail = booking.getUser().getEmail();
            String customerName = booking.getUser().getFirstName();
            String roomName = booking.getRoom().getName();
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            helper.setTo(toEmail);
            helper.setSubject("ยืนยันการจอง #" + booking.getId() + " - PetStay");
            helper.setText(buildEmailBody(booking, customerName, roomName, fmt), true);

            mailSender.send(message);
            log.info("Sent confirmation email to {} for Booking #{}", toEmail, booking.getId());

        } catch (MessagingException e) {
            // อย่าให้การส่งอีเมลล้มเหลวไปทำให้ transaction การจองพัง
            log.error("Failed to send email for Booking #{}: {}", booking.getId(), e.getMessage());
        }
    }

    private String buildEmailBody(Booking booking, String name, String room, DateTimeFormatter fmt) {
        return """
                <div style="font-family: sans-serif;">
                    <h2>สวัสดีคุณ %s 🐾</h2>
                    <p>การจองของคุณได้รับการยืนยันแล้ว</p>
                    <ul>
                        <li>เลขที่การจอง: #%d</li>
                        <li>ห้อง: %s</li>
                        <li>เช็คอิน: %s</li>
                        <li>เช็คเอาท์: %s</li>
                        <li>ยอดรวม: %s บาท</li>
                    </ul>
                    <p>ขอบคุณที่ใช้บริการ PetStay ครับ</p>
                </div>
                """.formatted(
                name,
                booking.getId(),
                room,
                booking.getCheckInDate().format(fmt),
                booking.getCheckOutDate().format(fmt),
                booking.getTotalPrice()
        );
    }
}
