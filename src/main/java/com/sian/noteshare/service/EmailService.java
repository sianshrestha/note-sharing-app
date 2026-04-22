package com.sian.noteshare.service;

import com.sian.noteshare.entity.Note;
import com.sian.noteshare.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Service class responsible for dispatching email notifications to users.
 * Uses JavaMailSender to handle SMTP communication.
 */
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    /**
     * Sends a welcome email to newly registered users.
     *
     * @param user The registered User entity containing the email address.
     */
    public void sendWelcomeEmail(User user) {
        String subject = "Welcome to ShareMyNotes!";
        String body = "Hi " + user.getUsername() + ",\n\nThank you for registering with ShareMyNotes.\n\nBest regards,\nThe ShareMyNotes Team";
        sendEmail(user.getEmail(), subject, body);
    }

    /**
     * Sends a confirmation email to a user after they successfully upload a note.
     *
     * @param user The User entity who uploaded the note.
     * @param note The uploaded Note entity.
     */
    public void sendUploadConfirmation(User user, Note note) {
        String subject = "Note Upload Confirmation";
        String body = "Hi " + user.getUsername() + ",\n\nYour note \"" + note.getTitle() + "\" was uploaded successfully.\n\nBest regards,\nThe ShareMyNotes Team";
        sendEmail(user.getEmail(), subject, body);
    }

    /**
     * Helper method to construct and send a simple text email.
     * Errors during sending are caught and logged to prevent application crashes.
     *
     * @param email The recipient email address.
     * @param subject The subject line of the email.
     * @param body The text body of the email.
     */
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