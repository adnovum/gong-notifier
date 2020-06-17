package ch.adnovum.gong.notifier.email;

import java.util.Collection;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class JavaxEmailSender implements EmailSender {

	private String smtpHost;
	private int smtpPort;

	public JavaxEmailSender(String smtpHost, int smtpPort) {
		this.smtpHost = smtpHost;
		this.smtpPort = smtpPort;
	}

	@Override
	public void sendMail(String sender, Collection<String> recipients, String subject, String body) throws EmailSenderException {
		Properties properties = System.getProperties();
		properties.setProperty("mail.smtp.host", smtpHost);
		properties.put("mail.smtp.port", Integer.toString(smtpPort));
		Session session = Session.getDefaultInstance(properties);

		MimeMessage message = new MimeMessage(session);

		try {
			message.setFrom(new InternetAddress(sender));

			for (String recipient: recipients) {
				message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
			}

			message.setSubject(subject);
			message.setContent(body, "text/html; charset=utf-8");

			Transport.send(message);
		}
		catch (MessagingException e) {
			throw new EmailSenderException(e);
		}

	}
}
