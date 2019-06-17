package fi.lauber.posttracking.util;

import java.util.Properties;

import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.URLName;
import javax.mail.Message.RecipientType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.log4j.Logger;

import com.sun.mail.smtp.SMTPSSLTransport;

/**
 * TODO: This class is just copy'n'pasted from googleorder - refactor!
 */
public class EmailUtils {

	public static final Logger logger = Logger.getLogger(EmailUtils.class);
	
	private static String SENDER = "someemail@gmail.com";
	private static String PASSWORD = "somepassword";
	private static int OUTBOUND_PORT = 465;
	private static String OUTBOUND_PROTOCOL = "smtp";
	private static String OUTBOUND_HOST = "smtp.gmail.com";
	private static String SENDER_NAME = "Seuraapostia";
	
	private static Session getSession() {
		Properties props = new Properties();
		props.put("mail.transport.protocol",OUTBOUND_PROTOCOL);
		props.put("mail.smtp.host",OUTBOUND_HOST);
		props.put("mail.username",SENDER);
		props.put("mail.password",PASSWORD);
		props.put("mail.host",OUTBOUND_HOST);
		return Session.getInstance(props);
	}

	public static void sendEmail(String body, String emailAddress, String subject) {
		try {
			Session session = getSession();
			MimeMessage message = new MimeMessage(session);
			message.setText(body,"utf-8");
			message.setSubject(subject,"utf-8");
			MimeMessage newMessage = new MimeMessage((MimeMessage)message);
			
			/* Make sure that the email is _only_ sent to the specified receiver, even
			 * if a forwarded email contained several recipients. */
			newMessage.setRecipients(RecipientType.TO,new InternetAddress[0]);
			newMessage.setRecipients(RecipientType.CC,new InternetAddress[0]);
			newMessage.setRecipients(RecipientType.BCC,new InternetAddress[0]);
			if (subject != null) {
				newMessage.setSubject(subject,"utf-8");
			}
			String sender = SENDER;
			newMessage.setFrom(new InternetAddress(SENDER_NAME + " <" + sender + ">"));
			newMessage.setSender(new InternetAddress(sender));
			InternetAddress[] recipient = new InternetAddress[1];
			recipient[0] = new InternetAddress(emailAddress,true);
			newMessage.setHeader("To",emailAddress);
			//newMessage.setHeader("Content-Type","text/plain; charset=\"utf-8\"");
			newMessage.setHeader("Content-Transfer-Encoding", "quoted-printable");
	
			logger.debug("Sending email to " + emailAddress + " (subject = " + subject + "): " + body);
			
			Transport transport = null;
			try {
				URLName url = new URLName(OUTBOUND_PROTOCOL,OUTBOUND_HOST,OUTBOUND_PORT,"",sender,PASSWORD);
				transport = new SMTPSSLTransport(session,url);
				transport.connect();
				transport.sendMessage(newMessage,recipient);
			} finally {
				if (transport != null) {
					if (transport.isConnected()) {
						transport.close();
					}
				}
			}
		} catch (Exception e) {
			throw new RuntimeException("Couldn't send email: ", e);
		}
	}

}
