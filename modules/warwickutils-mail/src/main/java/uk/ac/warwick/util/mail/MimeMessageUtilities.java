package uk.ac.warwick.util.mail;

import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.IOException;
import java.util.Stack;

import static org.springframework.util.StringUtils.arrayToCommaDelimitedString;

public class MimeMessageUtilities {
    public static String mimeMessageToString(MimeMessage message) throws MessagingException, IOException {
        StringBuilder sb = new StringBuilder("MimeMessage: ");
        sb.append("from=").append(arrayToCommaDelimitedString(message.getFrom())).append("; ");
        sb.append("replyTo=").append(arrayToCommaDelimitedString(message.getReplyTo())).append("; ");
        sb.append("to=").append(arrayToCommaDelimitedString(message.getRecipients(MimeMessage.RecipientType.TO))).append("; ");
        sb.append("cc=").append(arrayToCommaDelimitedString(message.getRecipients(MimeMessage.RecipientType.CC))).append("; ");
        sb.append("bcc=").append(arrayToCommaDelimitedString(message.getRecipients(MimeMessage.RecipientType.BCC))).append("; ");
        sb.append("sentDate=").append(message.getSentDate()).append("; ");
        sb.append("subject=").append(message.getSubject()).append("; ");
        sb.append("text=");
        if (message.getContent() instanceof MimeMultipart) {
            MimeMultipart multipartMessage = (MimeMultipart) message.getContent();
            processMultipartMessage(sb, multipartMessage);

        } else {
            sb.append(message.getContent());
        }
        return sb.toString();
    }

    private static void processMultipartMessage(StringBuilder sb, MimeMultipart multipartMessage) throws MessagingException, IOException {
        Stack<BodyPart> bodies = new Stack<>();
        for (int i = 0; i < multipartMessage.getCount(); i++) {
            bodies.push(multipartMessage.getBodyPart(i));
        }
        while (!bodies.empty()) {
            BodyPart current = bodies.pop();
            if (current.isMimeType("text/plain") || current.isMimeType("text/html")) {
                sb.append("[").append(current.getContentType()).append("] ").append(current.getContent());
            } else if (current.getContent() instanceof BodyPart) {
                bodies.push((BodyPart) current.getContent());
                sb.append("\n----------\n");
            }
        }
    }
}
