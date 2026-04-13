package edu.tcu.cs.projectpulse.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.base-url}")
    private String baseUrl;

    public void sendStudentInvitation(String email, String token, String sectionName, String customMessage) {
        String link = baseUrl + "/register/student?token=" + token;
        String body = buildInvitationBody("student", sectionName, link, customMessage);
        sendEmail(email, "Welcome to Project Pulse - Complete Your Registration", body);
    }

    public void sendInstructorInvitation(String email, String token, String sectionName, String customMessage) {
        String link = baseUrl + "/register/instructor?token=" + token;
        String body = buildInvitationBody("instructor", sectionName, link, customMessage);
        sendEmail(email, "Welcome to Project Pulse - Complete Your Registration", body);
    }

    public void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            message.setFrom("noreply@projectpulse.edu");
            mailSender.send(message);
            log.info("Email sent to {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }

    private String buildInvitationBody(String role, String sectionName, String link, String customMessage) {
        StringBuilder sb = new StringBuilder();
        sb.append("You have been invited to join Project Pulse as a ").append(role);
        if (sectionName != null) {
            sb.append(" for section ").append(sectionName);
        }
        sb.append(".\n\n");
        if (customMessage != null && !customMessage.isBlank()) {
            sb.append(customMessage).append("\n\n");
        }
        sb.append("Please complete your registration by clicking the link below:\n");
        sb.append(link).append("\n\n");
        sb.append("This link is for one-time use only.\n\n");
        sb.append("Best regards,\nProject Pulse Team");
        return sb.toString();
    }
}
