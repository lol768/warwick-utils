package uk.ac.warwick.util.mail;

import java.util.concurrent.Future;

import javax.mail.internet.MimeMessage;

import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.MimeMessagePreparator;

/**
 * A service to send email.
 * 
 * @author Mat Mannion
 * @requires Spring
 */
public interface WarwickMailSender {
    MimeMessage createMimeMessage();
    Future<Boolean> send(MimeMessage message, boolean logBody) throws MailException;

    default Future<Boolean> send(MimeMessage message) throws MailException {
        return send(message, false);
    }

    Future<Boolean> send(SimpleMailMessage message) throws MailException;
    Future<Boolean> send(MimeMessagePreparator preparator, boolean logBody) throws MailException;

    default Future<Boolean> send(MimeMessagePreparator preparator) throws MailException {
        return send(preparator, false);
    }
}
