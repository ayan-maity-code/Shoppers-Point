package com.shopperspoint.email;

import com.shopperspoint.entity.Product;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class EmailService {
    @Autowired
    private final JavaMailSender mailSender;

    @Async
    public void sendActivationMail(String to, String token) {
        String activationLink = "http://localhost:8080/user/customer/activate?token=" + token;
        String subject = "Activate your account";

        String body = "<p>Click the link below to activate your account:</p>"
                + "<a href='" + activationLink + "'>Activate Now</a>";

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true);
            mailSender.send(message);


        } catch (MessagingException ex) {
            throw new RuntimeException("Failed to send activation email");
        }

    }


    @Async
    public void sendEmailToSeller(String email) {

        String subject = "Account created";
        String body = "<p>Account created successfully and waiting for approval</p>";

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper handler = new MimeMessageHelper(message, true);

            handler.setTo(email);
            handler.setSubject(subject);
            handler.setText(body, true);

            mailSender.send(message);

        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send success email");
        }

    }

    @Async
    public void sendEmailForAccountLocked(String email) {
        String subject = "Account Locked";
        String body = "<p>Account locked due to multiple login attempts." +
                "Please contact customer support to unlock your account.</p>";
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper handler = new MimeMessageHelper(message, true);

            handler.setTo(email);
            handler.setSubject(subject);
            handler.setText(body, true);

            mailSender.send(message);

        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send success email");
        }

    }

    @Async
    public void sendEmailForForgotPassword(String to, String token) {
        String resetLink = "http://localhost:8080/api/public/user/reset-password?token=" + token;
        String subject = "Reset your password";
        String body = "<p>Click the link below to reset your password:</p>"
                + "<a href='" + resetLink + "'>Reset Password Now</a>";

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true);
            mailSender.send(message);

        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email");
        }

    }

    @Async
    public void notifyAccountActivationSuccess(String to) {
        String subject = "Account activated";
        String body = "<p>Your account has been successfully activated</p>";

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true);
            mailSender.send(message);


        } catch (MessagingException ex) {
            throw new RuntimeException("Failed to send activation email");
        }

    }


    @Async
    public void notifyAccountDeActivation(String to) {
        String subject = "Account DeActivated";
        String body = "<p>Your account has been successfully deactivated.</p>";

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true);
            mailSender.send(message);


        } catch (MessagingException ex) {
            throw new RuntimeException("Failed to send activation email");
        }

    }

    @Async
    public void notifyPasswordChanged(String to) {
        String subject = "Password Changed";
        String body = "<p>Your password has been successfully changed.</p>";

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true);
            mailSender.send(message);


        } catch (MessagingException ex) {
            throw new RuntimeException("Failed to send activation email");
        }

    }

    @Async
    public void sendNewProductEmailToAdmin(String to, Product product) {

        String subject = "New Product Added – Approval Required";

        String body = """
                <html>
                        <body>
                            <p>A new product has been added and is waiting for your approval.</p>
                
                            <p><strong>Product Details:</strong></p>
                            <ul>
                                <li><strong>Name:</strong> %s</li>
                                <li><strong>Brand:</strong> %s</li>
                                <li><strong>Category:</strong> %s</li>
                                <li><strong>Seller:</strong> %s</li>
                                <li><strong>Company:</strong> %s</li>
                            </ul>
                
                            <p><strong>Action Required:</strong><br/>
                            This product is currently <strong>inactive</strong> and needs to be approved.</p>
                        </body>
                        </html>
                
                """.formatted(
                product.getName(),
                product.getBrand(),
                product.getCategory().getName(),
                product.getSeller().getEmail(),
                product.getSeller().getCompanyName()
        );


        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true);
            mailSender.send(message);


        } catch (MessagingException ex) {
            throw new RuntimeException("Failed to send activation email");
        }

    }

    @Async
    public void sendProductStatusUpdateToSeller(String to, Product product, boolean isActive) {

        String subject = isActive ? "Product Activated" : "Product Deactivated";

        String state = isActive ? "Activated" : "Deactivated";
        String body = """
                <p>Hello %s,</p>
                            <p>Your product has been <strong>%s</strong> by the admin.</p>
                
                            <p><strong>Product Details:</strong></p>
                            <ul>
                                <li><strong>Name:</strong> %s</li>
                                <li><strong>Brand:</strong> %s</li>
                                <li><strong>Category:</strong> %s</li>
                                <li><strong>Product ID:</strong> %d</li>
                            </ul>
                
                            <p>If you have any questions, please contact our support team.</p>
                            <p>Thank you,<br/>Admin Team</p>
                
                """.formatted(
                product.getSeller().getFirstName(),
                state,
                product.getName(),
                product.getBrand(),
                product.getCategory().getName(),
                product.getId()

        );

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true);
            mailSender.send(message);


        } catch (MessagingException ex) {
            throw new RuntimeException("Failed to send activation email");
        }


    }


}
