package ch.adnovum.gong.notifier.email;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestEmailSender implements EmailSender {

	private List<SentMail> sentMails = new LinkedList<>();

	@Override
	public void sendMail(String sender, List<String> recipients, String subject, String body) throws Exception {
		sentMails.add(new SentMail(sender, new HashSet<>(recipients), subject, body));
	}

	public void assertMail(List<String> recipients, String subjectContains) {
		Set<String> expected = new HashSet<>(recipients);
		Optional<SentMail> mail = sentMails.stream()
				.filter(m -> m.recipients.equals(expected) && m.subject.contains(subjectContains))
				.findFirst();
		assertTrue("No mail for " + String.join(",", recipients) +
				" with subject containing '" + subjectContains + "'", mail.isPresent());
	}

	public void assertCount(int expected) {
		assertEquals("Number of mails", expected, sentMails.size());
	}

	private class SentMail {
		public String sender;
		public Set<String> recipients;
		public String subject;
		public String body;

		public SentMail(String sender, Set<String> recipients, String subject, String body) {
			this.sender = sender;
			this.recipients = recipients;
			this.subject = subject;
			this.body = body;
		}
	}
}
