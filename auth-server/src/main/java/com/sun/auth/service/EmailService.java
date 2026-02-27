package com.sun.auth.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * EmailService — sends transactional emails asynchronously.
 * Runs on the emailTaskExecutor thread pool so HTTP threads are never blocked.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.base-url}")
    private String baseUrl;

    @Value("${app.email.from}")
    private String fromAddress;

    // ── Verification Email ────────────────────────────────────────────────────

    @Async("emailTaskExecutor")
    public void sendVerificationEmail(String toEmail, String fullName, String token) {
        String link    = baseUrl + "/auth/verify-email?token=" + token;
        String subject = "Confirm Your Insurance Account";
        sendHtml(toEmail, subject, buildVerificationBody(fullName, link));
        log.info("Verification email sent to {}", toEmail);
    }

    // ── Welcome Email (after successful verification) ─────────────────────────

    @Async("emailTaskExecutor")
    public void sendWelcomeEmail(String toEmail, String fullName) {
        String subject = "Welcome to Insurance Portal!";
        sendHtml(toEmail, subject, buildWelcomeBody(fullName));
        log.info("Welcome email sent to {}", toEmail);
    }

    // ── Password Reset Email ──────────────────────────────────────────────────

    @Async("emailTaskExecutor")
    public void sendPasswordResetEmail(String toEmail, String fullName, String token) {
        String link    = baseUrl + "/auth/reset-password?token=" + token;
        String subject = "Reset Your Insurance Account Password";
        sendHtml(toEmail, subject, buildPasswordResetBody(fullName, link));
        log.info("Password reset email sent to {}", toEmail);
    }

    // ── Core Sender ───────────────────────────────────────────────────────────

    private void sendHtml(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            // Email failures are non-fatal — log and continue
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }

    // ── HTML Templates ────────────────────────────────────────────────────────

    private String buildVerificationBody(String name, String link) {
        return """
                <!DOCTYPE html><html><body style="font-family:Arial,sans-serif;background:#f4f4f4;padding:20px;">
                  <div style="max-width:600px;margin:0 auto;background:white;padding:30px;border-radius:8px;">
                    <h2 style="color:#2c3e50;">Verify Your Email Address</h2>
                    <p>Hello <strong>%s</strong>,</p>
                    <p>Thank you for registering. Please verify your email by clicking the button below.</p>
                    <a href="%s" style="display:inline-block;background:#3498db;color:white;
                       padding:12px 24px;border-radius:4px;text-decoration:none;margin:16px 0;">
                      Verify Email
                    </a>
                    <p style="color:#888;font-size:13px;">This link expires in 24 hours.</p>
                  </div>
                </body></html>
                """.formatted(name, link);
    }

    private String buildWelcomeBody(String name) {
        return """
                <!DOCTYPE html><html><body style="font-family:Arial,sans-serif;background:#f4f4f4;padding:20px;">
                  <div style="max-width:600px;margin:0 auto;background:white;padding:30px;border-radius:8px;">
                    <h2 style="color:#27ae60;">Welcome to Insurance Portal!</h2>
                    <p>Hello <strong>%s</strong>,</p>
                    <p>Your account is now active. You can log in and explore our insurance offerings.</p>
                    <p style="color:#888;font-size:13px;">— Insurance Portal Team</p>
                  </div>
                </body></html>
                """.formatted(name);
    }

    private String buildPasswordResetBody(String name, String link) {
        return """
                <!DOCTYPE html><html><body style="font-family:Arial,sans-serif;background:#f4f4f4;padding:20px;">
                  <div style="max-width:600px;margin:0 auto;background:white;padding:30px;border-radius:8px;">
                    <h2 style="color:#e74c3c;">Password Reset Request</h2>
                    <p>Hello <strong>%s</strong>,</p>
                    <p>Click the button below to reset your password. This link expires in 1 hour.</p>
                    <a href="%s" style="display:inline-block;background:#e74c3c;color:white;
                       padding:12px 24px;border-radius:4px;text-decoration:none;margin:16px 0;">
                      Reset Password
                    </a>
                    <p style="color:#888;font-size:13px;">If you didn't request this, please ignore this email.</p>
                  </div>
                </body></html>
                """.formatted(name, link);
    }
}
