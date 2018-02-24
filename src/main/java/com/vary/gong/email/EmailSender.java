package com.vary.gong.email;

import java.util.List;

public interface EmailSender {

	void sendMail(String sender, List<String> recipients, String subject, String body) throws Exception;
}
