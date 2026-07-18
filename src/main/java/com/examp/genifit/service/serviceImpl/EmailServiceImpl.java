package com.examp.genifit.service.serviceImpl;


import com.examp.genifit.entity.PaymentTransaction;
import com.examp.genifit.entity.User;
import com.examp.genifit.entity.UserSubscription;
import com.examp.genifit.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {
    private final JavaMailSender mailSender;

    @Override
    public void sendRegistrationOtpEmail(String toEmail, String otpCode) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Mã xác thực đăng ký tài khoản Genifit");
        message.setText("Chào bạn,\n\n"
                + "Bạn đang thực hiện đăng ký tài khoản tại Genifit. Mã xác thực (OTP) của bạn là: " + otpCode + "\n\n"
                + "Mã này có hiệu lực trong vòng 5 phút. Vui lòng không chia sẻ mã này cho bất kỳ ai.\n\n"
                + "Trân trọng!\nĐội ngũ Genifit");

        mailSender.send(message);
    }

    @Override
    public void sendForgotPasswordOtpEmail(String toEmail, String otpCode) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Mã khôi phục mật khẩu tài khoản Genifit");
        message.setText("Chào bạn,\n\n"
                + "Chúng tôi nhận được yêu cầu khôi phục mật khẩu cho tài khoản của bạn. Mã xác thực (OTP) của bạn là: " + otpCode + "\n\n"
                + "Mã này có hiệu lực trong vòng 5 phút. Nếu bạn không yêu cầu thay đổi mật khẩu, vui lòng bỏ qua email này để đảm bảo an toàn.\n\n"
                + "Trân trọng!\nĐội ngũ Genifit");

        mailSender.send(message);
    }

    @Override
    public void sendInvoice(User user, PaymentTransaction transaction, UserSubscription subscription) {
        MimeMessage message = mailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(user.getEmail());
            helper.setSubject("Hóa đơn thanh toán GENEFIT - " + transaction.getOrderCode());
            helper.setText(buildEmailHtml(user, transaction, subscription), true); // true = HTML
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Gửi email thất bại: " + e.getMessage());
        }
    }

    private String buildEmailHtml(User user, PaymentTransaction transaction, UserSubscription subscription) {
        String method = transaction.getPaymentMethod().name();
        String amount = String.format("%,.0f VND", transaction.getAmount());
        String date = transaction.getUpdatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        String expiry = subscription.getEndDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        return "<div style='font-family:Arial,sans-serif;max-width:600px;margin:auto;padding:24px;border:1px solid #e0e0e0;border-radius:8px'>"
                + "<h2 style='color:#1D9E75;margin-bottom:4px'>Thanh toán thành công</h2>"
                + "<p style='color:#666;margin-bottom:24px'>Cảm ơn bạn đã sử dụng GENEFIT Premium</p>"
                + "<table style='width:100%;border-collapse:collapse'>"
                + "<tr style='background:#f9f9f9'>"
                + "<td style='padding:10px 14px;color:#888;width:45%'>Mã đơn hàng</td>"
                + "<td style='padding:10px 14px;font-weight:bold'>" + transaction.getOrderCode() + "</td>"
                + "</tr>"
                + "<tr>"
                + "<td style='padding:10px 14px;color:#888'>Gói đã mua</td>"
                + "<td style='padding:10px 14px;font-weight:bold'>" + transaction.getPlan().getPlanName() + "</td>"
                + "</tr>"
                + "<tr style='background:#f9f9f9'>"
                + "<td style='padding:10px 14px;color:#888'>Số tiền</td>"
                + "<td style='padding:10px 14px;font-weight:bold;color:#1D9E75'>" + amount + "</td>"
                + "</tr>"
                + "<tr>"
                + "<td style='padding:10px 14px;color:#888'>Phương thức</td>"
                + "<td style='padding:10px 14px'>" + method + "</td>"
                + "</tr>"
                + "<tr style='background:#f9f9f9'>"
                + "<td style='padding:10px 14px;color:#888'>Ngày thanh toán</td>"
                + "<td style='padding:10px 14px'>" + date + "</td>"
                + "</tr>"
                + "<tr>"
                + "<td style='padding:10px 14px;color:#888'>Hiệu lực đến</td>"
                + "<td style='padding:10px 14px;font-weight:bold'>" + expiry + "</td>"
                + "</tr>"
                + "</table>"
                + "<p style='margin-top:24px;color:#888;font-size:13px'>Nếu bạn có thắc mắc, vui lòng liên hệ hỗ trợ qua email này.</p>"
                + "<p style='color:#1D9E75;font-weight:bold'>— Đội ngũ GENEFIT</p>"
                + "</div>";
    }
}