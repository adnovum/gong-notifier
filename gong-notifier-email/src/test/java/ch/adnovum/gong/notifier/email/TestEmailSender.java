package ch.adnovum.gong.notifier.email;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class TestEmailSender implements EmailSender {

	private List<SentMail> sentMails = new LinkedList<>();

	@Override
	public void sendMail(String sender, Collection<String> recipients, String subject, String body) throws Exception {
		sentMails.add(new SentMail(sender, new HashSet<>(recipients), subject, body));
	}

	public void assertMail(Collection<String> recipients, String subjectContains) {
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
