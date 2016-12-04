package com.cheekymammoth.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.sun.mail.smtp.SMTPTransport;

import java.util.Date;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class CMEmail {

	private CMEmail() { }

    /**
     * Send email using cheekymammoth IMAP/SMTP server.
     *
     * @param username cheekymammoth username
     * @param password cheekymammoth password
     * @param recipientEmail TO recipient
     * @param ccEmail CC recipient. Can be empty if there is no CC recipient
     * @param title title of the message
     * @param message message to be sent
     * @throws AddressException if the email address parse failed
     * @throws MessagingException if the connection is dead or not in the connected state or if the message is not a MimeMessage
     */
    public static void Send(final String username, final String password, String recipientEmail,
    		String ccEmail, String title, String message, String attachment)
    				throws AddressException, MessagingException {
        //Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
        //final String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";

        // Get a Properties object
        Properties props = System.getProperties();
        props.setProperty("mail.imap.host", "mail.cheekymammoth.com");
        //props.setProperty("mail.smtp.socketFactory.class", SSL_FACTORY);
        //props.setProperty("mail.smtp.socketFactory.fallback", "false");
        props.setProperty("mail.imap.port", "143"); // 465
        //props.setProperty("mail.smtp.socketFactory.port", "143"); // 465
        props.setProperty("mail.imap.auth", "true");

        /*
        If set to false, the QUIT command is sent and the connection is immediately closed. If set 
        to true (the default), causes the transport to wait for the response to the QUIT command.

        ref :   http://java.sun.com/products/javamail/javadocs/com/sun/mail/smtp/package-summary.html
                http://forum.java.sun.com/thread.jspa?threadID=5205249
                smtpsend.java - demo program from javamail
        */
        //props.put("mail.smtp.quitwait", "false");

        Session session = Session.getInstance(props, null);

        // -- Create a new message --
        final MimeMessage msg = new MimeMessage(session);

        // -- Set the FROM and TO fields --
        msg.setFrom(new InternetAddress(username + "@cheekymammoth.com"));
        msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail, false));

        if (ccEmail != null && ccEmail.length() > 0) {
            msg.setRecipients(Message.RecipientType.CC, InternetAddress.parse(ccEmail, false));
        }

        msg.setSubject(title);
        msg.setSentDate(new Date());
        
        do {
	        if (attachment != null) {
	        	FileHandle file = Gdx.files.local(attachment);
				if (file != null && file.exists()) {
					BodyPart messageBodyPart = new MimeBodyPart();
					messageBodyPart.setText(message);
					
					Multipart multipart = new MimeMultipart();
					multipart.addBodyPart(messageBodyPart);
					
					messageBodyPart = new MimeBodyPart();
					DataSource source = new FileDataSource(attachment);
			        messageBodyPart.setDataHandler(new DataHandler(source));
			        messageBodyPart.setFileName("attachment.txt");
			        multipart.addBodyPart(messageBodyPart);
			        msg.setContent(multipart);
					break;
				}
	        }
	        
	        msg.setText(message, "utf-8");
        } while (false);

        SMTPTransport t = (SMTPTransport)session.getTransport("smtp");
        t.connect("mail.cheekymammoth.com", username, password);
        t.sendMessage(msg, msg.getAllRecipients());      
        t.close();
    }
}
