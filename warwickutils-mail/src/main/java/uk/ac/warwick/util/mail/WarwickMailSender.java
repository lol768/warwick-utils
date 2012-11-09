package uk.ac.warwick.util.mail;

import java.util.concurrent.Future;

import javax.mail.internet.MimeMessage;

import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;

/**
 * A service to send email.
 * 
 * @author Mat Mannion
 * @requires Spring
 */
public interface WarwickMailSender {
    MimeMessage createMimeMessage();
    Future<Boolean> send(MimeMessage message) throws MailException;
    Future<Boolean> send(SimpleMailMessage message) throws MailException;
}
