package com.vary.gong.email;

import com.thoughtworks.go.plugin.api.logging.Logger;

import java.util.List;

public class DebugEmailSender implements EmailSender {

	private static Logger LOGGER = Logger.getLoggerFor(DebugEmailSender.class);

	@Override
	public void sendMail(String sender, List<String> recipients, String subject, String body) throws Exception {
		LOGGER.info("===============================================");
		LOGGER.info("From: " + sender);
		LOGGER.info("To: " + String.join(",", recipients));
		LOGGER.info("Subject: " + subject);
		LOGGER.info("Body: " + body);
		LOGGER.info("===============================================");
	}
}
