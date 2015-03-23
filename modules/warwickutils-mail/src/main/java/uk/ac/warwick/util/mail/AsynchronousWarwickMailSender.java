package uk.ac.warwick.util.mail;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.MailParseException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import uk.ac.warwick.util.concurrency.TaskExecutionService;
import uk.ac.warwick.util.core.StringUtils;
import static org.springframework.util.StringUtils.arrayToCommaDelimitedString;

/**
 * A service to send mail asynchronously.
 * 
 * @author Mat Mannion
 * @requires Spring
 */
public final class AsynchronousWarwickMailSender implements WarwickMailSender {
    
    private static final String DEFAULT_SENDER = "no-reply@warwick.ac.uk";
    
    private static final Logger LOGGER = LoggerFactory.getLogger(AsynchronousWarwickMailSender.class);

    private final TaskExecutionService executionService;

    private final JavaMailSender mailSender;
    
    private String sender = DEFAULT_SENDER;

    public AsynchronousWarwickMailSender(TaskExecutionService service, JavaMailSender sender) {
        this.executionService = service;
        this.mailSender = sender;
    }

    public MimeMessage createMimeMessage() {
        return mailSender.createMimeMessage();
    }
    
    public Future<Boolean> send(MimeMessage message) throws MailException {
        // if we wanted to block and wait, we could do a .get() on the future
        return sendAndReturnFuture(message);
    }

    public Future<Boolean> send(SimpleMailMessage message) throws MailException {
        // if we wanted to block and wait, we could do a .get() on the future
        return sendAndReturnFuture(message);
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
            
            if (StringUtils.hasText(sender)) {
                message.setSender(new InternetAddress(sender));
            }
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

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    private class MimeMailSenderTask implements Callable<Boolean> {

        private final JavaMailSender sender;

        private final MimeMessage message;

        public MimeMailSenderTask(JavaMailSender javaMailSender, MimeMessage theMessage) {
            this.sender = javaMailSender;
            this.message = theMessage;
        }

        public Boolean call() {
            try {
                LOGGER.info("Trying to send mail " + mimeMessageToString(message));
            } catch (Exception e) {
                LOGGER.warn("Exception toString() for message: " + message);
            }
            
            try {
                sender.send(message);
                LOGGER.info("Message sent successfully");
                return true;
            } catch (MailException e) {
                LOGGER.error("Error sending mail",e);
                return false;
            }
        }
        
        private String mimeMessageToString(MimeMessage message) throws MessagingException, IOException {
            StringBuilder sb = new StringBuilder("MimeMessage: ");
            sb.append("from=").append(arrayToCommaDelimitedString(message.getFrom())).append("; ");
            sb.append("replyTo=").append(arrayToCommaDelimitedString(message.getReplyTo())).append("; ");
            sb.append("to=").append(arrayToCommaDelimitedString(message.getRecipients(RecipientType.TO))).append("; ");
            sb.append("cc=").append(arrayToCommaDelimitedString(message.getRecipients(RecipientType.CC))).append("; ");
            sb.append("bcc=").append(arrayToCommaDelimitedString(message.getRecipients(RecipientType.BCC))).append("; ");
            sb.append("sentDate=").append(message.getSentDate()).append("; ");
            sb.append("subject=").append(message.getSubject()).append("; ");
            sb.append("text=").append(message.getContent());
            return sb.toString();
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
            LOGGER.info("Trying to send mail " + message); // SimpleMailMessage has a nice toString
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
