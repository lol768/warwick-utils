package uk.ac.warwick.util.mail;

import org.junit.Assert;
import org.junit.Test;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import java.io.IOException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MimeMessageUtilitiesTest {

    @Test
    public void itWorks() throws MessagingException, IOException {
        MimeMessage mail = getTestMessage();

        String s = MimeMessageUtilities.mimeMessageToString(mail, true);
        Assert.assertTrue("toString contains subject", s.contains("Geese in building"));
        Assert.assertTrue("toString contains plain text part", s.contains("raise P2 incident"));
        Assert.assertTrue("toString contains HTML part", s.contains("raise <strong>P2</strong> incident"));
        Assert.assertTrue("toString contains recipient", s.contains("helpdesk@warwick.ac.uk"));
    }

    @Test
    public void bodyCanBeSuppressedWithFlag() throws MessagingException, IOException {
        MimeMessage mail = getTestMessage();
        String s = MimeMessageUtilities.mimeMessageToString(mail, false);
        Assert.assertTrue("toString contains subject", s.contains("Geese in building"));
        Assert.assertFalse("toString contains plain text part", s.contains("raise P2 incident"));
        Assert.assertFalse("toString contains HTML part", s.contains("raise <strong>P2</strong> incident"));
        Assert.assertTrue("toString contains redaction message", s.contains("Redacted"));
        Assert.assertTrue("toString contains recipient", s.contains("helpdesk@warwick.ac.uk"));
    }

    private MimeMessage getTestMessage() throws MessagingException {
        MimeMessage mail = new MimeMessage((Session) null);
        mail.setRecipients(Message.RecipientType.TO, "helpdesk@warwick.ac.uk");
        mail.setSubject("Geese in building");
        MimeBodyPart textPart = new MimeBodyPart();
        textPart.setText("Please raise P2 incident for geese.", "utf-8");

        MimeBodyPart htmlPart = new MimeBodyPart();
        htmlPart.setContent("Please raise <strong>P2</strong> incident for geese.", "text/html; charset=utf-8");
        Multipart multipart = new MimeMultipart("alternative");
        multipart.addBodyPart(textPart); // <-- first
        multipart.addBodyPart(htmlPart); // <-- second
        mail.setContent(multipart);
        return mail;
    }

}
