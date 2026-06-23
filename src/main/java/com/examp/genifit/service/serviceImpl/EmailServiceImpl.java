package com.examp.genifit.service.serviceImpl;


import com.examp.genifit.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

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
}