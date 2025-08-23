package com.sian.noteshare.service;

import com.sian.noteshare.entity.Note;
import com.sian.noteshare.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendWelcomeEmail(User user) {
        String subject = "Welcome to ShareMyNotes!";
        String body = "Hi " + user.getUsername() + ",\n\nThank you for registering with ShareMyNotes.\n\nBest regards,\nThe ShareMyNotes Team";
        sendEmail(user.getEmail(), subject, body);
    }

    public void sendUploadConfirmation(User user, Note note) {
        String subject = "Note Upload Confirmation";
        String body = "Hi " + user.getUsername() + ",\n\nYour note \"" + note.getTitle() + "\" was uploaded successfully.\n\nBest regards,\nThe ShareMyNotes Team";
        sendEmail(user.getEmail(), subject, body);
    }

    private void sendEmail(String email, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Failed to send email to " + email + ": " + e.getMessage());
        }
    }
}
