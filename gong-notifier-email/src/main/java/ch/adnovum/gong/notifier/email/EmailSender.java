package ch.adnovum.gong.notifier.email;

import java.util.Collection;

public interface EmailSender {

	void sendMail(String sender, Collection<String> recipients, String subject, String body) throws EmailSenderException;

	class EmailSenderException extends Exception {

		EmailSenderException(Throwable cause) {
			super(cause);
		}
	}
}
