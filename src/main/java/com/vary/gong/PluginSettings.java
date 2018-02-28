package com.vary.gong;

import java.util.HashMap;
import java.util.Map;

public class PluginSettings {
	
	private static final String DEFAULT_SMTP_HOST = "localhost";
	private static final String DEFAULT_SMTP_PORT = "25";
	private static final String DEFAULT_SENDER_EMAIL = "gocd@noreply.com";
	private static final String DEFAULT_REST_USER = null;
	private static final String DEFAULT_REST_PASSWORD = null;
	
	public static final Map<String, Field> FIELD_CONFIG = new HashMap<>();
	static {
		FIELD_CONFIG.put("smtpHost", new Field("Smtp Host", DEFAULT_SMTP_HOST, true, false, 0));
		FIELD_CONFIG.put("smtpPort", new Field("Smtp Port", DEFAULT_SMTP_PORT, true, false, 0));
		FIELD_CONFIG.put("senderEmail", new Field("Sender E-mail", DEFAULT_SENDER_EMAIL, true, false, 0));
		FIELD_CONFIG.put("restUser", new Field("Rest User", DEFAULT_REST_USER, true, false, 0));
		FIELD_CONFIG.put("restPassword", new Field("Rest Password", DEFAULT_REST_PASSWORD, true, true, 0));
	}

	private String smtpHost = DEFAULT_SMTP_HOST;
	private String smtpPort = DEFAULT_SMTP_PORT;
	private String senderEmail = DEFAULT_SENDER_EMAIL;
	private String restUser = DEFAULT_REST_USER;
	private String restPassword = DEFAULT_REST_PASSWORD;
	
	public String getSmtpHost() {
		return valueOrDefault(smtpHost, DEFAULT_SMTP_HOST);
	}
	
	public void setSmtpHost(String smtpHost) {
		this.smtpHost = smtpHost;
	}
	
	public Integer getSmtpPort() {
		return Integer.parseInt(valueOrDefault(smtpPort, DEFAULT_SMTP_PORT));
	}
	
	public void setSmtpPort(Integer smtpPort) {
		this.smtpPort = Integer.toString(smtpPort);
	}
	
	public String getSenderEmail() {
		return valueOrDefault(senderEmail, DEFAULT_SENDER_EMAIL);
	}

	public void setSenderEmail(String senderEmail) {
		this.senderEmail = senderEmail; 
	}

	public String getRestUser() {
		return valueOrDefault(restUser, DEFAULT_REST_USER);
	}

	public void setRestUser(String restUser) {
		this.restUser = restUser;
	}

	public String getRestPassword() {
		return valueOrDefault(restPassword, DEFAULT_REST_PASSWORD);
	}

	public void setRestPassword(String restPassword) {
		this.restPassword = restPassword;
	}

	private static String valueOrDefault(String value, String defaultValue) {
		return value == null || value.isEmpty() ? defaultValue : value;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		PluginSettings that = (PluginSettings) o;

		if (getSmtpHost() != null ? !getSmtpHost().equals(that.getSmtpHost()) : that.getSmtpHost() != null)
			return false;
		if (getSmtpPort() != null ? !getSmtpPort().equals(that.getSmtpPort()) : that.getSmtpPort() != null)
			return false;
		if (getSenderEmail() != null ? !getSenderEmail().equals(that.getSenderEmail()) : that.getSenderEmail() != null)
			return false;
		if (getRestUser() != null ? !getRestUser().equals(that.getRestUser()) : that.getRestUser() != null)
			return false;
		return getRestPassword() != null ? getRestPassword().equals(that.getRestPassword()) : that.getRestPassword() == null;
	}

	@Override
	public int hashCode() {
		int result = getSmtpHost() != null ? getSmtpHost().hashCode() : 0;
		result = 31 * result + (getSmtpPort() != null ? getSmtpPort().hashCode() : 0);
		result = 31 * result + (getSenderEmail() != null ? getSenderEmail().hashCode() : 0);
		result = 31 * result + (getRestUser() != null ? getRestUser().hashCode() : 0);
		result = 31 * result + (getRestPassword() != null ? getRestPassword().hashCode() : 0);
		return result;
	}

	public static class Field {
		public String displayName;
		public String defaultValue;
		public boolean isRequired;
		public boolean isSecure;
		public int displayOrder;
		public Field(String displayName, String defaultValue, boolean isRequired, boolean isSecure,
				int displayOrder) {
			this.displayName = displayName;
			this.defaultValue = defaultValue;
			this.isRequired = isRequired;
			this.isSecure = isSecure;
			this.displayOrder = displayOrder;
		}
	}

}
