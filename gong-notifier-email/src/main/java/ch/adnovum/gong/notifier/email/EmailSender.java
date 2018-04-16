package ch.adnovum.gong.notifier.email;

import java.util.Collection;

public interface EmailSender {

	void sendMail(String sender, Collection<String> recipients, String subject, String body) throws Exception;
}
