package com.authsphere.authsphere_backend.identity.email;

import com.authsphere.authsphere_backend.identity.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String mailUsername;

    @Value("${server.port}")
    private String serverPort;

    @Override
    public void sendVerificationEmail(User user, String token) {

        String verificationLink =
                "http://localhost:" + serverPort +
                        "/api/auth/verify?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(mailUsername);
        message.setTo(user.getEmail());
        message.setSubject("Verify Your AuthSphere Account");

        message.setText(
                "Hello " + user.getFirstName() + ",\n\n" +

                        "Welcome to AuthSphere!\n\n" +

                        "Thank you for creating an account with us. " +
                        "Please verify your email address by clicking the link below:\n\n" +

                        verificationLink + "\n\n" +

                        "This verification link is valid for 1 hour.\n\n" +

                        "If you did not create an AuthSphere account, " +
                        "you can safely ignore this email.\n\n" +

                        "Regards,\n" +
                        "AuthSphere Team"
        );

        mailSender.send(message);

        System.out.println(
                "Verification email sent to: " + user.getEmail()
        );
    }
}