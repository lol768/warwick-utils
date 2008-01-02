package uk.ac.warwick.util.mail;

import javax.mail.internet.MimeMessage;

import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;

/**
 * A service to send email.
 * 
 * @author Mat Mannion
 */
public interface WarwickMailSender {
    MimeMessage createMimeMessage();
    void send(MimeMessage message) throws MailException;
    void send(SimpleMailMessage message) throws MailException;
}
