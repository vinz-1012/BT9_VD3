package vn.iotstar.service.impl;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import vn.iotstar.service.EmailService;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:no-reply@shop.vn}")
    private String fromEmail;

    @Override
    public void sendOtpEmail(String toEmail, String otpCode, String purpose) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);

            String subject = "[Shop Web] Mã xác thực OTP cho " + purpose;
            helper.setSubject(subject);

            String content = """
                <div style="font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; max-width: 600px; margin: 0 auto; padding: 25px; border: 1px solid #e2e8f0; border-radius: 12px; background-color: #ffffff;">
                    <div style="text-align: center; margin-bottom: 20px;">
                        <h2 style="color: #4f46e5; margin: 0; font-size: 24px;">Hệ Thống Shop Web</h2>
                        <p style="color: #64748b; font-size: 14px; margin-top: 5px;">Xác thực tài khoản của bạn</p>
                    </div>
                    <div style="padding: 20px; background-color: #f8fafc; border-radius: 8px; text-align: center; margin-bottom: 20px;">
                        <p style="font-size: 16px; color: #334155; margin-bottom: 15px;">Mã OTP của bạn cho yêu cầu <strong>%s</strong> là:</p>
                        <div style="font-size: 36px; font-weight: 700; letter-spacing: 8px; color: #4f46e5; background: #e0e7ff; display: inline-block; padding: 10px 24px; border-radius: 8px;">
                            %s
                        </div>
                        <p style="font-size: 13px; color: #ef4444; margin-top: 15px; font-weight: 500;">
                            Mã này có hiệu lực trong vòng <strong>5 phút</strong> và cho phép nhập sai tối đa 5 lần.
                        </p>
                    </div>
                    <p style="font-size: 13px; color: #94a3b8; text-align: center; margin: 0;">
                        Nếu bạn không yêu cầu mã này, vui lòng bỏ qua email này hoặc liên hệ bộ phận hỗ trợ.
                    </p>
                </div>
                """.formatted(purpose, otpCode);

            helper.setText(content, true);

            mailSender.send(message);
            log.info("Sent OTP email to: {} for purpose: {}", toEmail, purpose);
        } catch (MessagingException e) {
            log.error("Failed to send OTP email to {}: {}", toEmail, e.getMessage(), e);
            throw new RuntimeException("Không thể gửi email OTP, vui lòng thử lại sau.", e);
        }
    }
}
