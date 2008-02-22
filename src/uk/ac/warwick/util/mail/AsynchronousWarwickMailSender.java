package uk.ac.warwick.util.mail;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.log4j.Logger;
import org.springframework.mail.MailException;
import org.springframework.mail.MailParseException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import uk.ac.warwick.util.concurrency.TaskExecutionService;
import uk.ac.warwick.util.core.StringUtils;

/**
 * A service to send mail asynchronously.
 * 
 * @author Mat Mannion
 */
public final class AsynchronousWarwickMailSender implements WarwickMailSender {
    
    private static final Logger LOGGER = Logger.getLogger(AsynchronousWarwickMailSender.class);

    private final TaskExecutionService executionService;

    private final JavaMailSender mailSender;

    public AsynchronousWarwickMailSender(TaskExecutionService service, JavaMailSender sender) {
        this.executionService = service;
        this.mailSender = sender;
    }

    public MimeMessage createMimeMessage() {
        return mailSender.createMimeMessage();
    }
    
    public void send(MimeMessage message) throws MailException {
        // if we wanted to block and wait, we could do a .get() on the future
        sendAndReturnFuture(message);
    }

    public void send(SimpleMailMessage message) throws MailException {
        // if we wanted to block and wait, we could do a .get() on the future
        sendAndReturnFuture(message);
    }

    /**
     * Send a MimeMessage to the thread execution pool and return a Future
     * boolean result. Calling .get() will block until the mail has been sent
     * and return a boolean true or false for reported success (success is where
     * the mail sender does not throw an exception when asked to send the mail)
     */
    private Future<Boolean> sendAndReturnFuture(MimeMessage message) throws MailException {
        // we need to throw a MailException if the address is invalid in any way
        try {
            validateRecipients(message.getAllRecipients());
            validateRecipients(message.getFrom());
        } catch (MessagingException e) {
            throw new MailParseException(e);
        }

        return executionService.submit(new MimeMailSenderTask(mailSender, message));
    }

    /**
     * Send a SimpleMailMessage to the thread execution pool and return a Future
     * boolean result. Calling .get() will block until the mail has been sent
     * and return a boolean true or false for reported success (success is where
     * the mail sender does not throw an exception when asked to send the mail)
     */
    private Future<Boolean> sendAndReturnFuture(SimpleMailMessage message) throws MailException {
        // we need to throw a MailException if the address is invalid in any way
        try {
            validateAddress(message.getFrom());
            validateAddresses(message.getTo());
            validateAddresses(message.getCc());
            validateAddresses(message.getBcc());
        } catch (MessagingException e) {
            throw new MailParseException(e);
        }

        return executionService.submit(new SimpleMailSenderTask(mailSender, message));
    }

    private void validateRecipients(Address[] recipients) throws AddressException {
        if (recipients != null) {
            for (Address address: recipients) {
                validateAddress(address.toString());
            }
        }
    }

    private void validateAddress(String address) throws AddressException {
        if (StringUtils.hasText(address)) {
            InternetAddress ia = new InternetAddress(address);
            ia.validate();
        }
    }

    private void validateAddresses(String[] addresses) throws AddressException {
        if (addresses != null) {
            for (String address: addresses) {
                validateAddress(address);
            }
        }
    }

    private class MimeMailSenderTask implements Callable<Boolean> {

        private final JavaMailSender sender;

        private final MimeMessage message;

        public MimeMailSenderTask(JavaMailSender javaMailSender, MimeMessage theMessage) {
            this.sender = javaMailSender;
            this.message = theMessage;
        }

        public Boolean call() {
            LOGGER.info("Trying to send mail " + message);
            try {
                sender.send(message);
                LOGGER.info("Message sent successfully");
                return true;
            } catch (MailException e) {
                LOGGER.error("Error sending mail",e);
                return false;
            }
        }

    }

    private class SimpleMailSenderTask implements Callable<Boolean> {

        private final JavaMailSender sender;

        private final SimpleMailMessage message;

        public SimpleMailSenderTask(JavaMailSender javaMailSender, SimpleMailMessage theMessage) {
            this.sender = javaMailSender;
            this.message = theMessage;
        }

        public Boolean call() {
            LOGGER.info("Trying to send mail " + message);
            try {
                sender.send(message);
                LOGGER.info("Message sent successfully");
                return true;
            } catch (MailException e) {
                LOGGER.error("Error sending mail",e);
                return false;
            }
        }

    }

}
